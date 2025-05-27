"""
HDFS에서 데이터를 로드하는 모듈
"""
import logging
import pandas as pd
from pyspark.sql import SparkSession
from . import config_year as config  # ← 수정된 부분

# 로거 설정
logger = logging.getLogger(__name__)

def create_spark_session():
    """Spark 세션 생성 - 메모리 설정 최적화"""
    spark = SparkSession.builder \
        .appName(config.SPARK_APP_NAME) \
        .master(config.SPARK_MASTER) \
        .config("spark.driver.memory", config.SPARK_DRIVER_MEMORY) \
        .config("spark.executor.memory", config.SPARK_EXECUTOR_MEMORY) \
        .config("spark.sql.shuffle.partitions", config.SPARK_SHUFFLE_PARTITIONS) \
        .config("spark.network.timeout", config.SPARK_NETWORK_TIMEOUT) \
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("ERROR")
    logger.info("✅ Spark 세션 생성 완료")
    return spark

def load_stopwords(path):
    """불용어 사전 로드"""
    try:
        stopwords = set(pd.read_csv(path)["stopword"].tolist())
        logger.info(f"✅ 불용어 {len(stopwords)}개 로드 완료")
        return stopwords
    except Exception as e:
        logger.error(f"❌ 불용어 로드 실패: {e}")
        return set()

def load_data_from_hdfs(spark):
    """HDFS에서 parquet 데이터 로드"""
    try:
        logger.info(f"📦 HDFS에서 데이터 로딩 중: {config.HDFS_PATH}")
        df = spark.read.parquet(config.HDFS_PATH)
        logger.info(f"✅ 데이터 로딩 완료: {df.count()} 행")
        
        # 샘플 데이터 확인
        logger.info("🔍 데이터 샘플:")
        df.select("bidNtceNo", "bidNtceNm", "industry_code").show(2, truncate=False)
        
        return df
    except Exception as e:
        logger.error(f"❌ 데이터 로드 실패: {e}")
        raise
