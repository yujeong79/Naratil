from pydantic import Field

from fastapi import APIRouter, Body
from typing import List, Dict

from app.core.index_manager.manager_instance import current_index_manager, past_index_manager
from app.config.logging_config import index_logger as logger
from app.models.commons import BaseCamelModel

router = APIRouter()

class AddIndexRequest(BaseCamelModel):
    collection_name: str = Field(..., description="컬렉션 이름(인덱싱키)", example="industry_etc")
    vectors: List[List[float]] = Field(..., description="FAISS에 추가할 벡터 목록")
    keywords_map: Dict[int, List[str]] = Field(..., description="키워드 목록")
    ids: List[int] = Field(..., description="ID 목록")


# 현재 공고
@router.post("/current/index")
async def add_current_index(request: AddIndexRequest):
    """
    현재 공고용 인덱스 추가
    """
    logger.info(f"[요청] 현재 공고용 인덱스 추가 요청 (업종: {request.collection_name})")
    current_index_manager.add_to_index(request.collection_name, request.keywords_map, request.vectors, request.ids)
    return {"message": f"현재 인덱스 추가 완료 (업종: {request.collection_name}, 개수: {len(request.ids)})"}

@router.post("/current/clear")
async def clear_current_index():
    """
    현재 공고용 인덱스 전체 비우기
    """
    logger.info(f"[요청] 현재 공고용 인덱스 전체 삭제 요청)")
    current_index_manager.clear()
    return {"message": "현재 인덱스 전체 삭제 완료"}

@router.get("/current/empty")
async def is_current_empty():
    """
    현재 공고용 인덱스 비어있는지 확인
    """
    empty = current_index_manager.is_empty()
    return {"empty": empty}

@router.get("/current/empty/{collection_name}")
async def is_current_empty_collection(collection_name: str):
    """
    현재 공고용 특정 인덱스 비어있는지 확인
    """
    empty = current_index_manager.is_empty_by_industry(collection_name=collection_name)
    return {"message": f"과거 인덱스(업종: {collection_name}) 인덱스 여부: {empty}"}

@router.post("/current/clear/{collection_name}")
async def clear_current_collection_index(collection_name: str):
    """
    특정 인덱스 삭제
    """
    current_index_manager.clear_index(collection_name)
    return {"message": f"현재 인덱스 삭제 완료 (업종: {collection_name})"}


# 과거 공고
@router.post("/past/index")
async def add_past_index(request: AddIndexRequest):
    """
    과거 공고용 인덱스 추가
    """
    logger.info(f"[요청] 과거 공고용 인덱스 추가 요청 (업종: {request.collection_name})")
    past_index_manager.add_to_index(request.collection_name, request.keywords_map, request.vectors, request.ids)
    return {"message": f"과거 인덱스 추가 완료 (업종: {request.collection_name}, 개수: {len(request.ids)})"}

@router.post("/past/clear")
async def clear_past_index():
    """
    과거 공고용 인덱스 전체 비우기
    """
    logger.info(f"[요청] 과거 공고용 인덱스 전체 삭제 요청)")
    past_index_manager.clear()
    return {"message": "과거 인덱스 전체 삭제 완료"}

@router.get("/past/empty")
async def is_past_empty():
    """
    과거 공고용 인덱스 비어있는지 확인
    """
    empty = past_index_manager.is_empty()
    return {"empty": empty}

@router.post("/past/clear/{collection_name}")
async def clear_past_collection_index(collection_name: str):
    """
    특정 인덱스 삭제
    """
    past_index_manager.clear_index(collection_name)
    return {"message": f"과거 인덱스 삭제 완료 (업종: {collection_name})"}


@router.get("/past/empty/{collection_name}")
async def is_past_empty_collection(collection_name: str):
    """
    과거 공고용 특정 인덱스 비어있는지 확인
    """
    empty = past_index_manager.is_empty_by_industry(collection_name=collection_name)
    return {"message": f"과거 인덱스(업종: {collection_name}) 인덱스 여부: {empty}"}
