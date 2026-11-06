import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { Reservation } from "@/lib/api/models";

export interface ReservationFilters extends PageParams {
  status?: string;
  keyword?: string;
}

export const listReservations = (filters: ReservationFilters) =>
  api.get<PageResult<Reservation>>(endpoints.reservations.list, { ...filters });

export const promoteReservation = (id: string) =>
  api.post<Reservation>(endpoints.reservations.promote(id));

export const expireReservation = (id: string) =>
  api.post<Reservation>(endpoints.reservations.expire(id));
