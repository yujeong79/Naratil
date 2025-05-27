# ~/Project/daily/csvTohdfs.py
"""
CSV -> HDFS 적재 모듈
CSV 파일을 처리하고 company_full 데이터와 병합하여 HDFS에 저장
"""
import logging
import os
import subprocess
from pyspark.sql import SparkSession
from pyspark.sql.functions import regexp_extract, col, lit, udf, size, explode, struct, collect_list
from pyspark.sql.types import ArrayType, StructType, StructField, StringType
from pymongo import MongoClient
import config_daily as config
import pandas as pd
from hdfs import InsecureClient
from urllib.parse import urlparse

# 로거 설정
logger = logging.getLogger(__name__)

# bizs_parsed 스키마 정의
bizs_parsed_schema = ArrayType(StructType([
    StructField("opengRank", StringType()),
    StructField("prcbdrBizno", StringType()),
    StructField("prcbdrNm", StringType()),
    StructField("bidprcAmt", StringType()),
    StructField("bidprcrt", StringType()),
    StructField("rmrk", StringType()),
    StructField("emplyeNum", StringType()),
    StructField("indstrytyCd", StringType()),
    StructField("recentprc", StringType())
]))

@udf(bizs_parsed_schema)
def parse_bizs_column(bizs_series):
    """
    bizs 컬럼을 파싱하는 UDF 함수
    Row(...) 구조를 파싱하고 따옴표 내 괄호나 특수문자도 올바르게 처리
    """
    if not bizs_series:
        return []
    
    result = []
    
    try:
        bizs_series = bizs_series.strip()
        
        # Row 찾기 - 중첩된 괄호 처리를 위한 개선
        row_patterns = []
        paren_depth = 0
        quote_mode = False
        quote_char = None
        start_idx = -1
        
        # 첫 단계: Row 시작과 끝 위치 찾기
        for i, char in enumerate(bizs_series):
            # 따옴표 모드 처리
            if char in ["'", '"'] and (i == 0 or bizs_series[i-1] != '\\'):
                if not quote_mode:
                    quote_mode = True
                    quote_char = char
                elif char == quote_char:
                    quote_mode = False
                    quote_char = None
            
            # Row 시작 감지
            if not quote_mode and bizs_series[i:i+4] == 'Row(' and (i == 0 or bizs_series[i-1] not in 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_'):
                start_idx = i
                paren_depth = 0
            
            # 괄호 깊이 추적
            if not quote_mode:
                if char == '(' and start_idx != -1:
                    paren_depth += 1
                elif char == ')' and start_idx != -1:
                    paren_depth -= 1
                    if paren_depth == 0:  # Row(...) 완료
                        row_patterns.append((start_idx, i))
                        start_idx = -1
        
        # 두 번째 단계: 각 Row 패턴 파싱
        for start_idx, end_idx in row_patterns:
            row_content = bizs_series[start_idx+4:end_idx]  # 'Row(' 제외하고 시작
            
            # 필드 파싱을 위한 상태 변수
            fields = {}
            field_name = ""
            field_value = ""
            parsing_name = True
            in_quotes = False
            quote_char = None
            escaped = False
            
            i = 0
            while i < len(row_content):
                char = row_content[i]
                
                # 이스케이프 문자 처리
                if escaped:
                    field_value += char
                    escaped = False
                    i += 1
                    continue
                
                if char == '\\':
                    escaped = True
                    i += 1
                    continue
                
                # 따옴표 모드 전환
                if char in ["'", '"'] and not in_quotes:
                    in_quotes = True
                    quote_char = char
                    i += 1
                    continue
                elif char == quote_char and in_quotes:
                    in_quotes = False
                    i += 1
                    continue
                
                # 필드 이름-값 구분
                if char == '=' and parsing_name and not in_quotes:
                    field_name = field_name.strip()
                    parsing_name = False
                    i += 1
                    continue
                
                # 필드 종료 (콤마)
                if char == ',' and not in_quotes and not parsing_name:
                    fields[field_name] = field_value.strip()
                    field_name = ""
                    field_value = ""
                    parsing_name = True
                    i += 1
                    continue
                
                # 일반 문자 추가
                if parsing_name:
                    field_name += char
                else:
                    field_value += char
                
                i += 1
            
            # 마지막 필드 처리
            if field_name and not parsing_name:
                fields[field_name] = field_value.strip()
            
            # 값 정리 - 따옴표 제거
            for key in fields:
                value = fields[key]
                if (value.startswith("'") and value.endswith("'")) or (value.startswith('"') and value.endswith('"')):
                    fields[key] = value[1:-1]
            
            # Row 데이터 추가
            result.append({
                "opengRank": fields.get("opengRank", ""),
                "prcbdrBizno": fields.get("prcbdrBizno", ""),
                "prcbdrNm": fields.get("prcbdrNm", ""),
                "bidprcAmt": fields.get("bidprcAmt", ""),
                "bidprcrt": fields.get("bidprcrt", ""),
                "rmrk": fields.get("rmrk", ""),
                "emplyeNum": "",  # 기본값
                "indstrytyCd": "",  # 기본값
                "recentprc": ""  # 기본값 - 나중에 bidprcAmt의 첫 번째 값으로 대체
            })
    except Exception as e:
        logger.error(f"❌ 파싱 오류: {e}")
        return []
    
    return result

def create_spark_session():
    spark = SparkSession.builder \
        .appName("CSV to HDFS") \
        .master("local[4]") \
        .config("spark.driver.memory", "8g") \
        .config("spark.executor.memory", "4g") \
        .config("spark.hadoop.fs.defaultFS", "hdfs://namenode:8020")\
        .config("spark.sql.shuffle.partitions", "64") \
        .config("spark.network.timeout", "1200s") \
        .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    return spark

def get_hdfs_client():
    return InsecureClient(config.get_hdfs_web_url())

# def check_hdfs_path(path, spark):
#     logger.info(f"🔍 HDFS 경로 확인 중 (Spark 방식): {path}")
#     try:
#         fs = spark._jvm.org.apache.hadoop.fs.FileSystem.get(spark._jsc.hadoopConfiguration())
#         exists = fs.exists(spark._jvm.org.apache.hadoop.fs.Path(path))
#         logger.info(f"  - 결과: {'있음' if exists else '없음'}")
#         return exists
#     except Exception as e:
#         logger.error(f"❌ Spark 기반 경로 체크 실패: {e}")
#         return False
def check_hdfs_path(path):
    #전달 : "hdfs://namenode:8020/user/hadoop/data/bids/20250408"
    logger.info(f"🔍 HDFS 경로 확인 중 (WebHDFS 방식): {path}")
    try:
        parsed_path = urlparse(path).path  # 결과: /user/hadoop/data/bids/20250408
        client = get_hdfs_client() #http://namenode:9870 에서 확인.
        exists = client.status(parsed_path, strict=False) is not None
        #exists = client.status(path, strict=False) is not None
        logger.info(f"  - 결과: {'있음' if exists else '없음'}")
        return exists
    except Exception as e:
        logger.error(f"❌ WebHDFS 기반 경로 체크 실패: {e}")
        return False



def load_company_full_from_mongodb(spark):
    """MongoDB에서 company_full 데이터 로드 (recentprc 포함)"""
    try:
        logger.info("📦 MongoDB에서 company_full 데이터 로드 중...")

        client = MongoClient(config.MONGO_URI)
        db = client[config.MONGO_DB_NAME]
        
        if "company_full" not in db.list_collection_names():
            raise ValueError("❌ 'company_full' 컬렉션이 존재하지 않습니다.")
        
        # MongoDB → 리스트로 로드
        company_list = list(db["company_full"].find())
        if not company_list:
            raise ValueError("❌ company_full 컬렉션에 데이터가 없습니다.")
        
        client.close()

        # ❗ _id 컬럼을 문자열로 변환
        for doc in company_list:
            if "_id" in doc:
                doc["_id"] = str(doc["_id"])

        # Pandas → Spark 변환
        pandas_df = pd.DataFrame(company_list)
        company_df = spark.createDataFrame(pandas_df)

        logger.info(f"✅ MongoDB → Spark DataFrame 로딩 완료: {company_df.count()} rows")

        # recentprc 추출용 UDF 등록
        @udf(StringType())
        def extract_first_bidprc(bidprc_value):
            if bidprc_value is None:
                return ""
            try:
                if isinstance(bidprc_value, str) and bidprc_value.startswith("[") and bidprc_value.endswith("]"):
                    items = bidprc_value.strip("[]").replace("'", "").replace('"', "").split(", ")
                    return items[0].strip() if items and items[0] else ""
                elif isinstance(bidprc_value, list) and len(bidprc_value) > 0:
                    return str(bidprc_value[0])
                return ""
            except Exception as e:
                logger.error(f"❌ bidprcAmt 파싱 오류: {e} - 값: {bidprc_value}")
                return ""

        # recentprc 컬럼 추가
        if "bidprcAmt" in company_df.columns:
            company_df = company_df.withColumn("recentprc", extract_first_bidprc(col("bidprcAmt")))
            sample_result = company_df.filter(col("bidprcAmt").isNotNull()).limit(5)
            logger.info("🔍 recentprc 추출 샘플:")
            sample_result.select("prcbdrBizno", "bidprcAmt", "recentprc").show(truncate=False)
        else:
            logger.warning("⚠️ bidprcAmt 컬럼이 없어 recentprc는 빈 값으로 설정됩니다.")
            company_df = company_df.withColumn("recentprc", lit(""))

        return company_df

    except Exception as e:
        logger.error(f"❌ company_full 데이터 로드 실패: {e}")
        raise


def enrich_bizs_parsed_with_company_info(spark, df, company_df):
    """bizs_parsed에 기업 정보 병합"""
    logger.info("🔄 bizs_parsed에 company_full 데이터에서 기업 정보 병합 중...")
    
    # company_df에서 필요한 컬럼만 선택
    company_info = company_df.select(
        col("prcbdrBizno"),
        col("emplyeNum"),
        col("indstrytyCd"),
        col("recentprc")  # 이미 extract_first_bidprc UDF로 처리된 recentprc 컬럼 (첫 번째 값)
    )
    
    # 선택된 컬럼 확인
    logger.info("📋 company_info에서 선택된 컬럼:")
    for field in company_info.schema.fields:
        logger.info(f"  - {field.name}: {field.dataType}")
    
    # 샘플 데이터 확인
    sample_companies = company_info.limit(3)
    logger.info("\n🔍 company_info 샘플 데이터:")
    sample_companies.show(truncate=False)
    
    # 데이터프레임을 테이블로 등록
    df.createOrReplaceTempView("bids")
    company_info.createOrReplaceTempView("companies")
    
    # SQL을 사용하여 기업 정보를 bizs_parsed에 병합
    enriched_df = spark.sql("""
    WITH exploded_bizs AS (
        SELECT 
            a.bidNtceNo,
            a.process_date,
            posexplode(a.bizs_parsed) AS (pos, biz)
        FROM bids a
    ),
    enriched_bizs AS (
        SELECT 
            b.bidNtceNo,
            b.process_date,
            b.pos,
            struct(
                b.biz.opengRank AS opengRank,
                b.biz.prcbdrBizno AS prcbdrBizno,
                b.biz.prcbdrNm AS prcbdrNm,
                b.biz.bidprcAmt AS bidprcAmt,
                b.biz.bidprcrt AS bidprcrt,
                b.biz.rmrk AS rmrk,
                COALESCE(c.emplyeNum, '') AS emplyeNum,
                COALESCE(c.indstrytyCd, '') AS indstrytyCd,
                COALESCE(c.recentprc, '') AS recentprc  /* 기업 정보에서 추출한 첫 번째 bidprcAmt 값 */
            ) AS enriched_biz
        FROM exploded_bizs b
        LEFT JOIN companies c ON b.biz.prcbdrBizno = c.prcbdrBizno
    ),
    grouped_bizs AS (
        SELECT 
            bidNtceNo,
            process_date,
            collect_list(enriched_biz) AS bizs_parsed_enriched
        FROM enriched_bizs
        GROUP BY bidNtceNo, process_date
    )
    SELECT 
        a.*,
        COALESCE(b.bizs_parsed_enriched, a.bizs_parsed) AS bizs_parsed_enriched
    FROM bids a
    LEFT JOIN grouped_bizs b ON a.bidNtceNo = b.bidNtceNo
    """)
    
    # 기존 bizs_parsed 컬럼을 제거하고 새로운 bizs_parsed_enriched로 대체
    enriched_df = enriched_df.drop("bizs_parsed").withColumnRenamed("bizs_parsed_enriched", "bizs_parsed")
    
    # 샘플 출력 - 기업 정보가 병합된 결과 확인
    sample_rows = enriched_df.select("bidNtceNo", "process_date", "bizs_parsed").limit(2).collect()
    logger.info("\n🔍 기업 정보가 병합된 bizs_parsed 샘플:")
    
    for i, row in enumerate(sample_rows):
        logger.info(f"\n샘플 {i+1}:")
        logger.info(f"  - bidNtceNo: {row['bidNtceNo']}")
        logger.info(f"  - process_date: {row['process_date']}")
        
        for j, biz in enumerate(row['bizs_parsed'][:2]):  # 처음 2개만 출력
            logger.info(f"    기업 {j+1}: {{opengRank: '{biz['opengRank']}', " +
                       f"prcbdrBizno: '{biz['prcbdrBizno']}', " +
                       f"emplyeNum: '{biz['emplyeNum']}', " +
                       f"indstrytyCd: '{biz['indstrytyCd']}', " +
                       f"recentprc: '{biz['recentprc']}'}}")  # recentprc는 bidprcAmt의 첫 번째 값
    
    return enriched_df


def process_csv_files():
    """CSV 파일 처리 및 HDFS 적재"""
    spark = create_spark_session()
    output_path = config.get_today_data_path()
    #"hdfs://namenode:8020/user/hadoop/data/bids/20250408"

    try:
        # HDFS 경로 확인 (삭제는 하지 않음)
        #path_exists = check_hdfs_path(output_path)
        path_exists = check_hdfs_path(output_path)
        if path_exists:
            logger.info(f"⚠️ 오늘 날짜 경로가 이미 존재합니다: {output_path}")
            logger.info("  - 기존 데이터를 유지하고 계속 진행합니다.")
        
        # company_full 데이터 로드 (bidprcAmt 첫 번째 값을 recentprc로 추출)
        company_df = load_company_full_from_mongodb(spark)
        
        # 전체 데이터프레임 초기화
        total_bid_df = None
        
        # 각 CSV 파일 처리
        for file in config.CSV_FILES:
            file_path = os.path.join(config.CSV_BASE_PATH, f"{file}.csv")
            
            if os.path.exists(file_path):
                # 파일 로드
                logger.info(f"📄 CSV 파일 로드 중: {file_path}")
                #df = spark.read.option("header", True).csv(file_path)
                # 🚨 Spark에게 로컬 파일임을 명시적으로 알려줌
                spark_file_path = f"file://{file_path}"  
                df = spark.read.option("header", True).csv(spark_file_path)

                # lcnsLmtNm 파싱 및 추가 처리
                df = df.withColumn("industry_code", regexp_extract(col("lcnsLmtNm"), r"/([0-9]+)$", 1)) \
                       .withColumn("lcnsLmtNm", regexp_extract(col("lcnsLmtNm"), r"^([^/]+)", 1))
                
                # bizs 컬럼 파싱
                df = df.withColumn("bizs_parsed", parse_bizs_column(col("bizs")))
                
                # 데이터프레임 병합
                if total_bid_df is None:
                    total_bid_df = df
                else:
                    total_bid_df = total_bid_df.unionByName(df, allowMissingColumns=True)
                
                logger.info(f"  - {file} 처리 완료: {df.count()} 행")
            else:
                logger.warning(f"⚠️ CSV 파일을 찾을 수 없습니다: {file_path}")
        
        if total_bid_df is None:
            logger.error("❌ 처리할 CSV 파일이 없습니다.")
            return False
        
        # 처리 날짜 컬럼 추가
        total_bid_df = total_bid_df.withColumn("process_date", lit(config.TODAY))
        
        # bizs_parsed 통계
        total_count = total_bid_df.count()
        non_empty_count = total_bid_df.filter(size(col("bizs_parsed")) > 0).count()
        empty_count = total_count - non_empty_count
        logger.info(f"\n📊 bizs_parsed 통계:")
        logger.info(f"  - 총 행 수: {total_count}")
        logger.info(f"  - 파싱 성공: {non_empty_count} 행 ({non_empty_count / total_count * 100:.2f}%)")
        logger.info(f"  - 파싱 실패: {empty_count} 행")
        
        # company_full 데이터로 bizs_parsed 보강 (첫 번째 bidprcAmt를 recentprc로)
        enriched_df = enrich_bizs_parsed_with_company_info(spark, total_bid_df, company_df)
        
        # HDFS에 저장
        logger.info(f"💾 데이터를 HDFS에 저장 중: {output_path}")
        # bizs 원본 컬럼은 제외하고 저장
        columns_to_drop = ["bizs"]
        enriched_df = enriched_df.drop(*columns_to_drop)
        enriched_df.write.mode("overwrite").parquet(output_path)
        logger.info(f"✅ HDFS 저장 완료: {output_path}")
        
        # 저장 검증
        logger.info("\n🔎 HDFS 저장 검증")
        reloaded = spark.read.parquet(output_path)
        logger.info(f"  - 저장된 레코드 수: {reloaded.count()}")
        
        # 샘플 데이터 확인
        sample = reloaded.select(
            "bidNtceNo", 
            explode("bizs_parsed").alias("biz")
        ).filter(col("biz.recentprc") != "").limit(5)
        
        logger.info("  - recentprc 샘플 확인:")
        sample.select("bidNtceNo", "biz.prcbdrBizno", "biz.recentprc").show(truncate=False)
        
        return True
    
    except Exception as e:
        logger.error(f"❌ 처리 중 오류 발생: {e}")
        import traceback
        logger.error(traceback.format_exc())
        return False
    
    finally:
        if spark:
            spark.stop()

def main():
    """메인 함수 - 명령줄에서 직접 실행할 때 사용"""
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
    logger.info(f"🚀 CSV -> HDFS 적재 시작 ({config.TODAY})")
    success = process_csv_files()
    if success:
        logger.info("✅ CSV -> HDFS 적재 완료")
    else:
        logger.error("❌ CSV -> HDFS 적재 실패")

if __name__ == "__main__":
    main()
