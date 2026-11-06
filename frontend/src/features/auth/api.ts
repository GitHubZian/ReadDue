import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { LoginPayload, LoginResult, SessionUser } from "@/lib/api/models";

export function login(payload: LoginPayload) {
  return api.post<LoginResult>(endpoints.auth.login, payload);
}

export function fetchMe() {
  return api.get<SessionUser>(endpoints.auth.me);
}

export function logout() {
  return api.post<null>(endpoints.auth.logout);
}
