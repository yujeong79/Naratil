from typing import Optional


def normalize_score(score: float, min_score: float, max_score: float) -> float:
    """
    최종 점수를 0~1 사이로 정규화
    """
    if max_score == min_score:
        return 0.0
    return (score - min_score) / (max_score - min_score)


def compute_final_score(
    cosine_score: Optional[float],
    jaccard_score: Optional[float],
    cosine_weight: float,
    jaccard_weight: float,
    min_score: float,
    max_score: float
) -> float:
    """
    가중치를 적용한 최종 스코어 계산 후 정규화 반환

    :param cosine_score: 코사인 유사도 점수 (-1 ~ 1)
    :param jaccard_score: 자카드 유사도 점수 (0 ~ 1)
    :param cosine_weight: 코사인 유사도 가중치
    :param jaccard_weight: 자카드 유사도 가중치
    :param min_score: 정규화 전 점수 범위 하한
    :param max_score: 정규화 전 점수 범위 상한
    :return: 0~1 정규화된 최종 점수
    """
    cosine_score = cosine_score or 0.0
    jaccard_score = jaccard_score or 0.0

    raw_score = (cosine_score * cosine_weight) + (jaccard_score * jaccard_weight)
    return normalize_score(raw_score, min_score, max_score)
