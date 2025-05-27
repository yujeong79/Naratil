from fastapi import Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.config.exceptions.error_code import ErrorCode
from app.config.logging_config import server_logger as logger


async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    detail = exc.detail

    if isinstance(detail, dict) and "code" in detail:
        code = detail["code"]
        message = detail["detail"]
    else:
        code = str(exc.status_code * 100)
        message = str(detail)

    logger.warning(f"[HTTP {exc.status_code}] {request.url} - {message}")

    return JSONResponse(
        status_code=exc.status_code,
        content={"code": code, "detail": message}
    )


async def validation_exception_handler(request: Request, exc: RequestValidationError):
    err = ErrorCode.VALIDATION_ERROR
    logger.warning(f"[400] {request.url} - Validation: {exc.errors()}")

    return JSONResponse(
        status_code=err.http_status(),
        content={"code": err.code(), "detail": err.message()}
    )


async def global_exception_handler(request: Request, exc: Exception):
    err = ErrorCode.INTERNAL_SERVER_ERROR
    logger.error(f"[500] {request.url} - 예외: {repr(exc)}", exc_info=True)

    return JSONResponse(
        status_code=err.http_status(),
        content={"code": err.code(), "detail": err.message()}
    )
