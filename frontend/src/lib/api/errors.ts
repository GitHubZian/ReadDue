export interface ApiErrorBody {
  code: number;
  message: string;
  traceId?: string;
  details?: Record<string, unknown>;
}

export class ApiError extends Error {
  readonly code: number;
  readonly traceId?: string;
  readonly details?: Record<string, unknown>;
  readonly status?: number;

  constructor(body: ApiErrorBody, status?: number) {
    super(body.message);
    this.name = "ApiError";
    this.code = body.code;
    this.traceId = body.traceId;
    this.details = body.details;
    this.status = status;
  }

  /** 后端约定：401 未登录 / 403 无权限 / 409 冲突 / 422 校验失败。 */
  get isAuthError() {
    return this.status === 401 || this.code === 401;
  }

  get isForbidden() {
    return this.status === 403 || this.code === 403;
  }

  /** 409：并发冲突（如规则版本被他人更新）。 */
  get isConflict() {
    return this.status === 409 || this.code === 40900;
  }

  /** 422：业务规则拒绝（如读者证号不存在、宽限天数超限）。 */
  get isValidation() {
    return this.status === 422 || this.code === 42200;
  }
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;
  if (error instanceof Error) {
    return new ApiError({ code: -1, message: error.message });
  }
  return new ApiError({ code: -1, message: "网络异常，请稍后重试" });
}
