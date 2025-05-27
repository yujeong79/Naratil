# ~/Project/daily/vectorizer.py
"""
텍스트 데이터 벡터화 처리 모듈
"""
import logging
import pandas as pd
import re
import os
from pyspark.sql.functions import col, when, pandas_udf
from pyspark.sql.types import ArrayType, FloatType
from sentence_transformers import SentenceTransformer

import config_daily as config

# 로거 설정
logger = logging.getLogger(__name__)

# 모델 캐싱 함수
def get_model():
    """모델 로드 (캐싱 적용)"""
    if not hasattr(get_model, "model"):
        logger.info("🤖 모델 로딩 중 (캐싱된 모델 사용)...")
        get_model.model = SentenceTransformer(config.VECTOR_MODEL)
        logger.info("✅ 모델 로딩 완료 (캐싱됨)")
    return get_model.model

# 불용어 로드 함수 (캐싱 적용)
def load_stopwords():
    """불용어 리스트 로드 (캐싱 적용)"""
    if not hasattr(load_stopwords, "stopwords"):
        stopwords_path = config.STOPWORDS_PATH
        try:
            if os.path.exists(stopwords_path):
                df = pd.read_csv(stopwords_path, header=None)
                load_stopwords.stopwords = set(df[0].str.strip())
                logger.info(f"📚 불용어 사전 로드 완료: {len(load_stopwords.stopwords)}개")
            else:
                logger.warning(f"⚠️ 불용어 파일을 찾을 수 없습니다: {stopwords_path}")
                load_stopwords.stopwords = set()
        except Exception as e:
            logger.error(f"❌ 불용어 로드 오류: {e}")
            load_stopwords.stopwords = set()
    return load_stopwords.stopwords

# 텍스트 정제 함수 (불용어 제거 포함)
def clean_and_filter(texts: pd.Series, stopwords: set) -> pd.Series:
    """텍스트 정제 및 불용어 제거"""
    # 특수문자, 괄호 내용 제거
    cleaned = texts.fillna("").apply(lambda x: re.sub(r"\[.*?\]|\(.*?\)|\{.*?\}|<.*?>", "", x))
    cleaned = cleaned.apply(lambda x: re.sub(r"[#@!*%^&]", "", x))
    cleaned = cleaned.apply(lambda x: re.sub(r"\s+", " ", x).strip())
    
    # 여기서 불용어 제거
    #cleaned = cleaned.apply(lambda x: " ".join([w for w in x.split() if w not in stopwords]))
    cleaned = cleaned.apply(
    lambda x: re.sub(r"\s+", " ", 
                     "".join([x.replace(word, "") for word in stopwords])
                    ).strip()
)

    return cleaned

def vectorize_data(spark, df):
    """데이터 벡터화"""
    logger.info("⚙️ 데이터 벡터화 시작...")
    
    # 불용어 로드
    stopwords = load_stopwords()
    
    # UDF 정의
    @pandas_udf(ArrayType(FloatType()))
    def vectorize_udf(texts: pd.Series) -> pd.Series:
        # 불용어 제거를 포함한 텍스트 정제
        cleaned = clean_and_filter(texts, stopwords)
        model = get_model()
        
        # 청크(배치) 단위로 벡터화 처리
        all_vectors = []
        for i in range(0, len(cleaned), config.BATCH_SIZE):
            batch_texts = cleaned.iloc[i:i + config.BATCH_SIZE].tolist()
            vectors = model.encode(batch_texts, convert_to_numpy=True)
            all_vectors.extend(vectors.tolist())
        
        return pd.Series(all_vectors)
    
    # 벡터화 적용
    df_vector = df.withColumn("vectorNm", vectorize_udf(col("bidNtceNm")))
    
    # 데이터 null 처리
    columns_to_clean = [c for c in df_vector.columns if c != "vectorNm" and c != "bizs_parsed"]
    for col_name in columns_to_clean:
        df_vector = df_vector.withColumn(
            col_name,
            when((col(col_name) == "") | (col(col_name) == "NaN"), None).otherwise(col(col_name))
        )
    
    logger.info("✅ 벡터화 및 null 정제 완료")
    
    # 벡터화 결과 샘플 확인
    sample_vectors = df_vector.select("bidNtceNm", "vectorNm").limit(2)
    logger.info("🔍 벡터화 결과 샘플:")
    for row in sample_vectors.collect():
        logger.info(f"  원본 텍스트: {row['bidNtceNm']}")
        logger.info(f"  벡터 차원: {len(row['vectorNm'])}")
        logger.info(f"  벡터 일부: {row['vectorNm'][:5]}...")  # 앞부분 5개 요소만 출력
        logger.info("---")
    
    return df_vector
