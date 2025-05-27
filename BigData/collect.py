""" 공고번호별 필요한 API 요청 응답을 모두 수집하여 각각의 parquet 파일로 저장 """
import asyncio
import aiohttp
import json
import pandas as pd
from datetime import datetime
from pyspark.sql.types import StructType, StructField, StringType, ArrayType
from utils import get_spark_session, get_hdfs_client, get_hdfs_path, get_service_key

class BidDetailCollector:
    def __init__(self):
        self.spark = get_spark_session("CollectDetailData")
        self.client = get_hdfs_client()
        self.hdfs_path = get_hdfs_path()
        self.service_key = get_service_key()

        self.category_suffix = {
            "Thing": "Thng",
            "ConstructionWork": "Cnstwk",
            "Service": "Servc"
        }

        self.required_columns = {
            "BidPublicNotice": ["ntceInsttNm", "dminsttNm", "cntrctCnclsMthdN", "bidBeginDt", "bidQlfctRgstDt", "bidClseDt", "presmptPrce", "prearngPrceDcsnMthdNm", "sucsfbidLwltRate", "ntceInsttOfclNm", "bidNtceDtlUrl"],
            "LicenseLimit": ["lcnsLmtNm"],
            "OpeningComplete": ["opengRank", "prcbdrBizno", "prcbdrNm", "bidprcAmt", "bidprcrt", "rmrk"],
            "PossibleRegion": ["prtcptPsblRgnNm"]
        }

        self.api_fetch_functions = {
            "BidPublicNotice": self.fetch_BidPublicNotice,
            "LicenseLimit": self.fetch_LicenseLimit,
            "OpeningComplete": self.fetch_OpeningComplete,
            "PossibleRegion": self.fetch_PossibleRegion
        }

    def resolve_endpoint(self, category, api_name):
        suffix = self.category_suffix.get(category, "")
        if api_name == "BidPublicNotice":
            return f"http://apis.data.go.kr/1230000/ad/BidPublicInfoService/getBidPblancListInfo{suffix}"
        elif api_name == "PossibleRegion":
            return "http://apis.data.go.kr/1230000/ad/BidPublicInfoService/getBidPblancListInfoPrtcptPsblRgn"
        elif api_name == "LicenseLimit":
            return "http://apis.data.go.kr/1230000/ad/BidPublicInfoService/getBidPblancListInfoLicenseLimit"
        elif api_name == "OpeningComplete":
            return "http://apis.data.go.kr/1230000/as/ScsbidInfoService/getOpengResultListInfoOpengCompt"
        else:
            raise ValueError(f"❌ 정의되지 않은 API 이름입니다: {api_name}")

    def load_bid_info(self, category):
        path = f"{self.hdfs_path}JoinTargets/{category}/SuccessfulBid_{category}.parquet"
        df = self.spark.read.parquet(path)
        return [(r.bidNtceNo, r.bidNtceOrd) for r in df.select("bidNtceNo", "bidNtceOrd").distinct().collect()]

    async def fetch_api_data(self, session, category, api_name, bid_info, params):
        url = self.resolve_endpoint(category, api_name)
        headers = {
            "Accept": "application/json",
            "User-Agent": "Mozilla/5.0 (compatible; Python aiohttp client)"
        }

        MAX_RETRIES = 3
        RETRY_DELAY = 1  # 초 단위 지연 시간

        for attempt in range(MAX_RETRIES):
            try:
                async with session.get(url, params=params, headers=headers, timeout=aiohttp.ClientTimeout(total=8)) as res:
                    content_type = res.headers.get("Content-Type", "")
                    text = await res.text()

                    # ✅ JSON 응답인 경우
                    if "application/json" in content_type:
                        data = json.loads(text)
                        items = data.get("response", {}).get("body", {}).get("items", [])
                        if items:
                            if api_name == "OpeningComplete":
                                return {
                                    "bidNtceNo": bid_info[0],
                                    "bizs": [{col: i.get(col, "") for col in self.required_columns[api_name]} for i in items]
                                }
                            else:
                                result = {col: items[0].get(col, "") for col in self.required_columns[api_name]}
                                result["bidNtceNo"] = bid_info[0]
                                return result

                    # ❗ 요청 제한 오류 (XML 응답 내 문자열로 감지)
                    if "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR" in text:
                        await asyncio.sleep(RETRY_DELAY)
                        continue

                    # ❗ 기타 XML 또는 비정상 응답 로그 출력
                    print(f"⚠️ {category} | {api_name} | {bid_info[0]} 비JSON 응답 {text[:300]}...")
                    break  # 더 시도하지 않음

            except Exception as e:
                await asyncio.sleep(RETRY_DELAY)

        if api_name == "OpeningComplete":
            return {"bidNtceNo": bid_info[0], "bizs": {}}

        return {"bidNtceNo": bid_info[0]}

    async def fetch_BidPublicNotice(self, session, category, bid_info, api_name):
        return await self.fetch_api_data(session, category, api_name, bid_info, {
            "serviceKey": self.service_key,
            "pageNo": 1,
            "numOfRows": 1,
            "type": "json",
            "inqryDiv": 2,
            "bidNtceNo": bid_info[0]
        })

    async def fetch_LicenseLimit(self, session, category, bid_info, api_name):
        return await self.fetch_api_data(session, category, api_name, bid_info, {
            "serviceKey": self.service_key,
            "pageNo": 1,
            "numOfRows": 1,
            "inqryDiv": 2,
            "bidNtceNo": bid_info[0],
            "bidNtceOrd": bid_info[1],
            "type": "json"
        })

    async def fetch_OpeningComplete(self, session, category, bid_info, api_name):
        return await self.fetch_api_data(session, category, api_name, bid_info, {
            "serviceKey": self.service_key,
            "pageNo": 1,
            "numOfRows": 5,
            "bidNtceNo": bid_info[0],
            "type": "json"
        })

    async def fetch_PossibleRegion(self, session, category, bid_info, api_name):
        return await self.fetch_api_data(session, category, api_name, bid_info, {
            "serviceKey": self.service_key,
            "pageNo": 1,
            "numOfRows": 1,
            "inqryDiv": 2,
            "bidNtceNo": bid_info[0],
            "bidNtceOrd": bid_info[1],
            "type": "json"
        })

    async def fetch_response(self, category, bid_info_list, api_name, max_connections=10):
        fetch_function = self.api_fetch_functions[api_name]
        connector = aiohttp.TCPConnector(limit=max_connections)
        async with aiohttp.ClientSession(connector=connector) as session:
            tasks = [fetch_function(session, category, bid_info, api_name) for bid_info in bid_info_list]
            results = []
            for i in range(0, len(tasks), 100):
                batch = tasks[i:i+100]
                batch_result = await asyncio.gather(*batch)
                await asyncio.sleep(0.2)
                results.extend(batch_result)
                print(f"🔄 진행률: {min(i+100, len(tasks))}/{len(tasks)}건 완료")
            return results

    def save_response_to_parquet(self, category, bid_info_list, api_name):
        all_data = asyncio.run(self.fetch_response(category, bid_info_list, api_name))
        pd_df = pd.DataFrame(all_data)
        output_path = f"{self.hdfs_path}JoinTargets/{category}/{api_name}_{category}.parquet"

        if api_name == "OpeningComplete":
            spark_schema = StructType([
                StructField("bidNtceNo", StringType()),
                StructField("bizs", ArrayType(
                    StructType([
                        StructField("opengRank", StringType()),
                        StructField("prcbdrBizno", StringType()),
                        StructField("prcbdrNm", StringType()),
                        StructField("bidprcAmt", StringType()),
                        StructField("bidprcrt", StringType()),
                        StructField("rmrk", StringType()),
                    ])
                ))
            ])
        else:
            spark_schema = StructType([StructField(col, StringType(), True) for col in pd_df.columns])

        spark_df = self.spark.createDataFrame(pd_df, schema=spark_schema)
        spark_df.write.mode("overwrite").parquet(output_path)
        print(f"✅ 저장 완료: {output_path} ({len(all_data)}건)")

    def collect_all(self):
        for category in self.category_suffix:
            print(f"📆 {category} 수집 시작...")
            bid_info_list = self.load_bid_info(category)

            for api_name in self.api_fetch_functions:
                self.save_response_to_parquet(category, bid_info_list, api_name)

if __name__ == "__main__":
    collector = BidDetailCollector()
    collector.collect_all()
    print("🎉 수집 완료!")
