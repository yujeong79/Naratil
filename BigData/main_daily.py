# 자정에 실행.
"""
메인 실행 파일

processAll: csv 원본 -> 정제(parsing, vectorization, json) -> MongoDB 클라우드 Upload (atlas)
1. processAll 안에서 CSV -> HDFS 적재 (bidprcAmt의 첫 번째 값을 recentprc로 저장)
2. HDFS -> 벡터화 -> 데이터 분류 -> MongoDB 저장
"""
import logging
import time
from datetime import datetime

import config_daily as config
from data_loader import load_todays_data, create_spark_session
from vectorizer import vectorize_data
from data_processor import process_data
from mongodb_sender import save_to_mongodb
from csvTohdfs import process_csv_files

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def process_all():
    """
    전체 프로세스 실행:
    1. CSV -> HDFS 적재 (company_full 데이터 처리 포함)
    2. HDFS -> 벡터화 -> 처리 -> MongoDB
    """
    start_time = time.time()
    logger.info(f"🚀 전체 데이터 처리 시작")
    
    try:
        # 1. CSV -> HDFS 적재 (bidprcAmt의 첫 번째 값을 recentprc로 저장)
        logger.info("1️⃣ CSV -> HDFS 적재 시작")
        csv_success = process_csv_files()
        if not csv_success:
            logger.error("❌ CSV -> HDFS 적재에 실패했습니다. 처리를 중단합니다.")
            return
        
        # 2. HDFS 데이터 로드 및 처리
        spark = create_spark_session("Daily Bid Data Processing")
        
        try:
            # 2.1 데이터 로드 (이미 company_full 정보가 병합된 데이터)
            logger.info("2️⃣ HDFS 데이터 로드")
            df = load_todays_data(spark)
            
            # 2.2 벡터화 (불용어 처리 포함)
            logger.info("3️⃣ 텍스트 벡터화")
            df_vector = vectorize_data(spark, df)
            
            # 2.3 데이터 처리 (업종 코드별 분류 및 ID 부여)
            logger.info("4️⃣ 데이터 처리 및 컬렉션 결정")
            no_limit_df, etc_df, industry_dfs = process_data(spark, df_vector)
            
            # 2.4 MongoDB 저장
            logger.info("5️⃣ MongoDB 저장")
            save_to_mongodb(no_limit_df, etc_df, industry_dfs)
            
            elapsed_time = time.time() - start_time
            logger.info(f"✅ 전체 데이터 처리 완료 (소요 시간: {elapsed_time:.2f}초)")
            
        finally:
            # Spark 세션 종료
            if spark:
                spark.stop()
    
    except Exception as e:
        logger.error(f"❌ 처리 중 오류 발생: {e}")
        import traceback
        logger.error(traceback.format_exc())

def main():
    """메인 함수"""
    process_all()

if __name__ == "__main__":
    main()
