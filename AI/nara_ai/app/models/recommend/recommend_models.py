from app.models.commons import BaseCamelModel
from pydantic import Field

# 추천 공고 후보
class RecommendedNtceScore(BaseCamelModel):
    '''
    추천 공고 정보
    '''
    bid_ntce_id: int = Field(..., description="공고 ID", example=1) # 현재 공고
    total_score: float = Field(..., description="각 점수 합계", example=4.5)
    avg_score:  float = Field(..., description="점수 평균", example=0.9)
    adjust_score: float = Field(..., description="조정 점수(최종)",  example=0.92)
    count: int = Field(..., description="해당 공고가 리스트에 포함된 횟수", example=5)


