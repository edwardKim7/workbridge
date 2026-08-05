package com.workbridge.server.exception;

import com.workbridge.server.api.common.ApiErrorResponse;
import com.workbridge.server.api.common.FieldErrorDetail;
import com.workbridge.server.audit.MaskingUtils;
import com.workbridge.server.audit.TraceIdContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.workbridge.server.service.operation.SystemErrorLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private SystemErrorLogService systemErrorLogService;

    @Autowired(required = false)
    void setSystemErrorLogService(SystemErrorLogService systemErrorLogService) {
        this.systemErrorLogService = systemErrorLogService;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = exception.getErrorCode();
        return build(errorCode, exception.getMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return build(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDefaultMessage(),
                toFieldErrors(exception.getBindingResult().getFieldErrors()), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
            BindException exception,
            HttpServletRequest request
    ) {
        return build(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDefaultMessage(),
                toFieldErrors(exception.getBindingResult().getFieldErrors()), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new FieldErrorDetail(
                        violation.getPropertyPath().toString(),
                        MaskingUtils.maskRejectedValue(violation.getPropertyPath().toString(), violation.getInvalidValue()),
                        violation.getMessage()
                ))
                .toList();
        return build(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDefaultMessage(), fieldErrors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return build(ErrorCode.MALFORMED_REQUEST, ErrorCode.MALFORMED_REQUEST.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleParameterError(Exception exception, HttpServletRequest request) {
        return build(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return build(ErrorCode.AUTHENTICATION_REQUIRED, ErrorCode.AUTHENTICATION_REQUIRED.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        if (!request.getRequestURI().substring(request.getContextPath().length()).startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, request.getContextPath() + "/access-denied")
                    .build();
        }
        return build(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return build(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        ResponseEntity<ApiErrorResponse> response = build(
                ErrorCode.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED.getDefaultMessage(),
                Collections.emptyList(),
                request
        );
        HttpHeaders headers = new HttpHeaders();
        if (exception.getSupportedHttpMethods() != null) {
            headers.setAllow(exception.getSupportedHttpMethods());
        }
        return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return build(ErrorCode.FILE_SIZE_EXCEEDED, ErrorCode.FILE_SIZE_EXCEEDED.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<ApiErrorResponse> handleCannotCreateTransaction(
            CannotCreateTransactionException exception,
            HttpServletRequest request
    ) {
        log.error("Database transaction could not be started. traceId={}, method={}, path={}",
                TraceIdContext.currentTraceId(), request.getMethod(), request.getRequestURI(), exception);
        return build(ErrorCode.DATABASE_UNAVAILABLE, ErrorCode.DATABASE_UNAVAILABLE.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.warn("Data integrity constraint rejected request. traceId={}, method={}, path={}",
                TraceIdContext.currentTraceId(), request.getMethod(), request.getRequestURI());
        return build(ErrorCode.DUPLICATE_RESOURCE, ErrorCode.DUPLICATE_RESOURCE.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccessException(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        log.error("Database access failed. traceId={}, method={}, path={}",
                TraceIdContext.currentTraceId(), request.getMethod(), request.getRequestURI(), exception);
        return build(ErrorCode.DATABASE_UNAVAILABLE, ErrorCode.DATABASE_UNAVAILABLE.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception. traceId={}, method={}, path={}",
                TraceIdContext.currentTraceId(), request.getMethod(), request.getRequestURI(), exception);
        return build(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
                Collections.emptyList(), request);
    }

    private List<FieldErrorDetail> toFieldErrors(List<FieldError> errors) {
        return errors.stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        MaskingUtils.maskRejectedValue(error.getField(), error.getRejectedValue()),
                        error.getDefaultMessage() == null ? "유효하지 않은 값입니다." : error.getDefaultMessage()
                ))
                .toList();
    }

    private ResponseEntity<ApiErrorResponse> build(
            ErrorCode errorCode,
            String message,
            List<FieldErrorDetail> fieldErrors,
            HttpServletRequest request
    ) {
        HttpStatus status = errorCode.getHttpStatus();
        ApiErrorResponse response = ApiErrorResponse.of(
                status.value(),
                errorCode.name(),
                message,
                fieldErrors,
                request.getRequestURI()
        );
        if (systemErrorLogService != null && request.getRequestURI().contains("/api/")) {
            systemErrorLogService.safeRecord(request, status.value(), errorCode.name(), message, null);
        }
        return ResponseEntity.status(status).body(response);
    }
}
