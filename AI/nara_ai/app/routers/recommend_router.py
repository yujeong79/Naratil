import logging
from typing import List, Dict

from fastapi import APIRouter, HTTPException
from pydantic import Field

from app.models.commons import BaseCamelModel
from app.models.recommend import RecommendedNtceScore
from app.models.similarity import NtceForComparison, SimilarNtceCondition
from app.services.recommend_service import get_recommended_ntces_by_list

# FastAPI Router 생성
router = APIRouter()

## key: corp_id (가입한 기업의 번호)
class RecommendNtcesRequest(BaseCamelModel):
    '''
    기업들의 과거 낙찰  공고들과 현재 공고들을 비교하여 가장 유사한 현재 공고 Top-K 요청
    '''
    corp_ntces: Dict[int, List[NtceForComparison]] = Field(..., description="기업별 과거 공고 리스트")
    similar_ntce_condition: SimilarNtceCondition = Field(..., description="추천 공고 산출 조건")

## key: corp_id (가입한 기업의 번호)
class RecommendNtcesResponse(BaseCamelModel):
    '''
    과거에 낙찰됐던 공고와 유사한 현재 공고 리스트 반환
    '''
    recommended_ntces: Dict[int, List[RecommendedNtceScore]] = Field(..., description="기업별 추천 공고 목록")

@router.post("/ntces", response_model=RecommendNtcesResponse)
async def recommend_ntces_from_history(request: RecommendNtcesRequest):
    '''
    과거 낙찰 기록을 기반으로 현재 진행중인 공고 추천
    :param request: 과거 낙찰 기록, 현재 진행중인 공고 정보
    :return: 추천 목록
    '''
    logging.info(f"[요청] 공고 추천: {len(request.corp_ntces)}개 기업")
    try:
        recommended_ntces = get_recommended_ntces_by_list(corp_ntces=request.corp_ntces,
                                                          similar_ntce_condition=request.similar_ntce_condition)
        logging.info(f"[성공] 공고 추천 {len(recommended_ntces.keys())}개 기업")
        return RecommendNtcesResponse(recommended_ntces=recommended_ntces)

    except Exception as e:
        logging.error(f"[실패] 공고 추천 실패")
        raise HTTPException(status_code=500, detail=str(e))
