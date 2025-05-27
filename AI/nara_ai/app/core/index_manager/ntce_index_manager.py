import faiss
import numpy as np
from typing import Dict, List

from app.core.similarity.score_utils import normalize_vectors
from app.config.logging_config import index_logger as logger


class NtceIndexManager:
    """
    현재 공고용 인덱스 매니저
    """

    def __init__(self, dimension: int = 768):
        self.keywords_map: Dict[str, Dict[int, List[str]]] = {}
        self.index_map: Dict[str, faiss.IndexIDMap] = {}
        self.dimension:int = dimension

    def create_index(self, key: str):
        """
        업종 코드별 인덱스 생성
        """
        self.keywords_map[key] = {}

        index = faiss.IndexFlatL2(self.dimension)
        self.index_map[key] = faiss.IndexIDMap(index)

        logger.info(f"[인덱싱] 공고 인덱스 생성 완료: {key}")

    def add_to_index(self, collection_name: str, keywords_map: Dict[int, List[str]], vectors: List[List[float]], ids: List[int]):
        """
        기존 인덱스가 있으면 추가(add_with_ids) - bulk insert, 없으면 새로 생성
        """
        # FAISS 인덱스 추가
        if collection_name not in self.index_map:
            self.create_index(collection_name)

        np_vectors = np.array(vectors, dtype=np.float32)
        np_vectors = normalize_vectors(np_vectors)

        np_ids = np.array(ids, dtype=np.int64)

        self.index_map[collection_name].add_with_ids(np_vectors, np_ids)
        
        # keywords 추가
        self.keywords_map[collection_name].update(keywords_map)
        
        logger.info(f"[인덱싱] 공고 인덱스 추가 완료 - 업종: {collection_name}, 추가 건수: {len(ids)}")

    def search_faiss(self, collection_name: str, query_vector: List[float], top_k: int):
        """
        인덱스에서 검색
        """
        if collection_name not in self.index_map:
            logger.warning(f"[인덱싱] 검색 실패 - 공고 인덱스 없음: {collection_name}")
            return [], []

        index = self.index_map[collection_name]

        query_vector = np.array(query_vector, dtype=np.float32)
        query_vector = np.expand_dims(query_vector, axis=0)
        query_vector = normalize_vectors(query_vector)

        distances, indices = index.search(query_vector, top_k)
        return distances[0], indices[0]

    def clear(self):
        """
        모든 현재 공고 인덱스 초기화
        """
        self.keywords_map.clear()
        self.index_map.clear()
        logger.info("[인덱싱] 공고 인덱스 전체 삭제 완료")

    def clear_index(self, collection_name: str):
        """
        특정 현재 공고 인덱스 초기화
        """
        if collection_name in self.keywords_map:
            del self.keywords_map[collection_name]
        if collection_name in self.index_map:
            del self.index_map[collection_name]

    def has_index(self, key: str) -> bool:
        """
        특정 업종코드 인덱스 존재 여부
        """
        return key in self.index_map

    def is_empty_by_industry(self, collection_name: str) -> bool:
        """
        특정 업종 인덱스 비어있는지 확인
        """
        return (collection_name not in self.keywords_map) and (collection_name not in self.index_map)

    def is_empty(self) -> bool:
        """
        전체 인덱스가 비어있는지 확인
        """
        return (len(self.keywords_map) == 0 and len(self.index_map) == 0)
