import asyncio
import aiohttp
import json
import pandas as pd
from pyspark.sql.types import *
from utils import get_spark_session, get_hdfs_path, get_service_key

# 📌 Spark 및 HDFS 설정
spark = get_spark_session("BizInfoProcessing")
HDFS_PATH = get_hdfs_path()
FINAL_PATH = f"{HDFS_PATH}FinalData/Biz/FinalBiz.parquet"

# 📌 API 설정
SERVICE_KEY = get_service_key()

# 📌 API별 ENDPOINT
API_ENDPOINTS = {
    "BasicInfo": "http://apis.data.go.kr/1230000/ao/UsrInfoService/getPrcrmntCorpBasicInfo",
    "IndustryTypeInfo": "http://apis.data.go.kr/1230000/ao/UsrInfoService/getPrcrmntCorpIndstrytyInfo"
}

# 📌 API 응답별 필터링할 컬럼 목록
REQUIRED_COLUMNS = {
    "BasicInfo": "emplyeNum",
    "IndustryTypeInfo": "indstrytyCd"
}

def get_path(category, inqry_date):
    """ 조회 날짜를 기준으로 파일 경로들을 반환 """

    return {
        "biz_info": f"{HDFS_PATH}JoinTargets/Biz/{inqry_date}/BizInfo_{category}_{inqry_date}.parquet",
        "basic_info": f"{HDFS_PATH}JoinTargets/Biz/{inqry_date}/BasicInfo_{category}_{inqry_date}.parquet",
        "industry_type": f"{HDFS_PATH}JoinTargets/Biz/{inqry_date}/IndustryType_{category}_{inqry_date}.parquet"
    }

def extract_api_targets(merged_list, biz_info_path):
    """ merged_list와 FinalBiz.parquet을 비교해 기존 기업은 병합, 신규 기업은 별도 저장 """

    # 기존 Final Biz 데이터 불러오기
    try:
        final_data = spark.read.parquet(FINAL_PATH).toPandas()
        print(f"📥 기업 데이터 로딩 완료 ({len(final_data)}건)")
    except:
        final_data = pd.DataFrame(columns=["prcbdrBizno", "prcbdrNm", "bidprcAmt", "emplyeNum", "indstrytyCd"])
        print("⚠️ 기존 FinalBiz 없음 → 초기 상태로 처리")

    # Final Biz → dict 변환
    final_biz_dict = {
        row["prcbdrBizno"]: { # prcbdrBizno가 key, prcbdrNm와 bidprcAmt가 value
            "prcbdrNm": row["prcbdrNm"], 
            "bidprcAmt": row["bidprcAmt"] if isinstance(row["bidprcAmt"], list) else [row["bidprcAmt"]],
            "emplyeNum": row.get("emplyeNum", ""),
            "indstrytyCd": row.get("indstrytyCd", "")
        }
        for _, row in final_data.iterrows() 
    }

    # 신규 API 요청 대상 추출
    api_targets = []

    # 병합 처리
    for row in merged_list:
        bizno = row["prcbdrBizno"]
        name = row["prcbdrNm"]
        new_amts = row["bidprcAmt"]

        if bizno in final_biz_dict: # 기존 Final Biz에 이미 있는 prcbdrBizno라면 새로운 bidprcAmt만 추가
            existing_amts = final_biz_dict[bizno]["bidprcAmt"]
            final_biz_dict[bizno]["bidprcAmt"] = new_amts + existing_amts
        else: # 기존 Final Biz에 없는 prcbdrBizno라면 API 요청 대상
            api_targets.append({"prcbdrBizno": bizno, "prcbdrNm": name, "bidprcAmt": new_amts})

    # 새로운 금액을 추가한 Final Biz를 새롭게 저장
    final_df = pd.DataFrame([
        {
            "prcbdrBizno": k,
            "prcbdrNm": v["prcbdrNm"],
            "bidprcAmt": v["bidprcAmt"],
            "emplyeNum": v["emplyeNum"],
            "indstrytyCd": v["indstrytyCd"]
        }
        for k, v in final_biz_dict.items()
    ])

    schema = StructType([
        StructField("prcbdrBizno", StringType()),
        StructField("prcbdrNm", StringType()),
        StructField("bidprcAmt", ArrayType(StringType())),
        StructField("emplyeNum", StringType()),
        StructField("indstrytyCd", StringType())
    ])

    if final_df.empty:
        print("⚠️ 최종 병합 대상이 없어 Spark 저장 생략")
    else:
        spark_df = spark.createDataFrame(final_df, schema=schema)
        spark_df.write.mode("overwrite").parquet(FINAL_PATH)
        print(f"✅ 기존 기업 병합 저장 완료 ({len(final_df)}건)")

    # 신규 API 요청 대상은 JoinTarget에 따로 저장
    if api_targets:
        pd_api_df = pd.DataFrame(api_targets)
        spark_api_df = spark.createDataFrame(pd_api_df)
        spark_api_df.write.mode("overwrite").parquet(biz_info_path)
        print(f"📦 신규 API 대상 저장 완료 ({len(api_targets)}건)")
    else:
        print("✅ 신규 API 요청 대상 없음")

def merge_biz_list(new_biz_list: list[dict]) -> list[dict]:
    """ new_biz_list를 prcbdrBizno를 기준으로 병합하여 list[dict] 반환"""
    biz_dict = {}

    for biz in new_biz_list:
        bizno = biz["prcbdrBizno"]
        name = biz["prcbdrNm"]
        amount = biz["bidprcAmt"]

        if bizno in biz_dict: # 중복된 사업자 번호라면
            biz_dict[bizno]["bidprcAmt"].insert(0, amount) # 기존의 행에 금액만 추가
        else:
            biz_dict[bizno] = {
                "prcbdrNm": name,
                "bidprcAmt": [amount]
            }

    return [
        {"prcbdrBizno": k, "prcbdrNm": v["prcbdrNm"], "bidprcAmt": v["bidprcAmt"]}
        for k, v in biz_dict.items()
    ]

# 기업 데이터 구축을 위한 추가적인 API 요청 및 응답 병합

def load_biz_info(biz_info_path):
    print(f"📥 HDFS에서 기업 정보 데이터 로딩 중")

    df = spark.read.parquet(biz_info_path)
    biz_list = [row.prcbdrBizno for row in df.select("prcbdrBizno").distinct().collect()]
    print(f"✅ 총 {len(biz_list)}건의 사업자 번호 로드 완료")
    return biz_list

async def fetch_BasicInfo(session, biz_no, api_name):
    """[사용자정보서비스] 조달업체 기본정보"""
    params = {
        "serviceKey":SERVICE_KEY,
        "pageNo":1,
        "numOfRows":1,
        "inqryDiv":3, # 사업자등록번호
        "bizno":biz_no,
        "type":"json"
    }
    return await fetch_api_data(session, api_name, biz_no, params)

async def fetch_IndustryTypeInfo(session, biz_no, api_name):
    """[사용자정보서비스] 조달업체 업종정보 조회"""
    params = {
        "serviceKey":SERVICE_KEY,
        "pageNo":1,
        "numOfRows":1,
        "inqryDiv":1, # 사업자등록번호
        "bizno":biz_no,
        "type":"json"
    }
    return await fetch_api_data(session, api_name, biz_no, params)

MAX_RETRIES = 3
RETRY_DELAY = 1

# 3️⃣ API 요청
async def fetch_api_data(session, api_name, biz_no, params):
    headers = {
        "Accept": "application/json",
        "User-Agent": "Mozilla/5.0 (compatible; Python aiohttp client)"
    }

    for attempt in range(MAX_RETRIES):
        try:
            async with session.get(API_ENDPOINTS.get(api_name), params=params, headers=headers, timeout=aiohttp.ClientTimeout(total=8)) as response:
                content_type = response.headers.get("Content-Type", "")
                text = await response.text()

                # ✅ JSON 응답인 경우
                if "application/json" in content_type:
                    json_data = json.loads(text)
                    items = json_data.get("response", {}).get("body", {}).get("items", [])
                    if items:
                        filtered = {REQUIRED_COLUMNS.get(api_name): items[0].get(REQUIRED_COLUMNS.get(api_name), "")}
                        filtered["prcbdrBizno"] = biz_no
                        return filtered

                # ❗ XML 응답이지만 요청 제한 에러일 경우
                if "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR" in text:
                    #print(f"⏳ {bid_info[0]} 요청 제한 초과, {RETRY_DELAY}초 후 재시도... (시도 {attempt + 1}/{MAX_RETRIES})")
                    await asyncio.sleep(RETRY_DELAY)
                    continue

                # ❗ 기타 XML 응답
                #print(f"⚠️ {biz_no} 응답이 JSON이 아님: {content_type}")
                print(biz_no, " ", api_name, "\n", text[:300])  # 디버깅용 일부 출력
                break

        except Exception as e:
            #print(f"⚠️ {biz_no} 요청 예외 (시도 {attempt + 1}/{MAX_RETRIES}): {e}")
            await asyncio.sleep(RETRY_DELAY)

    # 실패 시 기본값 반환
    return {"prcbdrBizno": biz_no}

# 1️⃣ api_name에 해당하는 함수 매칭 & 2️⃣ 요청 병렬 실행
async def fetch_response(biz_list, api_name, max_connections=10):
    fetch_function = API_FETCH_FUNCTIONS.get(api_name) # api_name에 해당하는 함수 가져오기

    connector = aiohttp.TCPConnector(limit=max_connections)
    async with aiohttp.ClientSession(connector=connector) as session:
        # 1️⃣ api_name에 해당하는 함수 매칭
        tasks = [fetch_function(session, biz_no, api_name) for biz_no in biz_list]
        results = []

        # 2️⃣ 요청 병렬 실행
        for i in range(0, len(tasks), 100):  # 100개씩 실행하며 진행률 추적
            batch = tasks[i:i+100]
            batch_result = await asyncio.gather(*batch)
            await asyncio.sleep(0.2)
            results.extend(batch_result)
            print(f"🔄 진행률: {min(i+100, len(tasks))}/{len(tasks)}건 완료")
        return results

# 0️⃣  최종 실행 함수
def save_response_to_parquet(biz_list, api_name, output_path):
    print(f"🚀 {api_name} 요청 시작...")

    # 1️⃣ api_name에 해당하는 함수 매칭 & 2️⃣ 요청 병렬 실행
    all_data = asyncio.run(fetch_response(biz_list, api_name))

    # 4️⃣ 응답을 parquet로 HDFS에 저장
    print("📦 Pandas DataFrame 생성 중...")
    pd_df = pd.DataFrame(all_data)

    spark_schema = StructType([StructField(col, StringType(), True) for col in pd_df.columns])
    spark_df = spark.createDataFrame(pd_df, schema=spark_schema)

    print(f"💾 Parquet 저장 중: {output_path}")
    spark_df.write.mode("overwrite").parquet(output_path)

def join_data(path):
    """ 사업자 등록 번호를 기준으로 parquet 파일을 JOIN """

    print("📥 Parquet 파일 불러오는 중...")
    df1 = spark.read.parquet(path.get("biz_info"))
    df2 = spark.read.parquet(path.get("basic_info"))
    df3 = spark.read.parquet(path.get("industry_type"))

    print("🔗 DataFrame 병합 중 (기준: prcbdrBizno)...")
    joined_df = (
        df1.join(df2, "prcbdrBizno", "outer") 
           .join(df3, "prcbdrBizno", "outer")
    )

    print("🔍 샘플 출력:")
    joined_df.show(10)

    print("📁 기존 FinalBiz 불러오기...")
    try:
        existing_df = spark.read.parquet(FINAL_PATH).cache()
        existing_df.count()
        print(f"✅ 기존 데이터 수: {existing_df.count()}")
    except:
        print("⚠️ 기존 FinalBiz 없음 → 새로 생성 시작")
        existing_df = spark.createDataFrame([], joined_df.schema)

    print("➕ 신규 데이터 append 중...")
    final_df = existing_df.unionByName(joined_df)

    print(f"💾 최종 결과 저장: {FINAL_PATH}")
    final_df \
        .coalesce(1) \
        .write.mode("overwrite").parquet(FINAL_PATH)

    print("✅ 최종 FinalBiz 저장 완료! 총 데이터 수:", final_df.count())

def collect_biz_data(path):
    """ 추가적인 API 요청 후 데이터 병합 """

    biz_list = load_biz_info(path.get("biz_info")) # 📌 새로 수집해야하는 사업자 등록 번호 조회

    save_response_to_parquet(biz_list, "BasicInfo", path.get("basic_info"))
    save_response_to_parquet(biz_list, "IndustryTypeInfo", path.get("industry_type"))

    join_data(path)

# 📌 API 요청별 함수 매핑
API_FETCH_FUNCTIONS = {
    "BasicInfo": fetch_BasicInfo,
    "IndustryTypeInfo": fetch_IndustryTypeInfo 
}

def merge_and_save_bizinfo(category, inqry_date, new_biz_list):
    """ collect.py에서 새롭게 받은 new_biz_list를 일단 사업자 번호 기준으로 병합 """

    path = get_path(category, inqry_date)

    # 신규로 받은 new_biz_list를 사업자등록번호 기준으로 병합
    merge_list = merge_biz_list(new_biz_list)

    # 병합된 신규 merge_list 중 새로 API 요청을 보내야하는 대상만 추출
    extract_api_targets(merge_list, path.get("biz_info"))

    # API 요청
    collect_biz_data(path)

if __name__ == "__main__":
    path = get_path("ConstructionWork", "202412")
    join_data(path)
