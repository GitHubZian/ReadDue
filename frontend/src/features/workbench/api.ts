import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { DashboardData } from "@/lib/api/models";

export const fetchDashboard = () => api.get<DashboardData>(endpoints.dashboard);
