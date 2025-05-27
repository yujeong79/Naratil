""" Spark, HDFS, API 설정 유틸 """
import os
from pyspark.sql import SparkSession
from hdfs import InsecureClient

# 환경변수에서 설정값 읽기 (기본값 포함)
HDFS_URL = os.getenv("HDFS_URL", "http://namenode:9870")
HDFS_PATH = os.getenv("HDFS_PATH", "hdfs://namenode:8020/user/hadoop/data/")
SERVICE_KEY = os.getenv("SERVICE_KEY", "/gvcUmBTJGzMCh8leGPHTtlWNYo5uvIwbk5G598pcQDC/BpahcQcnDXXxQxFZsuVd/HuKuTeEbdbc1ZxX4FYyw==")

# SparkSession 반환 (appName 설정 가능)
def get_spark_session(app_name="DefaultApp"):
    spark = SparkSession.builder \
        .appName(app_name) \
        .master(os.getenv("SPARK_MASTER_URL", "local[*]")) \
        .config("spark.driver.memory", os.getenv("SPARK_DRIVER_MEMORY", "2g")) \
        .config("spark.executor.memory", os.getenv("SPARK_EXECUTOR_MEMORY", "4g")) \
        .config("spark.executor.cores", os.getenv("SPARK_EXECUTOR_CORES", "2")) \
        .getOrCreate()
    return spark

# HDFS 클라이언트 반환
def get_hdfs_client():
    return InsecureClient(HDFS_URL)

# HDFS 경로 반환
def get_hdfs_path():
    return HDFS_PATH

# API 서비스 키 반환
def get_service_key():
    return SERVICE_KEY
