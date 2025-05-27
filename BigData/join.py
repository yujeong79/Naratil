""" 공고번호를 기준으로 모든 parquet 파일을 join 하여 데이터 완성 """
import os
import pandas as pd
from datetime import datetime
from utils import get_spark_session, get_hdfs_path

class BidDataJoiner:
    def __init__(self):
        self.spark = get_spark_session("JoinAllData")
        self.hdfs_path = get_hdfs_path()
        self.categories = ["Thing", "ConstructionWork", "Service"]

    def join_data(self, category):
        print(f"📥 {category} Parquet 파일 불러오는 중...")
        df1 = self.spark.read.parquet(f"{self.hdfs_path}JoinTargets/{category}/SuccessfulBid_{category}.parquet")
        df2 = self.spark.read.parquet(f"{self.hdfs_path}JoinTargets/{category}/BidPublicNotice_{category}.parquet")
        df3 = self.spark.read.parquet(f"{self.hdfs_path}JoinTargets/{category}/LicenseLimit_{category}.parquet")
        df4 = self.spark.read.parquet(f"{self.hdfs_path}JoinTargets/{category}/OpeningComplete_{category}.parquet")
        df5 = self.spark.read.parquet(f"{self.hdfs_path}JoinTargets/{category}/PossibleRegion_{category}.parquet")

        print("🔗 DataFrame 병합 중 (기준: bidNtceNo)...")
        joined_df = df1.join(df2, "bidNtceNo", "outer")\
                      .join(df3, "bidNtceNo", "outer")\
                      .join(df4, "bidNtceNo", "outer")\
                      .join(df5, "bidNtceNo", "outer")

        joined_df.show(5)

        output_path = f"{self.hdfs_path}FinalData/{category}/Final_{category}.parquet"
        print(f"💾 저장 중: {output_path}")
        joined_df.write.mode("overwrite").parquet(output_path)
        print(f"✅ 저장 완료: {output_path}")

        print(f"pandas로 변환 중...")
        pdf = joined_df.toPandas()

        base_dir = os.path.dirname(os.path.abspath(__file__))
        csv_output_path = os.path.join(base_dir, "data", f"Final_{category}.csv")
 
        os.makedirs(os.path.dirname(csv_output_path), exist_ok=True)

        pdf.to_csv(csv_output_path, index=False, encoding="utf-8-sig")

        print(f"csv 저장 완료: {csv_output_path}")

    def run(self):
        for category in self.categories:
            print(f"📦 {category} 병합 시작")

            self.join_data(category)

        self.spark.stop()
        print("🎉 모든 카테고리 병합 완료")

if __name__ == "__main__":
    joiner = BidDataJoiner()
    joiner.run()
