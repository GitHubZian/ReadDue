export interface BaseResponse<T> {
  code: number;
  message?: string;
  data: T;
  traceId?: string;
}

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

export type Tone = "mint" | "amber" | "red" | "slate";
