"""
MongoDB 수신용 FastAPI 서버 (EC2-1에 배포)
"""
from fastapi import FastAPI, HTTPException, Depends, Security, Request
from fastapi.security.api_key import APIKeyHeader
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import logging
import time
import os
from typing import List, Dict, Any
from pydantic import BaseModel
from pymongo import MongoClient

# 로그 디렉토리 생성
os.makedirs("logs", exist_ok=True)

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("logs/mongodb_api.log"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# API 키 설정
API_KEY = "your-secure-api-key"  # 실제 배포 시 환경 변수로 관리
api_key_header = APIKeyHeader(name="X-API-Key")

# MongoDB 연결 설정
MONGO_HOST = "localhost"
MONGO_PORT = 27017
MONGO_DB = "bidding_db"
MONGO_USER = ""  # 인증 필요한 경우 설정
MONGO_PASS = ""  # 인증 필요한 경우 설정

# MongoDB 클라이언트 초기화
try:
    if MONGO_USER and MONGO_PASS:
        mongo_uri = f"mongodb://{MONGO_USER}:{MONGO_PASS}@{MONGO_HOST}:{MONGO_PORT}/{MONGO_DB}"
    else:
        mongo_uri = f"mongodb://{MONGO_HOST}:{MONGO_PORT}/{MONGO_DB}"

    client = MongoClient(mongo_uri)
    # 연결 테스트
    client.admin.command('ping')
    logger.info("MongoDB 연결 성공")
    db = client[MONGO_DB]
except Exception as e:
    logger.error(f"MongoDB 연결 실패: {str(e)}")
    raise

# FastAPI 앱 생성
app = FastAPI(
    title="공고 데이터 API", 
    description="입찰 공고 데이터를 MongoDB에 저장하는 API",
    version="1.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 보안을 위해 실제 도메인으로 제한 권장
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API 키 인증 함수
def get_api_key(api_key: str = Security(api_key_header)):
    if api_key != API_KEY:
        logger.warning(f"잘못된 API 키 시도: {api_key[:5]}...")
        raise HTTPException(status_code=403, detail="인증 실패: 잘못된 API 키")
    return api_key

# 요청 데이터 모델
class BiddingData(BaseModel):
    data: List[Dict[str, Any]]

# 루트 엔드포인트
@app.get("/")
async def root():
    return {
        "message": "공고 데이터 API가 실행 중입니다.",
        "version": "1.0.0",
        "endpoints": [
            "/api/data/{collection_name} - 데이터 저장",
            "/api/collections - 컬렉션 목록 조회",
            "/api/health - 시스템 상태 확인"
        ]
    }

# 데이터 저장 엔드포인트
@app.post("/api/data/{collection_name}")
async def store_data(
    collection_name: str, 
    data: BiddingData, 
    request: Request,
    api_key: str = Depends(get_api_key)
):
    start_time = time.time()
    try:
        # 컬렉션 선택 (없으면 자동 생성)
        collection = db[collection_name]
        
        # 입력 데이터 검증
        if not data.data:
            raise HTTPException(status_code=400, detail="데이터가 비어 있습니다")
        
        # 데이터 count
        data_count = len(data.data)
        logger.info(f"컬렉션 {collection_name}에 {data_count}건 저장 시작")
        
        # bidNtceId 필드가 있는지 확인하고 로깅
        if data_count > 0 and 'bidNtceId' in data.data[0]:
            first_id = data.data[0]['bidNtceId']
            last_id = data.data[-1]['bidNtceId']
            logger.info(f"bidNtceId 범위: {first_id} ~ {last_id}")
        
        # MongoDB에 데이터 저장
        result = collection.insert_many(data.data)
        inserted_count = len(result.inserted_ids)
        
        # 로그 기록
        elapsed_time = time.time() - start_time
        logger.info(f"컬렉션 {collection_name}에 {inserted_count}건 저장 완료 (소요 시간: {elapsed_time:.2f}초)")
        
        return {
            "status": "success",
            "inserted_count": inserted_count,
            "collection": collection_name,
            "elapsed_time_ms": round(elapsed_time * 1000, 2)
        }
    except Exception as e:
        error_msg = f"데이터 저장 중 오류 발생: {str(e)}"
        logger.error(error_msg)
        raise HTTPException(status_code=500, detail=error_msg)

# 컬렉션 정보 조회
@app.get("/api/collections")
async def get_collections(api_key: str = Depends(get_api_key)):
    try:
        collections = db.list_collection_names()
        result = []
        
        for coll_name in collections:
            count = db[coll_name].count_documents({})
            
            # bidNtceId 범위 확인
            id_min = None
            id_max = None
            if count > 0:
                try:
                    id_min = db[coll_name].find().sort("bidNtceId", 1).limit(1)[0].get("bidNtceId")
                    id_max = db[coll_name].find().sort("bidNtceId", -1).limit(1)[0].get("bidNtceId")
                except:
                    pass  # bidNtceId가 없거나 오류 발생 시 무시
                
            result.append({
                "name": coll_name,
                "document_count": count,
                "bidNtceId_min": id_min,
                "bidNtceId_max": id_max
            })
        
        return {
            "status": "success",
            "collections": result
        }
    except Exception as e:
        error_msg = f"컬렉션 정보 조회 중 오류 발생: {str(e)}"
        logger.error(error_msg)
        raise HTTPException(status_code=500, detail=error_msg)

# API 상태 모니터링
@app.get("/api/health")
async def health_check():
    try:
        # MongoDB 연결 확인
        client.admin.command('ping')
        return {
            "status": "healthy", 
            "database_connection": "ok",
            "timestamp": time.time(),
            "server_info": {
                "host": MONGO_HOST,
                "port": MONGO_PORT,
                "database": MONGO_DB
            }
        }
    except Exception as e:
        logger.error(f"상태 확인 실패: {str(e)}")
        raise HTTPException(status_code=500, detail=f"데이터베이스 연결 실패: {str(e)}")

# 메인 실행
if __name__ == "__main__":
    logger.info("MongoDB API 서버 시작")
    # 워커 수를 4로 설정하여 대용량 요청 처리
    uvicorn.run("app:app", host="0.0.0.0", port=8000, workers=4)