from dataclasses import dataclass
from typing import List

from app.services.embedding.generator import generate_vectors, EmbeddingItem
from app.models.commons import NtceBase
from app.models.embedding import NtceVectorItem, NtceVectorFail
from pydantic import Field


@dataclass
class NtceEmbeddingResult:
    '''
    공고 임베딩 결과
    '''
    success: List[NtceVectorItem] = Field(..., description="임베딩 성공 공고 목록 ")
    failed: List[NtceVectorFail] = Field(..., description="임베딩 실패 공고 목록")


# 공고 벡터 생성 함수 (일반 벡터 생성 함수 호출)
async def generate_ntce_vectors(ntces: List[NtceBase]) -> NtceEmbeddingResult:

    # 임베딩 공통 객체로 변환
    embedding_items = [
        EmbeddingItem(
            id=ntce.bid_ntce_id,
            text=ntce.bid_ntce_nm
        ) for ntce in ntces
    ]

    # 임베딩 실행 결과
    embedding_results = await generate_vectors(items=embedding_items, domain="공고")

    # 임베딩 성공 목록
    success = [
        NtceVectorItem(
            bid_ntce_id=item.id,
            bid_ntce_nm=item.text,
            vector=item.vector
        ) for item in embedding_results.success
    ]

    # 암배당 실패 목록
    failed = [
        NtceVectorFail(
            bid_ntce_id=item.id,
            bid_ntce_nm=item.text,
            reason=item.reason
        ) for item in embedding_results.failed
    ]

    return NtceEmbeddingResult(success=success, failed=failed)