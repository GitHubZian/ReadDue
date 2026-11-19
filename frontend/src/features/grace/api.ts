import { api, request } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { GraceDecision, GraceRecord } from "@/lib/api/models";

export interface GraceFilters extends PageParams {
  status?: string;
  keyword?: string;
}

export const listGraces = (filters: GraceFilters) =>
  api.get<PageResult<GraceRecord>>(endpoints.graces.list, { ...filters });

export const approveGrace = (id: string, payload: GraceDecision) =>
  api.post<GraceRecord>(endpoints.graces.decide(id), payload);

export const rejectGrace = (id: string, payload: { note: string }) =>
  api.post<GraceRecord>(endpoints.graces.reject(id), payload);

/** 附件走受控下载：带 JWT 取 blob，避免把受保护文件暴露成公开链接。 */
export const fetchEvidenceBlob = (id: string) =>
  request<Blob>({
    url: endpoints.files.download(id),
    method: "GET",
    responseType: "blob",
  });