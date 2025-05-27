
"""
일일 데이터 처리 설정 파일
"""
import os
from datetime import datetime

# 기본 경로 설정
#PROJECT_ROOT = os.path.expanduser("~/Project")
PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
HDFS_BASE_PATH = "hdfs://namenode:8020/user/hadoop/data/bids"
HDFS_WEB_URL = "http://namenode:9870"

# CSV 파일 관련 설정
CSV_BASE_PATH = os.path.join(PROJECT_ROOT, "data")
CSV_FILES = ["Final_ConstructionWork", "Final_Service", "Final_Thing"]

#MONGODB_URI = "mongodb://j12a506.p.ssafy.io:27017/naratil"
#MONGODB_URI = "mongodb://3.36.76.231:27017/naratil"
#MONGODB_DB = "naratil"

MONGO_URI = "mongodb://j12a506.p.ssafy.io:27017/naratil"
#MONGODB_URI = "mongodb://3.36.76.231:27017/naratil"
MONGO_DB_NAME = "naratil"

# 날짜 설정
TODAY = datetime.now().strftime("%Y%m%d")

# 컬렉션 이름 설정
COLLECTION_PREFIX = "industry_"
DEFAULT_COLLECTION = "industry_etc"
NO_LIMIT_COLLECTION = "industry_no_limit"

# 벡터화 설정
VECTOR_MODEL = "jhgan/ko-sbert-sts"  # 한국어 STS 모델
VECTOR_DIMENSION = 768  # ko-sbert-sts 모델의 차원
BATCH_SIZE = 100

# 불용어 리스트 경로
STOPWORDS_PATH = os.path.join(PROJECT_ROOT, "data", "stopwords.csv")

def get_today_data_path():
    """오늘 날짜 데이터 경로 반환"""
    return f"{HDFS_BASE_PATH}/{TODAY}"

def get_hdfs_web_url():
    return HDFS_WEB_URL














