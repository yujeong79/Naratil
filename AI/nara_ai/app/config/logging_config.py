import logging
from logging.handlers import TimedRotatingFileHandler
from datetime import datetime
import os
from pathlib import Path


BASE_LOG_DIR = Path("logs")
LOG_TO_FILE:bool =True

# 로거 생성
def create_logger(name: str, level) -> logging.Logger:
    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.propagate = False

    if logger.handlers:
        return logger

    if LOG_TO_FILE: # 로그를 파일로 저장할 때
        log_dir = os.path.join(BASE_LOG_DIR, name)
        os.makedirs(log_dir, exist_ok=True)
        date_str = datetime.now().strftime("%Y-%m-%d")
        log_file = os.path.join(log_dir, f"{name}-{date_str}.log")

        handler = TimedRotatingFileHandler(
            filename=str(log_file),
            when="midnight",
            interval=1,
            backupCount=7,
            encoding='utf-8'
        )
        handler.suffix = "%Y-%m-%d"
    else:
        handler = logging.StreamHandler()

    formatter = logging.Formatter("[%(asctime)s] [%(levelname)s] %(name)s - %(message)s")
    handler.setFormatter(formatter)
    handler.setLevel(level)

    logger.addHandler(handler)
    return logger

# 로거 설정
server_logger     = create_logger("server", logging.INFO)
embedding_logger  = create_logger("embedding", logging.INFO)
similarity_logger = create_logger("similarity", logging.INFO)
recommend_logger  = create_logger("recommend",  logging.INFO)
index_logger = create_logger("index", logging.INFO)
