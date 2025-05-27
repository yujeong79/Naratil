from dataclasses import dataclass

from fastapi import HTTPException
from app.config.logging_config import embedding_logger as logger

import asyncio
from typing import List, Optional

from app.services.embedding import encode_text_async

@dataclass
class EmbeddingItem:
    id: int
    text: str

# 임베딩 성공 정보
@dataclass
class EmbeddingSuccess:
    id: int
    text: str
    vector: Optional[List[float]] = None

# 임베딩 실패 정보
@dataclass
class EmbeddingFail:
    id: int
    text: str
    reason: str

# 임베딩 결과
@dataclass
class EmbeddingResult:
    success: List[EmbeddingSuccess]
    failed: List[EmbeddingFail]


# 벡터 생성 함수 (성공/실패 분리하여 EmbeddingResult 반환)
async def generate_vectors(items: List[EmbeddingItem], domain: str) -> EmbeddingResult:
    logger.debug(f"[요청] 총 {len(items)}개 {domain} 벡터화 시작")

    # 단일 벡터화 처리
    async def _process(item: EmbeddingItem):
        try:
            logger.debug(f"[시작] {domain} ID {item.id} 벡터화 시작")
            vector = await encode_text_async(item.text)
            item.vector = vector.tolist()
            logger.debug(f"[완료] {domain} ID {item.id} 벡터화 완료")
            return "success", item

        except ValueError as e:
            logger.warning(f"[입력 오류] {domain} ID {item.id} - {e}")
            return ("failed", EmbeddingFail(
                id=item.id,
                text=item.text,
                reason=str(e)
            ))

        except Exception as e:
            logger.error(f"[벡터화 실패] {domain} ID {item.id} - {e}")
            return ("failed", EmbeddingFail(
                id=item.id,
                text=item.text,
                reason="벡터 생성 실패"
            ))

    # 전체 비동기 실행
    results = await asyncio.gather(*[_process(item) for item in items])

    success, failed = [], []
    for status, value in results:
        if status == "success":
            success.append(value)
        else:
            failed.append(value)

    logger.debug(f"[성공] {len(success)}개 / [실패] {len(failed)}개 {domain} 벡터화 완료")

    return EmbeddingResult(success=success, failed=failed)
