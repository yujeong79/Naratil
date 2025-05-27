""" 오늘치 낙찰 데이터 수집하여 HDFS에 저장 """
import requests
from datetime import datetime
from pyspark.sql.types import StructType, StructField, StringType
from utils import get_spark_session, get_hdfs_client, get_hdfs_path, get_service_key

class BidDataFetcher:
    def __init__(self, start_date: str, end_date: str):
        self.spark = get_spark_session("FetchData")
        self.hdfs = get_hdfs_client()
        self.hdfs_path = get_hdfs_path()
        self.service_key = get_service_key()
        self.start_date = start_date  # 예: '202503310000'
        self.end_date = end_date      # 예: '202504010000'
        self.num_of_rows = 999
        self.api_endpoints = {
            "Thing": "http://apis.data.go.kr/1230000/as/ScsbidInfoService/getScsbidListSttusThng",
            "ConstructionWork": "http://apis.data.go.kr/1230000/as/ScsbidInfoService/getScsbidListSttusCnstwk",
            "Service": "http://apis.data.go.kr/1230000/as/ScsbidInfoService/getScsbidListSttusServc"
        }
    
    def fetch_all(self):
        """ 물품/공사/용역별로 오늘치 낙찰 데이터 수집 """
        for category in self.api_endpoints:
            self.fetch_category(category)
    
    def fetch_category(self, category):
        """ 오늘치 낙찰 데이터 수집 """
        all_data = []
        page_no = 1

        while True:
            params = {
                "serviceKey": self.service_key,
                "pageNo": page_no,
                "numOfRows": self.num_of_rows,
                "inqryDiv": 1, # 1: 등록일시
                "type": "json",
                "inqryBgnDt": self.start_date,
                "inqryEndDt": self.end_date
            }

            response = requests.get(self.api_endpoints[category], params=params)
            if response.status_code != 200:
                print(f"❌ API 요청 실패! 상태 코드: {response.status_code}")
                break

            try:
                json_data = response.json()
            except requests.exceptions.JSONDecodeError:
                print("❌ JSON 디코딩 오류! 응답 확인 필요")
                break

            if "response" not in json_data or \
               "body" not in json_data["response"] or \
               "items" not in json_data["response"]["body"]:
                print(f"⚠️ {self.start_date[:8]} 데이터 없음!")
                break

            items = json_data["response"]["body"]["items"]
            if not isinstance(items, list):
                items = [items]

            filtered_items = [
                {
                    "bidNtceNo": item.get("bidNtceNo", ""),
                    "bidNtceOrd": item.get("bidNtceOrd", ""),
                    "bidNtceNm": item.get("bidNtceNm", ""),
                    "sucsfbidRate": item.get("sucsfbidRate", ""),
                    "rlOpengDt": item.get("rlOpengDt", "")
                } for item in items
            ]
            all_data.extend(filtered_items)

            if len(items) < self.num_of_rows:
                break
            page_no += 1

        if all_data:
            schema = StructType([
                StructField("bidNtceNo", StringType(), True),
                StructField("bidNtceOrd", StringType(), True),
                StructField("bidNtceNm", StringType(), True),
                StructField("sucsfbidRate", StringType(), True),
                StructField("rlOpengDt", StringType(), True),
            ])
            spark_df = self.spark.createDataFrame(all_data, schema=schema)
            yyyymmdd = self.start_date[:8]
            hdfs_filename = f"{self.hdfs_path}JoinTargets/{category}/SuccessfulBid_{category}.parquet"
            spark_df.write.mode("overwrite").parquet(hdfs_filename)
            print(f"✅ HDFS에 저장 완료: {hdfs_filename}")

if __name__ == "__main__":
    fetcher = BidDataFetcher()
    fetcher.fetch_all()
    print("🎉 수집 완료!")