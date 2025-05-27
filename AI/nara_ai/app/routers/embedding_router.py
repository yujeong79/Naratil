from app.config.logging_config import embedding_logger as logger

from app.models.commons import NtceBase, IndustryBase
from app.models.embedding import NtceVectorItem, IndustryVectorItem, NtceVectorFail, IndustryVectorFail
from app.services.embedding import generate_ntce_vectors, generate_industry_vectors

from typing import List
from pydantic import BaseModel, Field

from fastapi import APIRouter

# 공고명 벡터 요청
class NtceVectorRequest(BaseModel):
    '''
    공고명 벡터화 요청
    - 리스트[공고 ID, 공고명]
    '''
    ntces: List[NtceBase] = Field(..., description="공고 정보 리스트")

class NtceVectorResponse(BaseModel):
    '''
    공고 벡터화 결과 (성공 / 실패)
    '''
    success: List[NtceVectorItem] = Field(..., description="임베딩 성공 공고")
    failed: List[NtceVectorFail] = Field(..., description="임베딩 실패 공고")


# FastAPI Router 생성
router = APIRouter()

# (최대100개)의 공고명을 벡터로 변환
@router.post("/ntce_vectors", response_model=NtceVectorResponse)
async def embed_ntce_names(request: NtceVectorRequest):
    '''
    공고 임베딩 요청
    '''
    logger.info(f"[요청] 공고명 벡터화 요청 수: {len(request.ntces)}")

    # 비동기 벡터화 작업
    results = await generate_ntce_vectors(ntces=request.ntces)
    logger.info(f"[성공] 공고명 벡터화 완료: {len(results.success)}개 / [실패] 공고명 벡터화 실패: {len(results.failed)}개")
    return NtceVectorResponse(success=results.success, failed=results.failed)




class IndustryVectorResponse(BaseModel):
    '''
    업종 벡터화 결과 (성공 / 실패)
    '''
    success: List[IndustryVectorItem] = Field(..., description="임베딩 성공 업종")
    failed: List[IndustryVectorFail] = Field(..., description="임베딩 실패 업종")


# 업종명 벡터 요청
class IndustryVectorRequest(BaseModel):
    '''
    업종명 벡터화 요청
    '''
    industries: List[IndustryBase] = Field(..., description="업종 정보 리스트")

# 업종명을 벡터로 변환
@router.post("/industry_vectors", response_model=IndustryVectorResponse)
async def embed_industry_names(request: IndustryVectorRequest):
    '''
    업종명 임베딩 요청
    '''
    logger.info(f"[요청] 업종명 벡터화 요청 수: {len(request.industries)}")

    # 비동기 벡터화 작업
    results = await generate_industry_vectors(industries=request.industries)
    logger.info(f"[성공] 업종명 벡터화 완료: {len(results.success)}개 / [실패] 업종명 벡터화 실패: {len(results.failed)}개")
    return IndustryVectorResponse(success=results.success, failed=results.failed)