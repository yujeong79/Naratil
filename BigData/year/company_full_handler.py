import logging
from .config_year import COMPANY_FULL_PATH
from .mongodb_sender import send_to_mongodb

def handle_company_full(spark):
    """company_full 데이터 로드 및 MongoDB 전송 처리"""
    logger = logging.getLogger(__name__)

    # ✅ 여기서 직접 로드
    logger.info(f"🏢 company_full 데이터 로드 중... ({COMPANY_FULL_PATH})")
    company_df = spark.read.parquet(COMPANY_FULL_PATH)

    # 샘플 확인
    logger.info("📋 company_full 샘플:")
    sample = company_df.select("prcbdrNm", "prcbdrBizno").limit(3).collect()
    for row in sample:
        logger.info(f"  업체명: {row['prcbdrNm']}, 사업자번호: {row['prcbdrBizno']}")

    # MongoDB 전송
    logger.info("📤 company_full 데이터를 MongoDB로 전송 중...")
    company_success = send_to_mongodb(company_df, "company_full")
    logger.info(f"✅ company_full 전송 완료: {company_success}건")
