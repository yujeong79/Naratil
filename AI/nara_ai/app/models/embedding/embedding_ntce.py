from typing import List

from app.models.commons import NtceBase
from pydantic import Field

# 공고 벡터
class NtceVectorItem(NtceBase):
    '''
    공고 ID, 공고명, 공고 벡터
    '''
    vector: List[float] = Field(..., description="공고명 벡터", example=[0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9])


# 공고명 임베딩 실패 정보
class NtceVectorFail(NtceBase):
    '''
    벡터화 실패한 공고 정보
    '''
    reason: str = Field(..., description="실패 정보", example="벡터 생성 실패")