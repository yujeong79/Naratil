import re
import pandas as pd
import numpy as np
import time
import os

from pyspark.sql import SparkSession
from pyspark.sql.functions import pandas_udf, col, when, avg, count, lit
from pyspark.sql.types import ArrayType, FloatType, StringType
from sentence_transformers import SentenceTransformer

# ==============================
# ✅ 요구사항 정리
# ==============================
# 1. lcnsLmtNm을 업종명 / 코드로 분리하여 각각 lcnsLmtNm, Industry_code 컬럼에 저장
# 2. 벡터화는 bidNtceNm 기준으로 768차원 벡터 생성
# 3. null, 빈 문자열, "NaN", np.nan → 모두 null 처리
# 4. null이어도 json에 누락되지 않고 "컬럼명": null 형태로 출력되게 하기
# 5. Industry_code별 공고 수 집계 후
#    - null → no_limit 폴더에 저장
#    - 평균 미만 → etc 폴더에 저장
#    - 평균 이상 → Industry_code별 단일 폴더 저장
# 6. 모델은 캐싱하여 여러 번 로딩 방지
# ==============================

# 경로 설정
PARQUET_PATH = "hdfs://localhost:9000/user/hadoop/data/FinalData/Thing/Final_Thing_202410.parquet"
STOPWORDS_PATH = "data/stopwords.csv"
OUTPUT_DIR = "data"
os.makedirs(OUTPUT_DIR, exist_ok=True)

start_time = time.time()
print("\n🚀 Spark 시작...")

# Spark 세션 생성
spark = SparkSession.builder \
    .appName("Vectorize and Split") \
    .master("local[*]") \
    .config("spark.driver.memory", "4g") \
    .getOrCreate()

# 데이터 로딩
print("\n📦 Parquet 데이터 로딩 중...")
df = spark.read.parquet(PARQUET_PATH).repartition(8)
print(f"✅ 데이터 로딩 완료: {df.count()} rows")

# Stopword 불러오기
stopwords = set(pd.read_csv(STOPWORDS_PATH)["stopword"].tolist())

# 텍스트 정제 함수
def clean_and_filter(texts: pd.Series) -> pd.Series:
    cleaned = texts.fillna("").apply(lambda x: re.sub(r"\[.*?\]|\(.*?\)|\{.*?\}|<.*?>", "", x))
    cleaned = cleaned.apply(lambda x: re.sub(r"[#@!*%^&]", "", x))
    cleaned = cleaned.apply(lambda x: re.sub(r"\s+", " ", x).strip())
    cleaned = cleaned.apply(lambda x: " ".join([w for w in x.split() if w not in stopwords]))
    return cleaned

# 모델 캐싱 함수
def get_model():
    if not hasattr(get_model, "model"):
        print("\n🤖 모델 로딩 중 (캐싱된 모델 사용)...")
        get_model.model = SentenceTransformer("jhgan/ko-sbert-sts")
        print("✅ 모델 로딩 완료 (캐싱됨)")
    return get_model.model

# UDF 정의
@pandas_udf(ArrayType(FloatType()))
def vectorize_udf(texts: pd.Series) -> pd.Series:
    cleaned = clean_and_filter(texts)
    model = get_model()
    vectors = model.encode(cleaned.tolist(), convert_to_numpy=True)
    return pd.Series([vec.tolist() for vec in vectors])

@pandas_udf(StringType())
def extract_name_udf(col_text: pd.Series) -> pd.Series:
    return col_text.apply(lambda x: x.split("/")[0].strip() if isinstance(x, str) and "/" in x else None)

@pandas_udf(StringType())
def extract_code_udf(col_text: pd.Series) -> pd.Series:
    return col_text.apply(lambda x: x.split("/")[-1].strip() if isinstance(x, str) and "/" in x else None)

# UDF 적용
print("\n⚙️ 벡터화 및 업종 파싱 중...")
df_vector = (
    df.withColumn("Industry_code", extract_code_udf(col("lcnsLmtNm")))
      .withColumn("lcnsLmtNm", extract_name_udf(col("lcnsLmtNm")))
      .withColumn("vectorNm", vectorize_udf(col("bidNtceNm")))
)

# 모든 컬럼에서 공백/NaN/null 통합 정제
columns_to_clean = [c for c in df_vector.columns if c != "vectorNm"]
for col_name in columns_to_clean:
    df_vector = df_vector.withColumn(
        col_name,
        when((col(col_name) == "") | (col(col_name) == "NaN"), None).otherwise(col(col_name))
    )

print("✅ null 정제 완료")

# 공고 수 집계 및 평균 계산
industry_count = df_vector.groupBy("Industry_code").agg(count("bidNtceNo").alias("cnt"))
avg_count = industry_count.agg(avg("cnt")).first()[0]
print(f"📊 Industry 평균 공고 수: {avg_count:.2f}")

# 조건 필터링
df_vector = df_vector.join(industry_count, on="Industry_code", how="left")
no_limit_df = df_vector.filter(col("Industry_code").isNull())
etc_df = df_vector.filter((col("cnt") < avg_count) & col("Industry_code").isNotNull())
greater_df = df_vector.filter(col("cnt") >= avg_count)

# 저장
print("\n💾 JSON 파일 저장 중...")
no_limit_df.coalesce(1).write.mode("overwrite").json(f"{OUTPUT_DIR}/no_limit")
etc_df.coalesce(1).write.mode("overwrite").json(f"{OUTPUT_DIR}/etc")

for row in greater_df.select("Industry_code").distinct().collect():
    code = row["Industry_code"]
    print(f"✅ {code} 업종 저장 중...")
    df_code = greater_df.filter(col("Industry_code") == code)
    df_code.coalesce(1).write.mode("overwrite").json(f"{OUTPUT_DIR}/{code}")

print("✅ 모든 JSON 저장 완료")
print("⏱️ 총 소요 시간: {:.2f}초".format(time.time() - start_time))
