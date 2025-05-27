from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.config.exceptions import error_handlers
from app.config.logging_config import server_logger as logger

from app.routers import index_router, embedding_router, similarity_router, recommend_router
from app.services.embedding import get_model, get_stopwords

# 서버 시작시 로딩
@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("서버 시작: 모델 및 불용어 로딩")
    get_model()
    get_stopwords()
    yield  # 앱 실행됨

# FastAPI 앱 생성
app = FastAPI(
    title="나랏일 유사 공고 API",
    description="공고 사이의 유사도를 계산하여 유사한 공고를 반환하는 API",
    version="1.0",
    lifespan=lifespan
)

# 예외 핸들러 등록
app.add_exception_handler(StarletteHTTPException, error_handlers.http_exception_handler)
app.add_exception_handler(RequestValidationError, error_handlers.validation_exception_handler)
app.add_exception_handler(Exception, error_handlers.global_exception_handler)

# 라우터 등록
base_url="/batch/fastapi"
app.include_router(embedding_router, prefix=base_url+"/embedding", tags=["Embedding"])
app.include_router(similarity_router, prefix=base_url+"/similarity", tags=["Similarity"])
app.include_router(recommend_router, prefix=base_url+"/recommend", tags=["Recommend"])
app.include_router(index_router, prefix=base_url+"/indexing", tags=["Indexing"])


# 라우팅 정보
for route in app.routes:
    logger.debug(f"라우팅 등록됨: {route.path} → {route.name}")

# 테스트
# @app.get("/")
# async def root():
#     return {"message": "나랏일 유사 공고 API 정상 실행 중"}
