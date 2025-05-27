import math

from app.config.logging_config import recommend_logger as logger

from typing import List, Dict

from fastapi import HTTPException
from app.core.index_manager.ntce_index_manager import NtceIndexManager
from app.core.index_manager.manager_instance import current_index_manager
from app.models.recommend import RecommendedNtceScore
from app.models.similarity import NtceForComparison, SimilarNtceCondition, SimilarNtceItem
from app.services.similarity_service import get_similar_ntces_by_one

def get_recommended_ntces_by_list(
    corp_ntces: Dict[int, List[NtceForComparison]],
    similar_ntce_condition: SimilarNtceCondition
) -> Dict[int, List[RecommendedNtceScore]]:
    """
    기업들의 과거 공고를 기반으로 추천 공고 산출
    :param corp_ntces: 기업들
    :param similar_ntce_condition: 산출 조건
    :return: 기업별 추천 공고
    """

    cosine_top_k = similar_ntce_condition.cosine_top_k
    jaccard_top_k = similar_ntce_condition.jaccard_top_k
    final_top_k = similar_ntce_condition.final_top_k
    jaccard_weight = similar_ntce_condition.jaccard_weight
    cosine_weight = similar_ntce_condition.cosine_weight
    cosine_lower_bound = similar_ntce_condition.cosine_lower_bound
    lower_bound = similar_ntce_condition.lower_bound

    max_score = cosine_weight * 1 + jaccard_weight * 1
    min_score = cosine_weight * -1

    logger.info(f"[추천 공고] 조건: {similar_ntce_condition}")

    results: Dict[int, List[RecommendedNtceScore]] = {}
    for corp in corp_ntces.keys(): # id
        recommend = get_recommended_ntces_by_one(
            corp_id=corp,
            specific_ntces=corp_ntces[corp],
            cosine_top_k=cosine_top_k,
            jaccard_top_k=jaccard_top_k,
            final_top_k=final_top_k,
            cosine_weight=cosine_weight,
            jaccard_weight=jaccard_weight,
            cosine_lower_bound=cosine_lower_bound,
            lower_bound=lower_bound,
            max_score=max_score,
            min_score=min_score,
            index_manager=current_index_manager
        )
        results[corp] = recommend
        logger.debug(f"[추천 공고] 추천 정보(ID: {corp}): {recommend}")

    logger.info(f"[최종 결과] 기업 {len(corp_ntces)}건에 대한 유사 공고 산출 완료")
    return results

def get_recommended_ntces_by_one(
    corp_id: int,
    specific_ntces:List[NtceForComparison],
    cosine_top_k:int,
    jaccard_top_k:int,
    final_top_k:int,
    cosine_weight:float,
    jaccard_weight:float,
    cosine_lower_bound:float,
    lower_bound:float,
    max_score:float,
    min_score:float,
    index_manager:NtceIndexManager,
) -> List[RecommendedNtceScore]:
    """
    특정 공고들을 기반으로 비교 공고들 중 추천 공고 산출
    """

    if not specific_ntces:
        logger.warning("과거 낙찰 정보가 존재하지 않습니다.")
        return []

    results: List[List[SimilarNtceItem]] = []
    for specific_ntce in specific_ntces:
        result = get_similar_ntces_by_one(
                    specific_ntce=specific_ntce,
                    cosine_top_k=cosine_top_k,
                    jaccard_top_k=jaccard_top_k,
                    final_top_k=final_top_k,
                    cosine_weight=cosine_weight,
                    jaccard_weight=jaccard_weight,
                    cosine_lower_bound=cosine_lower_bound,
                    lower_bound=lower_bound,
                    max_score=max_score,
                    min_score=min_score,
                    index_manager=index_manager
                )

        results.append(result)

    # 클래스 변환
    scored_ntces: List[RecommendedNtceScore] = []
    for result in results: # List[List[SimilarNtceItem]]
        for ntce in result: # List[SimilarNtceItem]
            try:
                score_ntce = RecommendedNtceScore(
                                bid_ntce_id=ntce.bid_ntce_id,
                                total_score=ntce.final_score,
                                avg_score=0,  # 임시
                                adjust_score=0, # 임시
                                count=1
                            )
                logger.debug(f"[추천 변환] 공고 ID: {ntce.bid_ntce_id} -> score_ntce: {score_ntce}")
                scored_ntces.append(score_ntce)
            except Exception as e:
                logger.error(f"[추천 변환 실패] 공고 ID {ntce.bid_ntce_id} 변환 오류: {e}")

    logger.debug(f"[추천 변환] 변환된 총 건수: {len(scored_ntces)}")

    # 스코어 누적
    ntce_scores: Dict[int, Dict[str, float]] = {}  # key: bid_ntce_id, value {"total_scroe" : float, "count" : float}
    for ntce in scored_ntces:
        try:
            if ntce.bid_ntce_id not in ntce_scores:
                ntce_scores[ntce.bid_ntce_id] = {"total_score": 0, "count": 0} # 초기 세팅

            ntce_scores[ntce.bid_ntce_id]["total_score"] += ntce.total_score
            ntce_scores[ntce.bid_ntce_id]["count"] += ntce.count
            logger.debug(f"[추천 점수 누적] 공고 ID: {ntce.bid_ntce_id} -> 누적 점수 현황: {ntce_scores[ntce.bid_ntce_id]}")
        except Exception as e:
            logger.error(f"[추천 점수 누적 실패] 공고 ID {ntce.bid_ntce_id} 변환 오류: {e}")

    # 평균 점수 계산
    # avg_scored :List[RecommendedNtceScore]
    adjust_scored = []
    for bid_ntce_id, ntce_score in ntce_scores.items():
        try:
            if ntce_score["count"] == 0:
                logger.warning(f"[평균 점수 계산 스킵] 공고 ID: {bid_ntce_id} -> 데이터 수집 개수 0개")
                continue

            avg_score = round(ntce_score["total_score"] / ntce_score["count"], 7)

            alpha = 0.05  # 조정 계수 (5%)
            adjust_score = round(avg_score * (1 + alpha * math.log(ntce_score["count"] + 1)), 7)


            score_item = RecommendedNtceScore(
                bid_ntce_id=bid_ntce_id,
                total_score=ntce_score["total_score"],
                avg_score=avg_score,
                adjust_score=adjust_score,
                count=int(ntce_score["count"])
            )

            logger.debug(f"[평균 점수 계산] ID={bid_ntce_id}, 평균={avg_score}")
            adjust_scored.append(score_item)

        except Exception as e:
            logger.error(f"[평균 점수 계산 실패] 공고 ID={bid_ntce_id}, 공고={ntce_score} / 에러: {e}")

    # Top K 정렬
    top_k = sorted(adjust_scored, key=lambda x: x.adjust_score, reverse=True)[:final_top_k]

    logger.info(f"[최종 결과] 공고 ID:{corp_id} {final_top_k}건 요청 -> 추천 공고 {len(top_k)}건 반환")
    return top_k

