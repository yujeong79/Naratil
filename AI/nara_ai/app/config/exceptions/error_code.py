from enum import Enum

class ErrorCode(Enum):

    # 400
    VALIDATION_ERROR = (40000, "요청 형식이 잘못되었습니다.")
    EMPTY_TEXT = (40001, "빈 텍스트입니다.")

    # 409
    STATE_CONFLICT = (40901, "현재 상태에서는 요청을 처리할 수 없습니다.")

    # 429
    TOO_MANY_REQUESTS = (42900, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")

    # 500
    INTERNAL_SERVER_ERROR = (50000, "서버 내부 오류입니다.")
    EMBEDDING_FAILED = (50010, "공고명 벡터화에 실패했습니다.")
    SIMILARITY_CALC_FAILED = (50020, "유사도 계산 중 오류가 발생했습니다.")


    def code(self) -> int:
        return self.value[0]

    def message(self) -> str:
        return self.value[1]

    def http_status(self) -> int:
        return self.code() // 100