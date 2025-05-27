import logging

from app.config.logging_config import similarity_logger as logger

from typing import Dict, List

from app.core.index_manager.ntce_index_manager import NtceIndexManager
from app.models.similarity import NtceForComparison, SimilarNtceCondition, SimilarNtceItem
from app.core.similarity.cosine_similarity import get_top_k_by_cosine
from app.core.similarity.jaccard_similarity import get_top_k_by_jaccard
from app.core.similarity.final_score import compute_final_score
from app.core.index_manager.manager_instance import past_index_manager

# 현재 공고의 - 과거 유사 공고 뽑기
def get_similar_ntces_by_list(
    specific_ntces: List[NtceForComparison],
    similar_ntce_condition: SimilarNtceCondition
) -> Dict[int, List[SimilarNtceItem]]:
    """
    특정 공고와 비교 대상 공고들 간의 유사도 계산
    :param specific_ntces:
    :param specific_ntce: 유사 공고를 뽑을 타겟 공고들
    :param similar_ntce_condition: 유사도 계산 조건
    :return: 유사 공고 리스트 - 맵
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

    logger.info(f"[유사 공고] 조건: {similar_ntce_condition}")

    results: Dict[int, List[SimilarNtceItem]] = {}
    for specific_ntce in specific_ntces:
        similar_ntces = get_similar_ntces_by_one(
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
            index_manager=past_index_manager
        )
        results[specific_ntce.bid_ntce_id] = similar_ntces
    
    logger.info(f"[최종 결과] 공고 {len(specific_ntces)}건에 대한 유사 공고 산출 완료")
    return results

def get_similar_ntces_by_one(
        specific_ntce:NtceForComparison,
        cosine_top_k:int,
        jaccard_top_k:int,
        final_top_k:int,
        cosine_weight:float,
        jaccard_weight:float,
        cosine_lower_bound:float,
        lower_bound:float,
        max_score:float,
        min_score:float,
        index_manager:NtceIndexManager
        ) -> List[SimilarNtceItem]:
    """
    특정 공고와 비교 대상 공고들 간의 유사도 계산
    :param cosine_lower_bound:
    :param index_manager:
    :param min_score:
    :param max_score:
    :param jaccard_weight:
    :param cosine_weight:
    :param final_top_k:
    :param jaccard_top_k:
    :param cosine_top_k:
    :param lower_bound:
    :param specific_ntce: 유사 공고를 뽑을 타겟 공고
    :return: 유사 공고 리스트
    """

    logging.debug(f"특정 공고 유사 공고 검색 진입 (ID: {specific_ntce.bid_ntce_id})")

    # Step 1: FAISS 유사도
    faiss_top = get_top_k_by_cosine(
        query_ntce=specific_ntce,
        top_k=cosine_top_k,
        lower_bound=cosine_lower_bound,
        index_manager=index_manager
    )

    logger.debug(f"[코사인 상위] {len(faiss_top)}건")

    # Step 2: Jaccard 유사도
    jaccard_top = get_top_k_by_jaccard(
        query_ntce=specific_ntce,
        compare_ntces=faiss_top,
        top_k=jaccard_top_k,
        index_manager=index_manager
    )

    logger.debug(f"[자카드 상위] {len(jaccard_top)}건")

    # Step 3: 최종 스코어 계산
    scored = []
    for ntce in jaccard_top:
        final_score = compute_final_score(
            cosine_score=ntce.cosine_score,
            jaccard_score=ntce.jaccard_score,
            cosine_weight=cosine_weight,
            jaccard_weight=jaccard_weight,
            min_score=min_score,
            max_score=max_score
        )

        ntce.final_score = final_score
        scored.append(ntce)

    # Step 4: 점수 필터링 + 정렬 + 최종 Top-K
    scored = [ntce for ntce in scored if ntce.final_score >= lower_bound]
    scored.sort(key=lambda x: x.final_score, reverse=True)

    final_result = scored[:final_top_k]
    logger.debug(f"[최종 결과] 공고 ID {specific_ntce.bid_ntce_id} : Top-{final_top_k} 요청 -> 반환 {len(final_result)}건, 기준 점수: {lower_bound})")
    return final_result