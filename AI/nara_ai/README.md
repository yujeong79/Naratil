# 1. 가상환경 생성 및 패키지 설치
- python3 -m venv .venv
- source .venv/bin/activate       # Windows는 .venv\Scripts\activate
- pip install -r requirements.txt

# 2. 환경 변수 파일 (.env) 설정
- 루트 디렉토리에 .env 파일 작성 (불필요)

# 3. FastAPI 서버 실행
- uvicorn app.main:app --host 0.0.0.0 --port 8000