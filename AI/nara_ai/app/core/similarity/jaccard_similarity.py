from app.config.logging_config import similarity_logger as logger

from typing import List

from app.core.index_manager.ntce_index_manager import NtceIndexManager
from app.models.similarity import NtceForComparison, SimilarNtceItem


# Jaccard 유사도 계산
def jaccard_similarity_from_reference(list1: List[str], list2: List[str]) -> float:
    '''
    set1, set2 교집합 원소 개수 / set1 원소 개수
    - 기준이 되는 쿼리 측(set1) 대비 얼마나 겹치는지를 측정 (Precision 유사)
    '''
    set1 = set(list1)
    set2 = set(list2)
    if not set1 or not set2:
        return 0.0
    return len(set1 & set2) / len(set1)


# Jaccard 유사도 계산 (일반형)
def jaccard_similarity(list1: List[str], list2: List[str]) -> float:
    '''
    set1, set2 교집합 원소 개수 / set1, set2 합집합 원소 개수
    - 대칭적인 일반 자카드 유사도
    '''
    set1 = set(list1)
    set2 = set(list2)
    if not set1 or not set2:
        return 0.0
    return len(set1 & set2) / len(set1 | set2)


# Jaccard 유사도로 top k 선정
def get_top_k_by_jaccard(query_ntce: NtceForComparison, compare_ntces: List[SimilarNtceItem],
                         top_k: int, index_manager: NtceIndexManager) -> List[SimilarNtceItem]:
    '''
    자카드 유사도 기준으로 비교 대상 공고들 중 상위 K개 추출
    '''
    if not compare_ntces:
        logger.warning(f"[Jaccard] 비교 대상 공고가 없습니다. (컬렉션: {query_ntce.collection_name})")
        return []

    keywords_list = index_manager.keywords_map[query_ntce.collection_name] # Dict[int:List[str]]

    jaccard_results: List[SimilarNtceItem] = []
    for ntce in compare_ntces:
        jaccard_score = jaccard_similarity_from_reference(
            list1=query_ntce.keywords,
            list2=keywords_list[ntce.bid_ntce_id]
        )
        ntce.jaccard_score = jaccard_score
        jaccard_results.append(ntce)

    # 점수 기준 정렬
    sorted_results = sorted(jaccard_results, key=lambda x: x.jaccard_score or 0.0, reverse=True)
    top_results = sorted_results[:top_k]

    logger.info(f"[Jaccard] 공고 ID:{query_ntce.bid_ntce_id} Top-{top_k} 요청 → 실제 {len(top_results)}건 추출 완료(컬렉션: {query_ntce.collection_name})")
    return top_results