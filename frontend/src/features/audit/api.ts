import { api, request } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { AuditLog } from "@/lib/api/models";

export interface AuditFilters extends PageParams {
  category?: string;
  keyword?: string;
  from?: string;
  to?: string;
}

export const listAuditLogs = (filters: AuditFilters) =>
  api.get<PageResult<AuditLog>>(endpoints.audit.list, { ...filters });

export const fetchAuditLog = (id: string) => api.get<AuditLog>(endpoints.audit.detail(id));

/** 导出走浏览器下载，仍由统一 client 取 blob 以携带鉴权头。 */
export const exportAuditLogs = (filters: AuditFilters) =>
  request<Blob>({
    url: endpoints.audit.export,
    method: "GET",
    params: { ...filters },
    responseType: "blob",
  });
