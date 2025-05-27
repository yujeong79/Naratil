from app.config.logging_config import similarity_logger as logger

from app.models.commons import BaseCamelModel
from app.models.similarity import NtceForComparison, SimilarNtceCondition, SimilarNtceItem
from app.services.similarity_service import get_similar_ntces_by_list

from typing import Dict, List
from pydantic import Field

from fastapi import APIRouter, HTTPException

router = APIRouter()

class SimilarNtcesRequest(BaseCamelModel):
    '''
    여러 공고에 대한 유사 과거 공고 검색 요청
    '''
    current_ntces: List[NtceForComparison] = Field(..., description="현재 공고")
    similar_ntce_condition: SimilarNtceCondition = Field(..., description="유사 공고 산출 조건")


class SimilarNtcesResponse(BaseCamelModel):
    '''
    여러 공고에 대한 유사 과거 공고 검색 결과 반환
    '''
    results: Dict[int, List[SimilarNtceItem]] = Field(..., description="공고 ID → 유사 공고 목록")


# 현재 공고의 과거 유사 공고 top-k 뽑기
@router.post("/ntces", response_model=SimilarNtcesResponse)
async def compare_current_to_past_bids(request: SimilarNtcesRequest):
    logger.info(f"[요청] 현재 공고와 유사한 과거 공고 요청")
    try:
        results = get_similar_ntces_by_list(specific_ntces=request.current_ntces,
                                    similar_ntce_condition=request.similar_ntce_condition)
        logger.info(f"[성공] 현재 공고 {len(request.current_ntces)}건 과거 유사 공고 산출 완료")
        return SimilarNtcesResponse(results=results)

    except Exception as e:
        logger.exception("[FastAPI] 유사 공고 계산 중 예외 발생", exc_info=e)
        raise HTTPException(status_code=500, detail="유사 공고 산출 실패")
