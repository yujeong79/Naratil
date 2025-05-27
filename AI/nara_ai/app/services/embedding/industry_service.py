from dataclasses import dataclass
from typing import List

from app.models.commons import IndustryBase
from app.models.embedding import IndustryVectorItem, IndustryVectorFail
from app.services.embedding.generator import EmbeddingItem, generate_vectors
from pydantic import Field

@dataclass
class IndustryEmbeddingResult:
    '''
    업종 임베딩 결과
    '''
    success: List[IndustryVectorItem] = Field(..., description="임베팅 성공 업종 목록")
    failed: List[IndustryVectorFail] = Field(..., description="임베딩 실패 업종 목록")

# 업종 벡터 생성 함수 (일반 벡터 생성 함수 호출)
async def generate_industry_vectors(industries: List[IndustryBase]) -> IndustryEmbeddingResult:

    # 임베딩 공통 객체로 변환
    embedding_items = [
        EmbeddingItem(
            id=industry.industry_id,
            text=industry.industry_nm
        ) for industry in industries
    ]

    # 임베딩 실행 결과
    embedding_results = await generate_vectors(items=embedding_items, domain="업종")

    # 임베딩 성공 목록
    success = [
        IndustryVectorItem(
            industry_id=item.id,
            industry_nm=item.text,
            vector=item.vector
        ) for item in embedding_results.success
    ]

    # 암배당 실패 목록
    failed = [
        IndustryVectorFail(
            industry_id=item.id,
            industry_nm=item.text,
            reason=item.reason
        ) for item in embedding_results.failed
    ]

    return IndustryEmbeddingResult(success=success, failed=failed)