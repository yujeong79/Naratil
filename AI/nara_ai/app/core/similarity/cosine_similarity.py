from typing import List

from app.core.index_manager.ntce_index_manager import NtceIndexManager
from app.models.similarity import NtceForComparison, SimilarNtceItem
from app.config.logging_config import similarity_logger as logger

# 코사인 유사도로 top k 선정
def get_top_k_by_cosine(query_ntce: NtceForComparison, top_k: int, lower_bound:float, index_manager: NtceIndexManager) -> List[SimilarNtceItem]:
    """
    FAISS 기반 코사인 유사도 계산 후, 점수 상위 Top-K 개 공고 반환
    """
    collection_name = query_ntce.collection_name

    D, I = index_manager.search_faiss(collection_name, query_ntce.vector, top_k)

    faiss_results = []
    for idx, dist in zip(I, D):
        if idx == -1:  # FAISS -1 (match 없음)
            continue

        cos_sim = 1 - 0.5 * dist
        faiss_results.append(
            SimilarNtceItem(
                bid_ntce_id=idx,
                collection_name=collection_name,
                cosine_score=cos_sim,
                jaccard_score=None,
                final_score=None
            )
        )

    scored = [ntce for ntce in faiss_results if ntce.cosine_score >= lower_bound]
    sorted_results = sorted(scored, key=lambda x: x.cosine_score or 0.0, reverse=True)
    top_results = sorted_results[:top_k]

    logger.info(f"[Cosine] 공고 ID:{query_ntce.bid_ntce_id} Top-{top_k} 요청 → 실제 {len(top_results)}건 추출 완료(컬렉션: {query_ntce.collection_name})")
    return top_results