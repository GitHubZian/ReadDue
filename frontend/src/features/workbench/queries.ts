import { useQuery } from "@tanstack/react-query";
import { fetchDashboard } from "./api";

export const dashboardKey = ["dashboard"] as const;

export function useDashboard() {
  return useQuery({
    queryKey: dashboardKey,
    queryFn: fetchDashboard,
    refetchInterval: 60_000,
  });
}
