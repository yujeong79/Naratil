import numpy as np

# 벡터 정규화
def normalize_vectors(vectors: np.ndarray) -> np.ndarray:
    # 벡터를 정규화하여 크기를 1로 만듬
    norms = np.linalg.norm(vectors, axis=1, keepdims=True)
    # 분모가 0인 경우, 해당 벡터는 0으로 처리 (제로 벡터가 있을 경우)
    norms[norms == 0] = 1  # 제로 벡터를 정규화할 때 분모를 1로 처리하여 0 벡터가 되지 않도록 함
    return vectors / norms

# 최종 스코어 정규화 함수
def normalize_score(score, min_score, max_score):
    return (score - min_score) / (max_score - min_score)


# 최종 스코어 산출
def compute_final_score(cosine_score: float, jaccard_score: float,
                        cosine_weight: float, jaccard_weight: float,
                        min_score: float, max_score: float) -> float:
    raw = cosine_score * cosine_weight + jaccard_score * jaccard_weight
    return normalize_score(score=raw, min_score=min_score, max_score=max_score)
