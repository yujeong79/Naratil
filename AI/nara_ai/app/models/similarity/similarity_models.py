from typing import List, Optional

from pydantic import Field
from app.models.commons import NtceBase, BaseCamelModel
from app.models.embedding import NtceVectorItem

# 유사/추천 공고 산출을 위한 공고 정보
class NtceForComparison(NtceVectorItem):
    '''
     유사/추천 공고 산출을 위한 공고 정보
    '''
    keywords: List[str] = Field(..., description="공고명 키워드: 2글자 이상 명사, 영단어 리스트", example=["키워드1", "키워드2"])
    collection_name: str = Field(..., description="컬렉션이름", example="industry_0001")

# 유사 공고 산출 조건
class SimilarNtceCondition(BaseCamelModel):
    '''
    유사 공고 산출 조건
    '''
    cosine_top_k: int = Field(..., description="코사인 유사도로 산출할 개수", example=100) # cosine 유사도 계산으로 산출할 개수
    jaccard_top_k: int = Field(..., description="자카드 유사도로 산출할 개수", example=100)# jaccard 유사도로 산출할 개수
    jaccard_weight: float = Field(..., description="자카드 유사도 가중치", example=0.3)# Jaccard의 가중치 (0 ~ jaccard_weight)
    cosine_weight: float = Field(..., description="코사인 유사도 가중치", example=0.7) # Cosine의 가중치 (-1 * cosine_weight ~ 1 * cosine_weight)
    cosine_lower_bound: float = Field(..., description="코사인 유사도 최저점", example=0.7) 
    lower_bound: float = Field(..., description="반환할 공고 점수 제한", example=0.7) # 반환할 공고 점수 제한
    final_top_k: int = Field(..., description="최종 산출할 개수", example=20) # 최종 산출할 개수


# 유사 공고 산출 정보
class SimilarNtceItem(BaseCamelModel):
    '''
    유사 공고 정보
    '''
    bid_ntce_id: int = Field(..., description="공고 ID", example=1)
    collection_name: str = Field(..., description="공고 컬렉션", example="industry_0001")
    cosine_score: Optional[float] = Field(None, description="코사인 유사도 점수", example=0.8) # 최종 산출할 개수
    jaccard_score: Optional[float] = Field(None, description="자카드 유사도 점수", example=0.4)  # 자카드 유사도 점수
    final_score: Optional[float] = Field(None, description="최종 유사도 점수", example=0.8117647)  # 최종 유사도 점수


