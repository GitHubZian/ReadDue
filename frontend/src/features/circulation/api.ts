import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { BorrowResult, CirculationFlow, ReturnResult } from "@/lib/api/models";

export interface TodayFlow {
  flows: CirculationFlow[];
  borrows: number;
  returns: number;
}

export const fetchTodayFlow = () => api.get<TodayFlow>(endpoints.circulation.today);

export const borrowBook = (payload: { readerNo: string; barcode: string }) =>
  api.post<BorrowResult>(endpoints.circulation.borrow, payload);

export const returnBook = (payload: { barcode: string }) =>
  api.post<ReturnResult>(endpoints.circulation.return, payload);
