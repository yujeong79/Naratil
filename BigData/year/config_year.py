"""
애플리케이션 설정 파일
"""
import os
import logging

# 로깅 설정
LOG_LEVEL = logging.INFO
LOG_FORMAT = '%(asctime)s - %(levelname)s - %(message)s'

# Spark 설정
SPARK_APP_NAME = "HDFS_to_MongoDB"
SPARK_MASTER = "local[4]"
SPARK_DRIVER_MEMORY = "8g"
SPARK_EXECUTOR_MEMORY = "4g"
SPARK_SHUFFLE_PARTITIONS = "64"
SPARK_NETWORK_TIMEOUT = "1200s"

# HDFS 설정
HDFS_PATH = "hdfs://namenode:8020/user/hadoop/data/bids/nested_bids"
COMPANY_FULL_PATH = "hdfs://namenode:8020/user/hadoop/data/bids/company_full"

# 로컬 경로 설정
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
STOPWORDS_PATH = os.path.abspath(os.path.join(BASE_DIR, "..", "data", "stopwords.csv"))
TEMP_DIR = os.path.join(BASE_DIR, "temp")

# 데이터 처리 설정
CHUNK_SIZE = 100

# MongoDB 설정
MONGODB_URI = "mongodb://j12a506.p.ssafy.io:27017/naratil"
# MONGODB_URI = "mongodb://3.36.76.231:27017/naratil"
MONGODB_DB = "naratil"
