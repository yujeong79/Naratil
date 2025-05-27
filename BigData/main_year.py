"""
HDFS 데이터 벡터화 및 MongoDB 전송 메인 스크립트


import time
import logging
import traceback
import os
from pyspark.sql.functions import col, avg

# 설정 및 모듈 import
import config
from modules import (
    create_spark_session, 
    load_data_from_hdfs, 
    load_stopwords,
    vectorize_data,
    split_by_industry_code,
    get_industry_dfs,
    send_to_mongodb,
    init_mongodb,
    handle_company_full
)

# 로깅 설정
logging.basicConfig(
    level=config.LOG_LEVEL,
    format=config.LOG_FORMAT
)
logger = logging.getLogger(__name__)
"""
import time
import logging
import traceback
import os
from pyspark.sql.functions import col, avg

# 설정 및 모듈 import
import year.config_year as config  # ← 수정된 부분
from year import (
    create_spark_session, 
    load_data_from_hdfs, 
    load_stopwords,
    vectorize_data,
    split_by_industry_code,
    get_industry_dfs,
    send_to_mongodb,
    init_mongodb,
    handle_company_full
)

# 로깅 설정
logging.basicConfig(
    level=config.LOG_LEVEL,
    format=config.LOG_FORMAT
)
logger = logging.getLogger(__name__)

def main():
    """메인 실행 함수"""
    start_time = time.time()
    logger.info("🚀 HDFS 데이터 벡터화 및 MongoDB 전송 시작...")
    
    # 임시 디렉토리 생성
    os.makedirs(config.TEMP_DIR, exist_ok=True)
    
    try:
        # 1. MongoDB 연결 초기화
        if not init_mongodb():
            logger.error("MongoDB 연결 실패. 프로그램을 종료합니다.")
            return
        
        # 2. Spark 세션 생성
        spark = create_spark_session()
        
        # 3. 불용어 로드
        stopwords = load_stopwords(config.STOPWORDS_PATH)
        
        # 4. HDFS에서 데이터 로드
        df = load_data_from_hdfs(spark)
        
        # 5. 데이터 벡터화
        df_vector = vectorize_data(spark, df, stopwords)
        
        # 6. 업종 코드별 데이터 분리
        no_limit_df, etc_df, greater_df, industry_count = split_by_industry_code(df_vector)
        
        # 7. MongoDB로 데이터 전송
        
        # 7.0 company
        #handle_company_full(spark)
        
        #######
        # 7.3 업종별 개별 컬렉션 전송 (4989만 먼저 처리)
        greater_success = 0
        avg_value = industry_count.agg(avg("cnt")).first()[0]

        industry_dfs = get_industry_dfs(greater_df, industry_count, avg_value)
        logger.info(f"📦 업종 코드 each 처리")

        # 4989 업종만 먼저 찾아서 처리
        found_4989 = False
        target_code = 4989
        for code, df_code, count in industry_dfs:
             if str(code) == str(target_code):
                logger.info(f"📦 업종 코드 {code} 먼저 처리 중 (총 {count}건)...")
                logger.info(f"  (업종 코드 타입: {type(code)})")
                
                sample = df_code.select("bidNtceId", "bidNtceNo").limit(3).collect()
                logger.info(f"industry_{code} bidNtceId 샘플:")
                for row in sample:
                    logger.info(f"  bidNtceId: {row['bidNtceId']}, bidNtceNo: {row['bidNtceNo']}")
                # MongoDB로 전송
                code_success = send_to_mongodb(df_code, f"industry_{code}")
                greater_success += code_success
                found_4989 = True
                break  # 4989 처리 후 루프 종료

        # 4989를 찾지 못했을 경우 로그 출력
        if not found_4989:
            logger.warning("⚠️ 업종 코드 4989를 찾을 수 없습니다.")

        # 7.2 industry_etc 컬렉션 전송
        etc_success = send_to_mongodb(etc_df, "industry_etc")

# 나머지 업종 코드는 처리하지 않음
        
        
        # 7.1 no_limit 컬렉션 전송
        #no_limit_success = send_to_mongodb(no_limit_df, "industry_no_limit")
        total_success = etc_success + greater_success
        total_count = etc_df.count() + greater_df.count()
        
        logger.info("🎉 전체 프로세스 완료!")
        logger.info(f"  - 총 처리 건수: {total_count}건")
        logger.info(f"  - 전송 성공 건수: {total_success}건 ({total_success/total_count*100:.2f}%)")
        logger.info(f"  - 총 소요 시간: {(time.time() - start_time):.2f}초")
        
    except Exception as e:
        logger.error(f"❌ 처리 중 오류 발생: {e}")
        logger.error(traceback.format_exc())
    finally:
        # Spark 세션 종료
        if 'spark' in locals():
            spark.stop()
            logger.info("Spark 세션 종료")

if __name__ == "__main__":
    main()