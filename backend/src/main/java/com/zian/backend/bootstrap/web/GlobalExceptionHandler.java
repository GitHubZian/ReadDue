package com.zian.backend.bootstrap.web;

import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.ResultUtils;
import com.zian.backend.shared.exception.BusinessException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器。
 *
 * <p>统一出口：响应体 {@code BaseResponse} + 与业务码语义一致的 HTTP 状态码
 * （见 {@link ErrorCode#statusOf(int)}）。前端 axios 依赖 401 触发重新登录。
 */
@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    // ==================== 参数校验 ====================

    /**
     * RequestBody + @Valid 校验失败（最常用）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> methodArgumentNotValidHandler(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, msg));
    }

    /**
     * 表单/查询参数绑定校验失败（非 @RequestBody 场景）
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<?>> bindExceptionHandler(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", msg);
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, msg));
    }

    /**
     * RequestParam / @PathVariable 上的约束校验失败（需配合 @Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> constraintViolationHandler(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", msg);
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, msg));
    }

    /**
     * 请求体 JSON 解析失败（字段类型不匹配、格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<?>> httpMessageNotReadableHandler(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求体格式错误"));
    }

    /**
     * 参数类型不匹配（如路径变量应为 Long 却传了字符串）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<?>> typeMismatchHandler(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: name={}, value={}", e.getName(), e.getValue());
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数 " + e.getName() + " 类型不正确"));
    }

    /**
     * 缺少请求头（如 Authorization / Idempotency-Key）
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<BaseResponse<?>> missingRequestHeaderHandler(MissingRequestHeaderException e) {
        log.warn("缺少请求头: {}", e.getHeaderName());
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, "缺少请求头: " + e.getHeaderName()));
    }

    /**
     * 缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<?>> missingParamHandler(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, "缺少参数: " + e.getParameterName()));
    }

    /**
     * 请求方法不支持（如用 GET 调 POST 接口）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<?>> methodNotSupportedHandler(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMethod());
        return build(ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求方法不支持: " + e.getMethod()));
    }

    // ==================== 安全 ====================

    /**
     * 未登录 / token 失效（过滤器链外抛出时兜底）
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<?>> authenticationHandler(AuthenticationException e) {
        log.warn("未认证: {}", e.getMessage());
        return build(ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR));
    }

    /**
     * @PreAuthorize 拒绝：已登录但缺少权限码
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<?>> accessDeniedHandler(AccessDeniedException e) {
        log.warn("越权访问: {}", e.getMessage());
        return build(ResultUtils.error(ErrorCode.NO_AUTH_ERROR));
    }

    // ==================== 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> businessExceptionHandler(BusinessException e) {
        log.warn("BusinessException: code={}, msg={}", e.getCode(), e.getMessage());
        return build(ResultUtils.error(e.getCode(), e.getMessage()));
    }

    /**
     * 唯一索引冲突（如重复学号、重复借阅）→ 409
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<BaseResponse<?>> duplicateKeyHandler(DuplicateKeyException e) {
        log.warn("唯一键冲突: {}", e.getMessage());
        return build(ResultUtils.error(ErrorCode.CONFLICT_ERROR, "数据已存在或已被并发修改"));
    }

    // ==================== 兜底 ====================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return build(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误"));
    }

    private ResponseEntity<BaseResponse<?>> build(BaseResponse<?> body) {
        return ResponseEntity.status(ErrorCode.statusOf(body.getCode())).body(body);
    }
}
