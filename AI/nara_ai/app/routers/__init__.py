from .embedding_router import router as embedding_router
from .similarity_router import router as similarity_router
from .recommend_router import router as recommend_router
from .index_router import router as index_router

__all__ = [
    "embedding_router",
    "similarity_router",
    "recommend_router",
    "index_router"
]