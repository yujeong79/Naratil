"""
텍스트 데이터 벡터화 처리 모듈
"""
import logging
import pandas as pd
import re
from pyspark.sql.functions import col, when, pandas_udf
from pyspark.sql.types import ArrayType, FloatType
from sentence_transformers import SentenceTransformer

# 로거 설정
logger = logging.getLogger(__name__)

# 모델 캐싱 함수
def get_model():
    """모델 로드 (캐싱 적용)"""
    if not hasattr(get_model, "model"):
        logger.info("🤖 모델 로딩 중 (캐싱된 모델 사용)...")
        get_model.model = SentenceTransformer("jhgan/ko-sbert-sts")
        logger.info("✅ 모델 로딩 완료 (캐싱됨)")
    return get_model.model

# 텍스트 정제 함수
def clean_and_filter(texts: pd.Series, stopwords: set) -> pd.Series:
    """텍스트 정제 및 불용어 제거"""
    cleaned = texts.fillna("").apply(lambda x: re.sub(r"\[.*?\]|\(.*?\)|\{.*?\}|<.*?>", "", x))
    cleaned = cleaned.apply(lambda x: re.sub(r"[#@!*%^&]", "", x))
    cleaned = cleaned.apply(lambda x: re.sub(r"\s+", " ", x).strip())
    cleaned = cleaned.apply(
        lambda x: re.sub(
            r"\s+", " ",
            "".join([x.replace(word, "") for word in stopwords])
        ).strip()
    )
    return cleaned

def vectorize_data(spark, df, stopwords):
    """데이터 벡터화"""
    logger.info("⚙️ 데이터 벡터화 시작...")

    @pandas_udf(ArrayType(FloatType()))
    def vectorize_udf(texts: pd.Series) -> pd.Series:
        cleaned = clean_and_filter(texts, stopwords)
        model = get_model()
        vectors = model.encode(cleaned.tolist(), convert_to_numpy=True)
        return pd.Series([vec.tolist() for vec in vectors])
    
    df_vector = df.withColumn("vectorNm", vectorize_udf(col("bidNtceNm")))

    columns_to_clean = [c for c in df_vector.columns if c != "vectorNm" and c != "bizs_parsed"]
    for col_name in columns_to_clean:
        df_vector = df_vector.withColumn(
            col_name,
            when((col(col_name) == "") | (col(col_name) == "NaN"), None).otherwise(col(col_name))
        )
    
    logger.info("✅ 벡터화 및 null 정제 완료")
    sample_vectors = df_vector.select("bidNtceNm", "vectorNm").limit(2)
    logger.info("🔍 벡터화 결과 샘플:")
    for row in sample_vectors.collect():
        logger.info(f"  원본 텍스트: {row['bidNtceNm']}")
        logger.info(f"  벡터 차원: {len(row['vectorNm'])}")
        logger.info(f"  벡터 일부: {row['vectorNm'][:5]}...")
        logger.info("---")
    
    return df_vector
