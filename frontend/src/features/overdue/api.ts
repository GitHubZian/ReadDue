import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { OverdueItem } from "@/lib/api/models";

export interface OverdueFilters extends PageParams {
  status?: string;
  keyword?: string;
}

export const listOverdue = (filters: OverdueFilters) =>
  api.get<PageResult<OverdueItem>>(endpoints.overdue.list, { ...filters });

export const remindOverdue = (ids: string[]) =>
  api.post<{ sent: number }>(endpoints.overdue.remind, { ids });
