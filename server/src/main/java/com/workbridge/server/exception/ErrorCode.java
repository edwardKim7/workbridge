package com.workbridge.server.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청값을 확인해 주세요."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "요청 본문 형식을 확인해 주세요."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "이미 사용되었거나 폐기된 갱신 토큰입니다."),
    MOBILE_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "모바일 승인 업무 권한이 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부서 정보를 찾을 수 없습니다."),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "직원 정보를 찾을 수 없습니다."),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "역할 정보를 찾을 수 없습니다."),
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "고객 정보를 찾을 수 없습니다."),
    OPPORTUNITY_NOT_FOUND(HttpStatus.NOT_FOUND, "영업기회 정보를 찾을 수 없습니다."),
    SALES_ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "영업활동 정보를 찾을 수 없습니다."),
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "결재 정보를 찾을 수 없습니다."),
    INTERFACE_NOT_FOUND(HttpStatus.NOT_FOUND, "인터페이스 처리 이력을 찾을 수 없습니다."),
    MIGRATION_BATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터 이관 배치를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 요청 형식입니다."),
    CONFLICT(HttpStatus.CONFLICT, "현재 상태에서는 요청을 처리할 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 등록된 값이 존재합니다."),
    INVALID_BUSINESS_REGISTRATION_NO(HttpStatus.BAD_REQUEST, "사업자번호는 숫자 10자리로 입력해 주세요."),
    DUPLICATE_BUSINESS_REGISTRATION_NO(HttpStatus.CONFLICT, "이미 등록된 사업자번호입니다."),
    INVALID_SALES_ASSIGNEE(HttpStatus.BAD_REQUEST, "활성 SALES 역할 직원을 담당자로 지정해 주세요."),
    INVALID_OPPORTUNITY_PROBABILITY(HttpStatus.BAD_REQUEST, "영업단계와 수주확률의 조합을 확인해 주세요."),
    INVALID_OPPORTUNITY_TRANSITION(HttpStatus.CONFLICT, "종결된 영업기회는 다른 단계로 변경할 수 없습니다."),
    ACTIVITY_TARGET_MISMATCH(HttpStatus.BAD_REQUEST, "영업활동의 고객과 영업기회 고객이 일치하지 않습니다."),
    CUSTOMER_INACTIVE(HttpStatus.CONFLICT, "비활성 고객의 영업기회와 영업활동은 변경할 수 없습니다."),
    INVALID_APPROVAL_TRANSITION(HttpStatus.CONFLICT, "현재 결재 상태에서는 요청한 처리를 할 수 없습니다."),
    APPROVAL_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리되었거나 다른 사용자가 먼저 처리한 결재입니다."),
    APPROVAL_SELF_PROCESSING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "요청자와 승인자는 동일할 수 없습니다."),
    APPROVAL_REJECTION_OPINION_REQUIRED(HttpStatus.BAD_REQUEST, "반려 의견을 입력해 주세요."),
    APPROVAL_CANCEL_NOT_ALLOWED(HttpStatus.FORBIDDEN, "결재 요청자만 초안을 취소할 수 있습니다."),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "다른 사용자가 먼저 수정했습니다. 최신 정보를 다시 조회해 주세요."),
    DEPARTMENT_IN_USE(HttpStatus.CONFLICT, "활성 직원 또는 하위 부서가 있어 부서를 비활성화할 수 없습니다."),
    LAST_ACTIVE_ADMIN_REQUIRED(HttpStatus.CONFLICT, "시스템에는 활성 관리자 계정이 한 명 이상 필요합니다."),
    PROTECTED_ADMIN_PERMISSION(HttpStatus.CONFLICT, "관리자 권한 관리 메뉴는 읽기·쓰기 권한을 유지해야 합니다."),
    INVALID_MENU_PERMISSION(HttpStatus.BAD_REQUEST, "메뉴 권한 구성을 확인해 주세요."),
    ERP_TEMPORARY_FAILURE(HttpStatus.BAD_GATEWAY, "외부 ERP가 일시적으로 응답하지 않습니다."),
    ERP_PERMANENT_FAILURE(HttpStatus.UNPROCESSABLE_ENTITY, "외부 ERP가 요청을 처리할 수 없습니다."),
    INTERFACE_DATA_CHANGED(HttpStatus.CONFLICT, "최초 전송 이후 업무 데이터가 변경되어 같은 이력을 재처리할 수 없습니다."),
    INTERFACE_ALREADY_SUCCEEDED(HttpStatus.CONFLICT, "이미 성공한 인터페이스입니다."),
    INVALID_MIGRATION_FILE(HttpStatus.BAD_REQUEST, "CSV 파일 형식 또는 내용을 확인해 주세요."),
    DUPLICATE_MIGRATION_FILE(HttpStatus.CONFLICT, "동일한 내용의 이관 파일이 이미 완료 처리되었습니다."),
    MIGRATION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 데이터 이관 배치입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다."),
    DATABASE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "데이터베이스 연결 상태를 확인해 주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
