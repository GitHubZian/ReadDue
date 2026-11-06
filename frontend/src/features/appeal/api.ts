import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { AppealTicket } from "@/lib/api/models";

export interface AppealFilters extends PageParams {
  status?: string;
  category?: string;
  keyword?: string;
}

export type AppealAction = "resolve" | "reject" | "compensate";

export const listAppeals = (filters: AppealFilters) =>
  api.get<PageResult<AppealTicket>>(endpoints.appeals.list, { ...filters });

export const ackAppeal = (id: string) => api.post<AppealTicket>(endpoints.appeals.ack(id));

export const decideAppeal = (
  id: string,
  action: AppealAction,
  payload: { note: string; days?: number },
) => api.post<AppealTicket>(endpoints.appeals[action](id), payload);
