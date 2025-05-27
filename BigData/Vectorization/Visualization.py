from pyspark.sql import SparkSession
import matplotlib.pyplot as plt
plt.rcParams['font.family'] = 'NanumGothic'

# Spark 세션 생성
spark = SparkSession.builder \
    .appName("Visualize lcnsLmtNm Distribution") \
    .getOrCreate()

# HDFS의 parquet 파일 경로
#parquet_path = "hdfs://localhost:9000/user/hadoop/data/FinalData/Thing/Final_Thing_202410.parquet"


paths = [
"hdfs://localhost:9000/user/hadoop/data/FinalData/Thing/Final_Thing_202410.parquet",
"hdfs://localhost:9000/user/hadoop/data/FinalData/Thing/Final_Thing_202411.parquet",
"hdfs://localhost:9000/user/hadoop/data/FinalData/Thing/Final_Thing_202412.parquet"

]
# parquet 파일 읽기
df = spark.read.parquet(*paths)

df = df.filter(df['lcnsLmtNm'].isNotNull()) \
	.filter(df['lcnsLmtNm'] != 'NaN')

# 분포 집계
grouped_df = df.groupBy("lcnsLmtNm").count().orderBy("count", ascending=False)

# pandas로 변환
pdf = grouped_df.toPandas()
#pdf = pdf.dropna(subset=['lcnsLmtNm'])
#pdf = pdf[pdf['lcnsLmtNm'] != 'NaN']

# 파이차트
plt.figure(figsize=(8, 8))
plt.pie(pdf['count'], labels=pdf['lcnsLmtNm'], autopct='%1.1f%%', startangle=140)
plt.title("lcnsLmtNm-pie")
plt.axis('equal')  # 원형 유지
plt.tight_layout()
plt.savefig("Total_dis_pie.png")

# 바그래프
plt.figure(figsize=(10, 6))
plt.bar(pdf['lcnsLmtNm'], pdf['count'])
plt.xticks(rotation=45, ha='right')
plt.xlabel("lcnsLmtNm")
plt.ylabel("Count")
plt.title("lcnsLmtNm-bar")
plt.tight_layout()
plt.savefig("Total_bid_dis_bar.png")