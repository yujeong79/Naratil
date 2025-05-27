import asyncio
import re

import pandas as pd
import numpy as np
from sentence_transformers import SentenceTransformer
from app.config.exceptions.error_code import ErrorCode
from app.config.logging_config import embedding_logger as logger

# 지연 로딩을 위한 글로벌 변수
_model = None
_stopwords = None

# 모델과 Stopword를 처음 요청 시에만 로드
def get_model():
    global _model
    if _model is None:
        try:
            logger.info("SBERT 모델 로딩 중...")
            _model = SentenceTransformer("jhgan/ko-sbert-sts", cache_folder="models/")
            logger.info("SBERT 모델 로딩 완료")
        except Exception as e:
            logger.error(f"SBERT 모델 로딩 실패: {e}", exc_info=True)
            raise
    return _model

def get_stopwords():
    global _stopwords
    if _stopwords is None:
        try:
            logger.info("Stopwords 로딩 중...")
            df = pd.read_csv("resources/stopwords.csv")
            _stopwords = set(df["stopwords"].tolist())
            logger.info(f"Stopwords {len(_stopwords)}개 로딩 완료")
        except Exception as e:
            logger.error(f"Stopwords 로딩 실패: {e}", exc_info=True)
            raise
    return _stopwords

# 특수문자 제거
def clean_text(text: str):
    text = re.sub(r"\[.*?\]|\(.*?\)|\{.*?\}|<.*?>", "", text)
    text = re.sub(r"[#@!*%^&]", "", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text

# 불용어 제거
def remove_stopwords(text: str):
    stopwords = get_stopwords()
    for stopword in stopwords:
        text = text.replace(stopword, " ")
    text = re.sub(r"\s+", " ", text)  # 연속된 공백 하나로 줄이기
    return text.strip()

# 최종 벡터 변환 함수
def get_vector(text: str) -> np.ndarray:
    if not isinstance(text, str) or not text.strip():
        logger.warning("빈 문자열 또는 비정상 입력")
        raise ValueError(ErrorCode.EMPTY_TEXT.message())

    try:
        logger.debug(f"[시작] 벡터화 시작: {text[:30]}...")
        text = clean_text(text)
        text = remove_stopwords(text)
        model = get_model()
        vector = model.encode(text, convert_to_numpy=True)
        logger.debug(f"[성공] 벡터화 완료: Dimension {vector.shape}")
        return vector

    except Exception as e:
        logger.error(f"벡터화 중 예외 발생: {e}", exc_info=True)
        raise RuntimeError(ErrorCode.EMBEDDING_FAILED.message())

# 벡터 변환 비동기 호출 함수
async def encode_text_async(text: str) -> np.ndarray:
    loop = asyncio.get_event_loop()
    return await loop.run_in_executor(None, get_vector, text)
