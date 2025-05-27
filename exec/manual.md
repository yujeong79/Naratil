# 📌 배포 가이드

### 대상 환경

- EC2 서버 2대 구성
    - **EC2 (Frontend + API 서버 + Batch 서버 + DB)**
        - OS: Ubuntu 22.04
        - 퍼블릭 IP: 3.36.76.231
        - 역할: Nginx, Vue frontend, Spring Boot API, Spring Batch, MySQL, MongoDB
    - **EC2 (Spark + HDFS)**
        - OS: Ubuntu 22.04
        - 퍼블릭 IP: 52.78.63.130
        - 역할: Spark 작업, Hadoop NameNode/DataNode

### 필수 패키지 설치

```bash
pip install -r requirements.txt
```

### 프로젝트 클론 및 실행 준비

```bash
### 🔹 서버 1 (3.36.76.231)
git clone https://lab.ssafy.com/s12-bigdata-dist-sub1/S12P21A506
cd S12P21A506

### 🔹 서버 2 (52.78.63.130)
git clone https://lab.ssafy.com/s12-bigdata-dist-sub1/S12P21A506
cd S12P21A506/BigData
```

### 환경 변수 설정 - 서버 1 (3.36.76.231)

- `Frontend/.env`
    
    ```bash
    VITE_APP_TITLE=나라장터 입찰정보 빅데이터 플랫폼
    VITE_APP_PUBLIC_DATA_API_KEY=YOUR_PUBLIC_DATA_API_KEY
    VITE_APP_API_BASE_URL=https://j12a506.p.ssafy.io/api
    VITE_PUBLIC_DATA_SERVICE_KEY=FEmEpZ7W6QZ86IG2Ii9B3/aJXBuQL2upJWGmJoeEPJAEZykbjjAhiR08fbnbIvsRxRsxNauT9FFn+nQcE8XMqw==
    ```
    
- `batch/application.yml`
    
    ```bash
    spring:
  application:
    name: Naratil Batch

  batch:
    jdbc:
      initialize-schema: never  # 스프링 배치 초기화 정책
    job:
      enabled: false             # 자동 배치 실행 비활성화

  datasource:
    batch:
      jdbc-url: jdbc:mysql://3.36.76.231:3308/batchdb?useSSL=false&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
      username: myuser
      password: mypassword
      driver-class-name: com.mysql.cj.jdbc.Driver

    business:
      jdbc-url: jdbc:mysql://3.36.76.231:3307/apidb?useSSL=false&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
      username: myuser
      password: mypassword
      driver-class-name: com.mysql.cj.jdbc.Driver

    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  data:
    mongodb:
      uri: mongodb://3.36.76.231:27017/naratil
      database: naratil

- `batch/application.properties`
    ```
    spring.application.name=naratil

    server.port=8080

    # MySQL
    spring.datasource.url=jdbc:mysql://j12a506.p.ssafy.io:3307/apidb?useSSL=false&serverTimezone=UTC
    spring.datasource.username=myuser
    spring.datasource.password=mypassword
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

    # JPA
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

    spring.jpa.hibernate.ddl-auto=none
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true

    # log level WARN > INFO
    logging.level.root: INFO

    # SQL


    # MongoDB
    spring.data.mongodb.uri=mongodb://j12a506.p.ssafy.io:27017
    spring.data.mongodb.database=naratil

    # Hibernate SQL
    spring.jpa.properties.hibernate.generate_statistics=false
    spring.jpa.properties.hibernate.use_sql_comments=true



    logging.level.org.hibernate.SQL=OFF
    logging.level.org.hibernate.type.descriptor.sql=OFF

    # JWT
    jwt.access-token-secret=your_very_long_access_token_secret_here_32_bytes_or_more

    jwt.refresh-token-secret=your_refresh_token_secret
    jwt.access-token-expiration=1800000
    jwt.refresh-token-expiration=604800000

  

    public-data-api:
    #  service-key: JVKsNwUuO8MwiXsdGgfzudKwGbMCxGQDlytz6wR5EyeMs/Ud4jddXTEWPOvmXfH0ZK23O59aB4oOO2GPfECrFA==
    service-key: Yc1sqYmFYTOaFpM5Z7/LBwgIyJnyLUXZY/Z2x6qrA+K00x5ohHijdSJsNNWfYrO44CL2xkkWMykdCYnJRbwLYg==
    springdoc:
    api-docs:
        path: /api-docs
    swagger-ui:
        path: /swagger-ui.html
        operations-sorter: method
        tags-sorter: alpha
        display-request-duration: true
    packages-to-scan: com.naratil.batch.controller


    # FAST API
    fastapi:
    base-url: http://localhost:8000/batch/fastapi
    api:
        industry-vector: /embedding/industry_vectors
        ntce-vector: /embedding/ntce_vectors
        ntce-similarity: /similarity/ntces
        ntce-recommend: /recommend/ntces


    logging:
    level:
        sun:
        rmi: warn
        javax:
        management: warn
        org.springframework.jdbc.core: ERROR
        org.springframework.jdbc.datasource: ERROR
        org.mongodb.drive.cluster: ERROR
        com.zaxxer.hikari.pool.HikariPool: ERROR
        o.s.d.mongodb.MongoTransactionManager: ERROR
        org.springframework.batch: INFO
        root: INFO
    #  file:
    #    name: logs/batch.log

    server:
    port: 8081
    tomcat:
        apr:
        enabled: false


    ```
    

    

### 환경 변수 설정 - 서버 1 (52.78.63.130)

- `Bigdata/.env`
    
    ```bash
    HDFS_URL=
    HDFS_PATH=
    SERVICE_KEY=
    SPARK_MASTER_URL=
    SPARK_DRIVER_MEMORY=
    SPARK_EXECUTOR_MEMORY=
    SPARK_EXECUTOR_CORES=
    ```
    

### 애플리케이션 실행

```bash
docker compose up --build -d

### 🔹 서버 1 (3.36.76.231)
# 서비스 | 이름 | 설명 |	포트
# nginx |	정적 파일 서빙 및 리버스 프록시	| 80, 443
# api-server | Spring Boot 기반 API 서버	| 8080
# batch-server	| Spring Boot 기반 배치 처리 서버	| 8081
# fastapi	| Python FastAPI 기반 AI 추천 서버	| 8000
# mysql-api	| api-server 전용 MySQL DB (apidb)	| 3307:3306
# mysql-batch	| batch-server 전용 MySQL DB (batchdb)	| 3308:3306
# mongo	| 공통 MongoDB 서버	| 27017

### 🔹 서버 2 (52.78.63.130)
# 서비스 | 이름 | 설명 |	포트
# namenode |	HDFS의 NameNode 역할 (파일 메타데이터 관리)	| 9870(HDFS UI), 8020(HDFS 접근)
# datanode | HDFS의 DataNode 역할 (실제 데이터 저장)
# spark | PySpark 작업 실행용 Spark 컨테이너
```

```
docker-compose up -d --build
```

---


# 📌 DB

🔑 사용자 및 권한 설정 (init-user.sql)

CREATE USER 'myuser'@'%' IDENTIFIED BY 'myuser';
GRANT ALL PRIVILEGES ON *.* TO 'myuser'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

🗃️ 데이터베이스 스키마 (schema.sql)
```
CREATE TABLE `corporation` (
  `corpid` bigint NOT NULL AUTO_INCREMENT,
  `bizno` varchar(10) NOT NULL,
  `corp_nm` varchar(100) NOT NULL,
  `ceo_nm` varchar(35) DEFAULT NULL,
  `emplye_num` varchar(10) DEFAULT NULL,
  `opbiz_dt` date DEFAULT NULL,
  `industry_id` bigint DEFAULT NULL,
  PRIMARY KEY (`corpid`),
  UNIQUE KEY `UKktf6f0nweev8yp7vyjxt22qgd` (`bizno`),
  KEY `FKqjmhfb8g5dyt1mh5chgrhxw8l` (`industry_id`),
  CONSTRAINT `FKqjmhfb8g5dyt1mh5chgrhxw8l` FOREIGN KEY (`industry_id`) REFERENCES `industry` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `industry` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `industry_code` bigint NOT NULL,
  `industry_name` varchar(200) NOT NULL,
  `category_code` bigint NOT NULL,
  `category_name` varchar(255) NOT NULL,
  `major_category_code` bigint NOT NULL,
  `major_category_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `industry_code_UNIQUE` (`industry_code`),
  KEY `idx_industry_category_code` (`category_code`),
  KEY `idx_industry_major_category_code` (`major_category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(100) NOT NULL,
  `phone_number` varchar(11) DEFAULT NULL,
  `corpid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`),
  UNIQUE KEY `idx_user_email_password` (`email`,`password`),
  KEY `idx_user_corpid` (`corpid`),
  CONSTRAINT `FKcxh7u1bnv9w6dwduxsu4n42hu` FOREIGN KEY (`corpid`) REFERENCES `corporation` (`corpid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `recommend` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `corpid` bigint NOT NULL,
  `bid_ntce_id` bigint NOT NULL,
  `score` float NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_recommend_corpid_bid_ntce_id` (`corpid`,`bid_ntce_id`),
  CONSTRAINT `FKdp0ym32pbecr9weys1li60upb` FOREIGN KEY (`corpid`) REFERENCES `corporation` (`corpid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `favorite_bid` (
  `fav_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `bid_ntce_id` bigint NOT NULL,
  PRIMARY KEY (`fav_id`),
  UNIQUE KEY `idx_favorite_bid_user_id_bid_ntce_id` (`user_id`,`bid_ntce_id`),
  CONSTRAINT `FK3ja3q5ydbmlvir2ky2ajxykoi` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

```

# 📌 버전

## FE
```
프레임워크 : Vue.js
런타임 환경 : Node 20
상태 관리 : Pinia 
빌드 도구 : Vite 
패키지 관리 : npm 
CSS/스타일링 : PrimeVue 4.2.5 , Tailwind CSS 3.4.17
데이터 시각화 : Chart.js 
IDE : VS CODE 최신
```

## BE
```
언어: Java 17 , Python 3.11 
프레임워크: Spring Boot 3.4.3, FastAPI 0.45.0
데이터베이스: MySQL 8.0, MongoDB 8.0.5
ORM: JPA 
API: REST API (OpenAPI Documentation using Springdoc) 
컴파일 및 빌드: Gradle 8-jdk17 
배포: Docker , AWS EC2 
```

## AI
```
Fast API : 0.115.12
Python 3.11.7
```

## DATA
```
hadoop 3.2.2
jdk-8
spark 3.4.2
pyspark 3.4.2
pymongo, numpy, pandas, sentence-transformer
```

# 실행

## FE
1. 모듈 설치 및 실행
```
npm install
npm run dev
```


## AI

1. 가상환경 생성 및 패키지 설치
```
python3 -m venv .venv
source .venv/bin/activate       # Windows는 .venv\Scripts\activate
pip install -r requirements.txt
```
2. FastAPI 서버 실행
```
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## DATA
```

# 1. 작업 디렉토리로 이동 (홈 디렉토리 사용)
cd ~

# 2. 디스크 공간 확인
df -h

# 3. 스파크 3.4.2 다운로드 및 설치 (/opt 디렉토리에 설치)
cd /opt
sudo wget https://archive.apache.org/dist/spark/spark-3.4.2/spark-3.4.2-bin-hadoop3.tgz
sudo tar -xvzf spark-3.4.2-bin-hadoop3.tgz
sudo mv spark-3.4.2-bin-hadoop3 spark
sudo rm spark-3.4.2-bin-hadoop3.tgz

# 4. 디스크 공간이 충분한 위치 확인 (여유 공간이 많은 디렉토리로 대체 가능)
# 아래 예시는 홈 디렉토리 사용 (필요시 /mnt/data 등으로 변경)
WORK_DIR=~/Project
mkdir -p $WORK_DIR
cd $WORK_DIR

# 5. 환경변수 설정 (스파크 홈 및 경로 설정)
echo 'export SPARK_HOME=/opt/spark' >> ~/.bashrc
echo 'export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin' >> ~/.bashrc
echo 'export PYSPARK_PYTHON=python3' >> ~/.bashrc

# 6. 커스텀 캐시 위치 설정 (디스크 공간 문제 방지)
echo 'export TRANSFORMERS_CACHE=~/Project/model_cache' >> ~/.bashrc
echo 'export HF_HOME=~/Project/model_cache' >> ~/.bashrc
echo 'export PIP_CACHE_DIR=~/Project/pip_cache' >> ~/.bashrc

# 7. 환경변수 적용
source ~/.bashrc

# 8. 캐시 디렉토리 생성
mkdir -p ~/Project/model_cache
mkdir -p ~/Project/pip_cache

# 9. 가상환경 생성 및 활성화
mkdir -p ~/Project/venv
python3 -m venv ~/Project/venv
source ~/Project/venv/bin/activate

# 10. 필요한 Python 패키지 설치 (캐시 최소화)
pip install --no-cache-dir --upgrade pip
pip install --no-cache-dir pyspark==3.4.2
pip install --no-cache-dir sentence-transformers // 여기서 no space 에러남, 디스크 16G 이미 full
pip install --no-cache-dir numpy pandas scikit-learn pyarrow sshtunnel pymongo
```