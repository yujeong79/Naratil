from fastapi import HTTPException
from app.config.exceptions.error_code import ErrorCode
from typing import Callable, Type


def http_error(
    err: ErrorCode,
    logger: Callable[..., None] = None,
    message: str = None,
    exc: Exception = None,
) -> HTTPException:
    """
    HTTPException 생성 + 로깅
    """
    log_msg = message or err.message()

    if logger:
        if exc:
            logger(f"{log_msg} | {type(exc).__name__}: {exc}", exc_info=True)
        else:
            logger(log_msg)

    return HTTPException(
        status_code=err.http_status(),
        detail={"code": err.code(), "detail": err.message()}
    )


def raise_error(
    err: ErrorCode,
    logger: Callable[..., None] = None,
    log_msg: str = None,
    exc: Exception = None,
    ex_type: Type[Exception] = RuntimeError
):
    """
    일반 Exception 발생 + 로깅
    - ValueError, RuntimeError 등
    """
    message = log_msg or err.message()

    if logger:
        if exc:
            logger(f"{message} | {type(exc).__name__}: {exc}", exc_info=True)
        else:
            logger(message)

    raise ex_type(message)
