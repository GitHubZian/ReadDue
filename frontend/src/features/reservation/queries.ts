import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import {
  expireReservation,
  listReservations,
  promoteReservation,
  type ReservationFilters,
} from "./api";

export const reservationKeys = {
  list: (filters: ReservationFilters) => ["reservations", filters] as const,
};

export function useReservations(filters: ReservationFilters) {
  return useQuery({
    queryKey: reservationKeys.list(filters),
    queryFn: () => listReservations(filters),
    placeholderData: (prev) => prev,
  });
}

export function usePromoteReservation() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => promoteReservation(id),
    onSuccess: () => {
      toast.success("已提升为可借，并通知读者 48 小时内取书");
      void client.invalidateQueries({ queryKey: ["reservations"] });
    },
  });
}

export function useExpireReservation() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => expireReservation(id),
    onSuccess: () => {
      toast.success("已释放预约，队列顺位自动前移");
      void client.invalidateQueries({ queryKey: ["reservations"] });
    },
  });
}
