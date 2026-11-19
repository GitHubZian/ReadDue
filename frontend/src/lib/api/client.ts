import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig } from "axios";
import { toast } from "sonner";
import { clearSession, getToken } from "@/lib/auth/session";
import { ApiError } from "./errors";
import type { BaseResponse } from "./types";

const baseURL = import.meta.env.VITE_API_BASE_URL || "/api/v1";

const WRITE_METHODS = new Set(["POST", "PUT", "PATCH", "DELETE"]);

const traceId = () =>
  typeof crypto !== "undefined" && "randomUUID" in crypto
    ? crypto.randomUUID()
    : `trace-${Date.now()}-${Math.random().toString(16).slice(2)}`;

/**
 * 同一写操作（方法 + 地址 + 请求体）在成功前复用同一个 Idempotency-Key：
 * 网络失败后重试、或用户再点一次，都不会被后端当成第二笔业务。
 */
const pendingKeys = new Map<string, string>();
const MAX_PENDING_KEYS = 200;

interface IdempotentConfig extends InternalAxiosRequestConfig {
  idempotencySignature?: string;
}

function signatureOf(config: AxiosRequestConfig) {
  let body = "";
  try {
    body = typeof config.data === "string" ? config.data : JSON.stringify(config.data ?? null);
  } catch {
    body = "";
  }
  return `${(config.method ?? "get").toUpperCase()} ${config.url ?? ""} ${body}`;
}

const http = axios.create({ baseURL, timeout: 20_000 });

http.interceptors.request.use((config) => {
  const token = getToken();
  config.headers.set("X-Trace-Id", traceId());
  config.headers.set("X-Client", "readdue-admin");
  if (token) config.headers.set("Authorization", `Bearer ${token}`);
  const method = (config.method ?? "get").toUpperCase();
  if (WRITE_METHODS.has(method)) {
    const signature = signatureOf(config);
    let key = pendingKeys.get(signature);
    if (!key) {
      key = traceId();
      pendingKeys.set(signature, key);
      if (pendingKeys.size > MAX_PENDING_KEYS) {
        const oldest = pendingKeys.keys().next().value;
        if (oldest) pendingKeys.delete(oldest);
      }
    }
    (config as IdempotentConfig).idempotencySignature = signature;
    config.headers.set("Idempotency-Key", key);
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const signature = (response.config as IdempotentConfig).idempotencySignature;
    if (signature) pendingKeys.delete(signature);
    return response;
  },
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      const payload = error.response?.data as
        | { code?: number; message?: string; traceId?: string }
        | undefined;

      if (status === 401) {
        clearSession();
        const next = encodeURIComponent(window.location.pathname + window.location.search);
        const isLoginRequest = (error.config?.url ?? "").includes("/auth/login");
        if (!isLoginRequest && !window.location.pathname.startsWith("/login")) {
          window.location.assign(`/login?from=${next}`);
        }
      }

      throw new ApiError(
        {
          code: payload?.code ?? status ?? -1,
          message:
            payload?.message ??
            (status ? `请求失败（HTTP ${status}）` : "无法连接后端，请检查 8125 是否启动"),
          traceId: payload?.traceId ?? error.response?.headers?.["x-trace-id"],
        },
        status,
      );
    }
    throw new ApiError({ code: -1, message: "网络异常，请稍后重试" });
  },
);

export interface RequestOptions extends AxiosRequestConfig {
  /** 静默错误：用于批量轮询等不希望打扰用户的请求。 */
  silent?: boolean;
}

const lastToast = { message: "", at: 0 };

function notify(error: ApiError, silent?: boolean) {
  if (silent || error.isAuthError) return;
  const now = Date.now();
  if (lastToast.message === error.message && now - lastToast.at < 2500) return;
  lastToast.message = error.message;
  lastToast.at = now;

  const trace = error.traceId ? `trace: ${error.traceId}` : undefined;
  if (error.isConflict) {
    toast.error("数据已被他人修改", { description: `${error.message}${error.traceId ? ` · ${trace}` : ""}` });
    return;
  }
  if (error.isValidation) {
    toast.error("操作未通过校验", { description: `${error.message}${error.traceId ? ` · ${trace}` : ""}` });
    return;
  }
  toast.error(error.message, { description: trace });
}

export async function request<T>(options: RequestOptions): Promise<T> {
  try {
    const response = await http.request<BaseResponse<T>>(options);
    if (response.status === 204) return undefined as T;
    const body = response.data;
    if (body && typeof body === "object" && "code" in body) {
      if (body.code !== 0) {
        throw new ApiError({
          code: body.code,
          message: body.message || "请求失败",
          traceId: body.traceId,
        });
      }
      return body.data;
    }
    return body as T;
  } catch (error) {
    const apiError = error instanceof ApiError ? error : new ApiError({ code: -1, message: "未知错误" });
    notify(apiError, options.silent);
    throw apiError;
  }
}

export const api = {
  get: <T>(url: string, params?: Record<string, unknown>, silent?: boolean) =>
    request<T>({ url, method: "GET", params, silent }),
  post: <T>(url: string, data?: unknown, silent?: boolean) =>
    request<T>({ url, method: "POST", data, silent }),
  put: <T>(url: string, data?: unknown, silent?: boolean) =>
    request<T>({ url, method: "PUT", data, silent }),
  patch: <T>(url: string, data?: unknown, silent?: boolean) =>
    request<T>({ url, method: "PATCH", data, silent }),
  delete: <T>(url: string, silent?: boolean) =>
    request<T>({ url, method: "DELETE", silent }),
};
