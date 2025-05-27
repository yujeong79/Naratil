#!/bin/bash

echo "[INFO] Waiting for HDFS to be ready..."
sleep 30

# 최대 30초 대기: Safe mode 해제 대기
#for i in {1..30}; do
  #hdfs dfsadmin -safemode get 2>&1 | grep -q "OFF" && break
  #echo "[INFO] HDFS is still in safe mode... waiting ($i)"
  #sleep 1
#done

#echo "[INFO] Forcing leave of safe mode..."
#hdfs dfsadmin -safemode leave

echo "[INFO] Running Spark job..."
/opt/bitnami/spark/bin/spark-submit /app/scripts/main.py

echo "[INFO] Running Spark job: main_daily.py"
/opt/bitnami/spark/bin/spark-submit /app/scripts/main_daily.py

echo "[INFO] All Spark jobs completed."

