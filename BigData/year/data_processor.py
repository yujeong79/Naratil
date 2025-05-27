"""
데이터 분류 및 ID 부여 모듈
"""
import logging
from pyspark.sql import DataFrame, Window
from pyspark.sql.functions import col, count, avg, row_number

# 로거 설정
logger = logging.getLogger(__name__)

def split_by_industry_code(df_vector: DataFrame):
    """업종 코드별 평균을 기준으로 데이터 분리 및 컬렉션별 ID 부여"""
    logger.info("📊 업종 코드별 데이터 분리 중...")

    # 공고 수 집계 및 평균 계산
    industry_count = df_vector.groupBy("industry_code").agg(count("bidNtceNo").alias("cnt"))
    avg_count = industry_count.agg(avg("cnt")).first()[0]
    logger.info(f"Industry 평균 공고 수: {avg_count:.2f}")

    # 조건 필터링
    df_vector = df_vector.join(industry_count, on="industry_code", how="left")
    no_limit_df = df_vector.filter(col("industry_code").isNull())
    etc_df = df_vector.filter((col("cnt") < avg_count) & col("industry_code").isNotNull())
    greater_df = df_vector.filter(col("cnt") >= avg_count)

    # 카테고리별 개수 출력
    no_limit_count = no_limit_df.count()
    etc_count = etc_df.count()
    greater_count = greater_df.count()

    logger.info("✅ 데이터 분리 완료:")
    logger.info(f"  - no_limit: {no_limit_count}건")
    logger.info(f"  - industry_etc: {etc_count}건")
    logger.info(f"  - industry_code별 (평균 이상): {greater_count}건")

    # no_limit ID 부여
    if no_limit_count > 0:
        window_no_limit = Window.orderBy("bidNtceNo")
        no_limit_df = no_limit_df.withColumn("bidNtceId", row_number().over(window_no_limit) - 1)
        sample = no_limit_df.select("bidNtceId", "bidNtceNo").limit(3).collect()
        logger.info("no_limit bidNtceId 샘플:")
        for row in sample:
            logger.info(f"  bidNtceId: {row['bidNtceId']}, bidNtceNo: {row['bidNtceNo']}")

    # etc ID 부여
    if etc_count > 0:
        window_etc = Window.orderBy("bidNtceNo")
        etc_df = etc_df.withColumn("bidNtceId", row_number().over(window_etc) - 1)
        sample = etc_df.select("bidNtceId", "bidNtceNo").limit(3).collect()
        logger.info("industry_etc bidNtceId 샘플:")
        for row in sample:
            logger.info(f"  bidNtceId: {row['bidNtceId']}, bidNtceNo: {row['bidNtceNo']}")

    return no_limit_df, etc_df, greater_df, industry_count

def add_sequential_id(df: DataFrame, window_spec: Window = None) -> DataFrame:
    """데이터프레임에 연속적인 ID 추가 (0부터 시작)"""
    if window_spec is None:
        window_spec = Window.orderBy("bidNtceNo")

    return df.withColumn("bidNtceId", row_number().over(window_spec) - 1)

def get_industry_dfs(greater_df: DataFrame, industry_count: DataFrame, avg_value: float):
    """업종 코드별 데이터프레임 생성"""
    result = []

    for row in industry_count.filter(col("cnt") >= avg_value).collect():
        code = row["industry_code"]
        count = row["cnt"]

        df_code = greater_df.filter(col("industry_code") == code)

        window_code = Window.orderBy("bidNtceNo")
        df_code = df_code.withColumn("bidNtceId", row_number().over(window_code) - 1)

        result.append((code, df_code, count))

    return result
