from typing import List

from app.models.commons import IndustryBase
from pydantic import Field

# 업종 벡터
class IndustryVectorItem(IndustryBase):
    '''
    업종 ID, 업종명, 업종 벡터
    '''
    vector: List[float] = Field(..., description="업종명 벡터", example=[0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9])


# 업종명 임베딩 실패 정보
class IndustryVectorFail(IndustryBase):
    '''
    벡터화 실패한 업종 정보
    '''
    reason: str = Field(..., description="실패 정보")