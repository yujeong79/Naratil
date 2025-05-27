import re
import pandas as pd
from pyspark.sql import SparkSession
from pyspark.sql.functions import udf, col
from pyspark.sql.types import ArrayType, FloatType
from sentence_transformers import SentenceTransformer
import numpy as np

# 1. Spark 세션 생성
spark = SparkSession.builder \
    .appName("Vectorize Titles") \
    .master("local[*]") \
    .config("spark.driver.memory", "2g") \
    .getOrCreate()

# 2. HDFS에서 parquet 읽기 (상위 1개만)
parquet_path = "hdfs://localhost:9000/user/hadoop/data/FinalData/Thing/Final_Thing_202410.parquet"
df = spark.read.parquet(parquet_path).limit(1)

# 3. Stopword 불러오기
stopwords_path = "data/stopwords.csv"
stopwords_df = pd.read_csv(stopwords_path)
STOPWORDS = set(stopwords_df["stopword"].tolist())

# 4. 텍스트 전처리 함수
def clean_text(text):
    text = re.sub(r"\[.*?\]|\(.*?\)|\{.*?\}|<.*?>", "", text)
    text = re.sub(r"[#@!*%^&]", "", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text

def remove_stopwords(text, stopwords):
    return " ".join([word for word in text.split() if word not in stopwords])

# 5. 벡터 변환 함수
model = SentenceTransformer("jhgan/ko-sbert-sts")
def vectorize(text):
    if not isinstance(text, str) or text.strip() == "":
        return [0.0] * 768
    text = clean_text(text)
    text = remove_stopwords(text, STOPWORDS)
    return model.encode(text, convert_to_numpy=True).tolist()

# 6. UDF 등록
vector_udf = udf(vectorize, ArrayType(FloatType()))

# 7. 벡터화 및 컬럼 추가
df_vectorized = df.withColumn("title_vector", vector_udf(col("bidNtceNm")))

# 8. 로컬에 JSON으로 저장
output_path = "data/vectorized_result.json"
df_vectorized.toPandas().to_json(output_path, orient="records", force_ascii=False)

print("✅ 상위 1건 저장 완료 →", output_path)
