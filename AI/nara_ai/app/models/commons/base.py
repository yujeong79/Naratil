from pydantic import BaseModel, Field

def to_camel_case(s: str) -> str:
    return ''.join(word.capitalize() if i else word for i, word in enumerate(s.split('_')))

class BaseCamelModel(BaseModel):
    model_config = {
        "alias_generator": to_camel_case,
        "populate_by_name": True
    }

class NtceBase(BaseCamelModel):
    '''
    공고 기본 정보 (공고 ID, 공고명)
    '''
    bid_ntce_id: int = Field(..., description="공고 ID (!= 공고번호)", example=1001)
    bid_ntce_nm: str = Field(..., description="공고명", example="공고명1")

class IndustryBase(BaseCamelModel):
    '''
    업종 기본 (업종 ID, 업종명)
    '''
    industry_id: int = Field(..., description="업종 ID (!= 업종코드)", example=55)
    industry_nm: str = Field(..., description="업종명", example="업종명1")
