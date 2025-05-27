"""
데이터 처리 및 컬렉션 결정 모듈
"""
import logging
import pymongo
from pyspark.sql.functions import col, row_number, explode, lit
from pyspark.sql.window import Window

import config_daily as config

# 로거 설정
logger = logging.getLogger(__name__)

def get_existing_industry_codes():
    """MongoDB 기존 업종 코드 컬렉션 확인"""
    existing_industry_codes = set()
    try:
        client = pymongo.MongoClient(config.MONGO_URI)
        db = client[config.MONGO_DB_NAME]
        existing_collections = db.list_collection_names()
        prefix_len = len(config.COLLECTION_PREFIX)

        for coll_name in existing_collections:
            if coll_name.startswith(config.COLLECTION_PREFIX) and \
               coll_name != config.DEFAULT_COLLECTION and \
               coll_name != config.NO_LIMIT_COLLECTION:
                code = coll_name[prefix_len:]
                if code.isdigit():
                    existing_industry_codes.add(code)

        logger.info(f"📂 기존 업종 코드 컬렉션: {sorted(list(existing_industry_codes))}")
        client.close()
    except Exception as e:
        logger.error(f"❌ MongoDB 연결 오류: {e}")
    return existing_industry_codes

def get_max_bid_id(collection_name):
    """특정 컬렉션의 최대 bidNtceId 값 조회"""
    try:
        client = pymongo.MongoClient(config.MONGO_URI)
        db = client[config.MONGO_DB_NAME]

        if collection_name not in db.list_collection_names():
            logger.info(f"📌 컬렉션 {collection_name}이(가) 존재하지 않음, ID는 0부터 시작")
            client.close()
            return -1

        result = db[collection_name].find_one(sort=[("bidNtceId", pymongo.DESCENDING)])
        max_id = result["bidNtceId"] if result and "bidNtceId" in result else -1

        logger.info(f"📊 컬렉션 {collection_name}의 최대 bidNtceId: {max_id}")
        client.close()
        return max_id
    except Exception as e:
        logger.error(f"❌ 최대 bidNtceId 조회 오류 (컬렉션: {collection_name}): {e}")
        return -1

def process_data(spark, df_vector):
    """
    업종 코드 기준으로 데이터 분리 및 컬렉션별 ID 부여
    - 업종 코드가 있고, 기존 컬렉션에 있는 코드: 해당 업종 컬렉션
    - 업종 코드가 있지만 기존 컬렉션에 없는 코드: industry_etc 컬렉션
    - 업종 코드가 없는 경우: industry_no_limit 컬렉션
    """
    logger.info("📊 업종 코드별 데이터 분리 중...")

    existing_industry_codes = get_existing_industry_codes()
    no_limit_df = df_vector.filter(col("industry_code").isNull())

    industry_dfs = []
    etc_df = None
    unique_codes = df_vector.filter(col("industry_code").isNotNull()) \
                            .select("industry_code").distinct().collect()

    window_spec = Window.orderBy("bidNtceNo")

    for row in unique_codes:
        code = row["industry_code"]
        if not code:
            continue

        df_code = df_vector.filter(col("industry_code") == code)
        count = df_code.count()

        if code in existing_industry_codes:
            collection_name = f"{config.COLLECTION_PREFIX}{code}"
            max_id = get_max_bid_id(collection_name)
            start_id = max_id + 1

            df_with_id = df_code.withColumn(
                "bidNtceId",
                row_number().over(window_spec) + lit(start_id) - 1
            )
            industry_dfs.append((code, df_with_id, count))
            logger.info(f"업종 코드 {code}: {count}건, ID {start_id}~{start_id + count - 1} (기존 컬럼션에 추가)")
        else:
            logger.info(f"업종 코드 {code}: {count}건 - industry_etc로 분리 (신규 업조)")
            etc_df = df_code if etc_df is None else etc_df.union(df_code)

    no_limit_count = no_limit_df.count()
    if no_limit_count > 0:
        max_id = get_max_bid_id(config.NO_LIMIT_COLLECTION)
        start_id = max_id + 1
        no_limit_df = no_limit_df.withColumn(
            "bidNtceId",
            row_number().over(window_spec) + lit(start_id) - 1
        )
        sample = no_limit_df.select("bidNtceId", "bidNtceNo").limit(3).collect()
        logger.info(f"industry_no_limit: {no_limit_count}건, ID {start_id}~{start_id + no_limit_count - 1}, 샘플:")
        for row in sample:
            logger.info(f"  bidNtceId: {row['bidNtceId']}, bidNtceNo: {row['bidNtceNo']}")

    if etc_df is not None and not etc_df.rdd.isEmpty():
        max_id = get_max_bid_id(config.DEFAULT_COLLECTION)
        start_id = max_id + 1
        etc_count = etc_df.count()
        etc_df = etc_df.withColumn(
            "bidNtceId",
            row_number().over(window_spec) + lit(start_id) - 1
        )
        sample = etc_df.select("bidNtceId", "bidNtceNo", "industry_code").limit(3).collect()
        logger.info(f"industry_etc: {etc_count}건, ID {start_id}~{start_id + etc_count - 1}, 샘플:")
        for row in sample:
            logger.info(f"  bidNtceId: {row['bidNtceId']}, bidNtceNo: {row['bidNtceNo']}, industry_code: {row['industry_code']}")

        # MongoDB에 industry_etc 저장
        # try:
        #     client = pymongo.MongoClient(config.MONGO_URI)
        #     db = client[config.MONGO_DB_NAME]

        #     if config.DEFAULT_COLLECTION not in db.list_collection_names():
        #         logger.info(f"ℹ️ '{config.DEFAULT_COLLECTION}' 컬럼션이 없어 새로 생성 예정.")
        #         db.create_collection(config.DEFAULT_COLLECTION)

        #     etc_data = [row.asDict(recursive=True) for row in etc_df.collect()]
        #     if etc_data:
        #         db[config.DEFAULT_COLLECTION].insert_many(etc_data)
        #         logger.info(f"📃 {len(etc_data)}건 industry_etc 컬럼션에 저장 완료")
        #     client.close()
        # except Exception as e:
        #     logger.error(f"❌ industry_etc 저장 오류: {e}")
    else:
        logger.info("industry_etc: 0건 (신규 업조 없음)")
        etc_df = None

    logger.info("\n✅ 데이터 분리 완료:")
    logger.info(f"  - industry_no_limit: {no_limit_count}건")
    logger.info(f"  - 업종별 컬럼션: {len(industry_dfs)}개")
    logger.info(f"  - industry_etc: {etc_df.count() if etc_df is not None else 0}건")

    if no_limit_count > 0:
        sample = no_limit_df.select("bidNtceNo", explode("bizs_parsed").alias("biz")) \
                                .filter(col("biz.recentprc") != "").limit(3)
        logger.info("\n🔍 recentprc 샘플 (bidprcAmt의 첫 번째 값):")
        sample.select("bidNtceNo", "biz.prcbdrBizno", "biz.recentprc").show(truncate=False)

    return no_limit_df, etc_df, industry_dfs
