# ~/Project/daily/data_loader.py
"""
HDFS에서 데이터 로드 모듈
"""
import logging
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, size
from hdfs import InsecureClient
import config_daily as config
from urllib.parse import urlparse

# 로거 설정
logger = logging.getLogger(__name__)


def create_spark_session(app_name="Bid Data Loader"):
    """Spark 세션 생성"""
    spark = SparkSession.builder \
        .appName(app_name) \
        .master("local[4]") \
        .config("spark.driver.memory", "8g") \
        .config("spark.executor.memory", "4g") \
        .config("spark.hadoop.fs.defaultFS", "hdfs://namenode:8020") \
        .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    return spark


def get_hdfs_client():
    """WebHDFS 클라이언트 생성"""
    return InsecureClient("http://namenode:9870")  # 포트는 환경에 맞게 조정


# def check_hdfs_path(path):
#     """WebHDFS를 이용해 경로 존재 여부 확인"""
#     logger.info(f"🔍 WebHDFS로 경로 확인 중: {path}")
#     try:
#         client = get_hdfs_client()
#         exists = client.status(path, strict=False) is not None
#         logger.info(f"  - 결과: {'있음' if exists else '없음'}")
#         return exists
#     except Exception as e:
#         logger.error(f"❌ WebHDFS 경로 확인 실패: {e}")
#         return False


def check_hdfs_path(path):
    logger.info(f"🔍 WebHDFS로 경로 확인 중: {path}")
    try:
        # hdfs://namenode:8020/user/hadoop/data/bids/20250407 → /user/hadoop/data/bids/20250407
        parsed_path = urlparse(path).path
        client = get_hdfs_client()
        exists = client.status(parsed_path, strict=False) is not None
        logger.info(f"  - 결과: {'있음' if exists else '없음'}")
        return exists
    except Exception as e:
        logger.error(f"❌ WebHDFS 경로 확인 실패: {e}")
        return False



def load_todays_data(spark):
    """HDFS에서 오늘 날짜 데이터 로드"""
    today_path = config.get_today_data_path()
    logger.info(f"🔄 오늘 데이터 로딩 중: {today_path}")

    if not check_hdfs_path(today_path):
        logger.error(f"❌ HDFS에 오늘 날짜 경로가 없습니다: {today_path}")
        return None

    try:
        # Parquet 파일 로드
        df = spark.read.parquet(today_path)
        count = df.count()
        logger.info(f"✅ 데이터 로딩 완료: {count}개 레코드")

        # 유효성 검사
        non_empty_bizs = df.filter(size(col("bizs_parsed")) > 0).count()
        logger.info(f"📊 유효한 bizs_parsed: {non_empty_bizs}/{count} ({non_empty_bizs / count * 100:.2f}%)")

        # 샘플 recentprc 확인
        recentprc_sample = df.select(
            "bidNtceNo",
            col("bizs_parsed")[0]["prcbdrBizno"].alias("prcbdrBizno"),
            col("bizs_parsed")[0]["recentprc"].alias("recentprc")
        ).limit(3)

        logger.info("🔍 recentprc 샘플 (bidprcAmt의 첫 번째 값):")
        recentprc_sample.show(truncate=False)

        return df

    except Exception as e:
        logger.error(f"❌ 데이터 로딩 오류: {e}")
        import traceback
        logger.error(traceback.format_exc())
        raise
