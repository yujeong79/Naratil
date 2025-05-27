# Hadoop 학습 및 실습

## 1️⃣ Linux에서 MapReduce 환경 설정

- 데이터 생성 및 코딩은 Linux 환경에서 수행하며, 이후 MapReduce 코드와 입력 데이터는 HDFS (Hadoop Distributed File System) 에 저장하여 실행.

- Unix(Linux) 디렉토리 구조:
    - src/ : MapReduce 코드 저장
    - data/ : 입력 데이터 저장
    - build.xml : 컴파일을 위한 파일
- HDFS 디렉토리 구조:
- wordcount_test/ : MapReduce 실행을 위한 데이터 디렉토리
- wordcount_test_out/ : 실행 결과 저장 디렉토리

## 2️⃣ Hadoop 실습 환경 구성

- Hadoop 실습을 위한 소스코드는 /home/Project/src에 설치됨.

- Driver.java 수정:
    - pgd.addClass("wordcount", Wordcount.class, "A map/reduce program that perform word counting.");
    - src 디렉토리 내에 새로운 코드 추가 시, Driver.java 파일에 pgd.addClass를 새로 추가해야 함.
    - Driver.java 수정 후 ant 빌드 필수.
    - 실행 명령 예시: 
        - hadoop jar ssafy.jar wordcount wordcount_test wordcount_test_out


### 5️⃣ HDFS 디렉토리 생성 및 관리
- Hadoop 계정에서 HDFS 디렉토리 확인:
    - hdfs dfs -ls /
- HDFS 디렉토리 생성:
    hdfs dfs -mkdir /user
    hdfs dfs -mkdir /user/hadoop
    - 생성 후 ls 명령어로 확인.