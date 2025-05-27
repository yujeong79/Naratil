"""
MongoDB 데이터 전송 모듈 
"""
import logging
import json
import math
import os
from pymongo import MongoClient
from . import config_year as config  # ← 수정된 부분
from pyspark.accumulators import AccumulatorParam

# 로거 설정
logger = logging.getLogger(__name__)

def init_mongodb():
    """MongoDB 연결 초기화"""
    try:
        client = MongoClient(config.MONGODB_URI)
        db = client[config.MONGODB_DB]
        client.admin.command('ping')  # 연결 테스트
        logger.info(f"✅ MongoDB 연결 성공: {config.MONGODB_URI}/{config.MONGODB_DB}")
        return client, db
    except Exception as e:
        logger.error(f"❌ MongoDB 연결 실패: {str(e)}")
        return None, None

class DictAccumulatorParam(AccumulatorParam):
    """Dictionary 타입의 Accumulator 구현"""
    def zero(self, initialValue):
        return {"count": 0, "success": 0, "failure": 0}
    
    def addInPlace(self, v1, v2):
        v1["count"] += v2["count"]
        v1["success"] += v2["success"]
        v1["failure"] += v2["failure"]
        return v1

def send_to_mongodb(df, collection_name):
    """DataFrame을 MongoDB로 전송"""
    total_count = df.count()
    if total_count == 0:
        logger.info(f"⚠️ {collection_name}에 데이터가 없습니다.")
        return 0

    os.makedirs(config.TEMP_DIR, exist_ok=True)
    chunk_size = config.CHUNK_SIZE
    chunks_count = math.ceil(total_count / chunk_size)
    logger.info(f"  총 {total_count}건, {chunks_count}개 청크로 전송")

    sc = df.sparkSession.sparkContext
    stats_acc = sc.accumulator({"count": 0, "success": 0, "failure": 0}, DictAccumulatorParam())
    df = df.repartition(chunks_count)

    def process_partition(partition_idx, iterator):
        client, db = init_mongodb()
        if client is None or db is None:
            return
        
        try:
            collection = db[collection_name]
            batch = []
            batch_count = 0
            batch_size = min(1000, chunk_size)

            for row in iterator:
                record = row.asDict()
                if 'bidNtceId' in record and not isinstance(record['bidNtceId'], int):
                    try:
                        record['bidNtceId'] = int(record['bidNtceId'])
                    except (ValueError, TypeError):
                        pass
                
                if 'vectorNm' in record and not isinstance(record['vectorNm'], list):
                    record['vectorNm'] = [] if record['vectorNm'] is None else list(record['vectorNm'])

                batch.append(record)
                batch_count += 1

                if len(batch) >= batch_size:
                    try:
                        result = collection.insert_many(batch)
                        stats_acc.add({"count": len(batch), "success": len(result.inserted_ids), "failure": 0})
                    except Exception as e:
                        stats_acc.add({"count": len(batch), "success": 0, "failure": len(batch)})
                        logger.error(f"  ❌ 배치 삽입 중 오류 (파티션 {partition_idx}): {str(e)}")
                        try:
                            error_file = f"{config.TEMP_DIR}/{collection_name}_partition_{partition_idx}_batch_{batch_count//batch_size}.json"
                            with open(error_file, 'w', encoding='utf-8') as f:
                                json.dump(batch, f, ensure_ascii=False, indent=2)
                            logger.info(f"  ⚠️ 실패한 배치를 {error_file}에 저장했습니다.")
                        except Exception as backup_error:
                            logger.error(f"  ❌ 백업 파일 저장 실패: {str(backup_error)}")
                    batch = []

            if batch:
                try:
                    result = collection.insert_many(batch)
                    stats_acc.add({"count": len(batch), "success": len(result.inserted_ids), "failure": 0})
                except Exception as e:
                    stats_acc.add({"count": len(batch), "success": 0, "failure": len(batch)})
                    logger.error(f"  ❌ 남은 배치 삽입 중 오류 (파티션 {partition_idx}): {str(e)}")
                    try:
                        error_file = f"{config.TEMP_DIR}/{collection_name}_partition_{partition_idx}_final_batch.json"
                        with open(error_file, 'w', encoding='utf-8') as f:
                            json.dump(batch, f, ensure_ascii=False, indent=2)
                        logger.info(f"  ⚠️ 실패한 배치를 {error_file}에 저장했습니다.")
                    except Exception as backup_error:
                        logger.error(f"  ❌ 백업 파일 저장 실패: {str(backup_error)}")

        finally:
            if client is not None:
                client.close()

    def init_collection():
        client, db = init_mongodb()
        if client and db:
            try:
                db[collection_name].drop()
                logger.info(f"✅ {collection_name} 컬렉션 초기화 완료")
            finally:
                client.close()

    def create_indexes():
        client, db = init_mongodb()
        if client and db:
            try:
                collection = db[collection_name]
                collection.create_index("bidNtceId")
                if collection_name.startswith("industry_"):
                    collection.create_index("industry_code")
                logger.info(f"✅ {collection_name} 인덱스 생성 완료")
            finally:
                client.close()

    init_collection()

    df.rdd.mapPartitionsWithIndex(
        lambda idx, iterator: [process_partition(idx, iterator)]
    ).foreach(lambda x: None)

    if stats_acc.value["success"] > 0:
        create_indexes()

    stats = stats_acc.value
    logger.info(f"✅ {collection_name} 데이터 전송 완료: {stats['success']}/{total_count}건 성공 ({stats['failure']}건 실패)")
    return stats['success']
