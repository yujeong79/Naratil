"""
MongoDB 저장 모듈
"""
import logging
import pandas as pd
import pymongo

import config_daily as config
import numpy as np

# 로거 설정
logger = logging.getLogger(__name__)

def inspect_dataframe(df, name, show_full=False, condition=None, sample_size=5):
    """
    데이터프레임의 기본 정보를 로깅하고 검사하는 함수
    
    Args:
        df (DataFrame): 검사할 데이터프레임
        name (str): 데이터프레임의 이름 (로깅용)
        show_full (bool, optional): 전체 데이터프레임 출력 여부. 기본값은 False.
        condition (callable, optional): 필터링에 사용할 조건 함수
        sample_size (int, optional): 미리보기로 표시할 레코드 수. 기본값은 5.
    
    Returns:
        bool: 데이터프레임 유효성 여부
    """
    if df is None:
        logger.warning(f"⚠️ {name}: 데이터프레임이 None입니다.")
        return False
    
    try:
        # Spark DataFrame을 Pandas DataFrame으로 변환
        records = df.toPandas()
        
        # 데이터프레임 기본 정보 로깅
        logger.info(f"\n🔍 {name} 데이터프레임 검사:")
        
        # 데이터프레임 크기 확인
        logger.info(f"  - 행 수: {len(records)}")
        logger.info(f"  - 열 수: {len(records.columns)}")
        
        # 컬럼 목록 로깅
        logger.info(f"  - 컬럼 목록: {', '.join(records.columns)}")
        
        # 결측값 확인
        null_counts = records.isnull().sum()
        null_cols = null_counts[null_counts > 0]
        if not null_cols.empty:
            logger.warning("  ⚠️ 결측값이 있는 컬럼:")
            for col, count in null_cols.items():
                logger.warning(f"    - {col}: {count}개")
        
        # 중복 레코드 확인
        duplicates = records.duplicated(subset=['bidNtceNo'])
        if duplicates.sum() > 0:
            logger.warning(f"  ⚠️ 중복된 bidNtceNo 레코드: {duplicates.sum()}개")
        
        # 조건부 필터링
        if condition:
            filtered_records = records[records.apply(condition, axis=1)]
            logger.info(f"\n🔎 조건에 맞는 레코드:")
            logger.info(filtered_records.to_string())
        
        # 첫 몇 개 레코드 미리보기
        logger.info("\n📋 레코드 미리보기:")
        preview = records.head(sample_size).to_dict(orient='records')
        for record in preview:
            logger.info(f"    {record}")
        
        # 전체 데이터 출력 옵션
        if show_full:
            logger.info("\n📦 전체 데이터:")
            # 대용량 데이터의 경우 전체 출력 제한
            if len(records) > 100:
                logger.warning("  ⚠️ 데이터가 너무 많아 처음 100개 레코드만 출력합니다.")
                logger.info(records.head(100).to_string())
            else:
                logger.info(records.to_string())
        
        return True
    except Exception as e:
        logger.error(f"❌ {name} 데이터프레임 검사 중 오류 발생: {e}")
        return False

def store_dataframe_to_collection(df, collection_name, mongo_client=None):
    """데이터프레임을 MongoDB 컬렉션에 저장 (인덱스 생성 없음)"""
    if df is None or df.rdd.isEmpty():
        logger.warning(f"⚠️ {collection_name}: 저장할 데이터가 없습니다")
        return
    
    close_client = False
    if mongo_client is None:
        logger.info(f"🔄 MongoDB 연결 중: {config.MONGO_URI}")
        mongo_client = pymongo.MongoClient(config.MONGO_URI)
        close_client = True
        
    try:
        db = mongo_client[config.MONGO_DB_NAME]
        collection = db[collection_name]
        
        # 데이터프레임을 파이썬 딕셔너리로 변환하여 처리
        records = df.toPandas()
        documents = []
        
        for _, record in records.iterrows():
            # MongoDB에 저장할 데이터 구성
            mongo_doc = {}
            
            # 기본 필드 저장
            for field in df.columns:
                # 배열 필드 처리를 위한 수정
                if field in record:
                    value = record[field]
                    # 배열/리스트가 아니거나, None이 아닌 경우에만 추가
                    if not isinstance(value, (list, np.ndarray)) and pd.notna(value):
                        mongo_doc[field] = value
                    # 배열/리스트인 경우, 비어있지 않으면 추가
                    elif isinstance(value, (list, np.ndarray)) and len(value) > 0:
                        mongo_doc[field] = value
            
            documents.append(mongo_doc)
        
        # 한 번에 모든 문서 삽입
        if documents:
            collection.insert_many(documents)
            logger.info(f"✅ {collection_name}에 {len(documents)} 레코드 삽입 완료")
        
    finally:
        if close_client:
            mongo_client.close()

def save_to_mongodb(no_limit_df, etc_df, industry_dfs):
    """처리된 데이터 MongoDB 저장"""
    logger.info("🔄 MongoDB 저장 시작")
    
    # MongoDB 연결
    mongo_client = pymongo.MongoClient(config.MONGO_URI)
    
    try:
        # 1. 업종 코드 없는 데이터 저장 (industry_no_limit)
        inspect_dataframe(no_limit_df, "업종 코드 없는 데이터", show_full=True)
        store_dataframe_to_collection(no_limit_df, config.NO_LIMIT_COLLECTION, mongo_client)
        
        # 2. 신규 업종 코드 데이터 저장 (industry_etc)
        if etc_df is not None and not etc_df.rdd.isEmpty():
            inspect_dataframe(etc_df, "업종 코드 기타 데이터", show_full=True)
            store_dataframe_to_collection(etc_df, config.DEFAULT_COLLECTION, mongo_client)
        else:
            logger.info(f"⏩ {config.DEFAULT_COLLECTION}: 저장할 데이터 없음")
        
        # 3. 기존 업종 코드별 컬렉션에 저장
        for code, df, count in industry_dfs:
            collection_name = f"{config.COLLECTION_PREFIX}{code}"
            inspect_dataframe(df, "업종 코드 단일 데이터", show_full=True)
            store_dataframe_to_collection(df, collection_name, mongo_client)
        
        # 컬렉션별 레코드 수 확인
        db = mongo_client[config.MONGO_DB_NAME]
        collections = db.list_collection_names()
        logger.info("\n📊 MongoDB 컬렉션 통계:")
        for coll in collections:
            count = db[coll].count_documents({})
            logger.info(f"  - {coll}: {count} 레코드")
        
        # recentprc 데이터 샘플 확인 (bidprcAmt의 첫 번째 값)
        no_limit_coll = db[config.NO_LIMIT_COLLECTION]
        sample_docs = list(no_limit_coll.find({}, {"bidNtceNo": 1, "bizs_parsed": 1}).limit(3))
        if sample_docs:
            logger.info("\n🔍 MongoDB에 저장된 recentprc 샘플 (bidprcAmt의 첫 번째 값):")
            for doc in sample_docs:
                if "bizs_parsed" in doc and doc["bizs_parsed"] and len(doc["bizs_parsed"]) > 0:
                    biz = doc["bizs_parsed"][0]
                    # biz가 리스트인 경우와 딕셔너리인 경우를 모두 처리
                    if isinstance(biz, dict):
                        logger.info(f"  bidNtceNo: {doc['bidNtceNo']}, " +
                                f"prcbdrBizno: {biz.get('prcbdrBizno', '')}, " +
                                f"recentprc: {biz.get('recentprc', '')}")
                    elif isinstance(biz, list) and len(biz) > 0:
                        # biz가 리스트인 경우 첫 번째 항목 사용 또는 리스트 자체를 출력
                        logger.info(f"  bidNtceNo: {doc['bidNtceNo']}, " +
                                f"bizs_parsed 첫 번째 항목: {biz}")
                    else:
                        logger.info(f"  bidNtceNo: {doc['bidNtceNo']}, " +
                                f"bizs_parsed 타입: {type(biz)}, 값: {biz}")
        logger.info("✅ MongoDB 저장 완료")
    finally:
        mongo_client.close()
