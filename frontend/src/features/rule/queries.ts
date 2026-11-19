import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import {
  createCalendar,
  deleteCalendar,
  fetchCalendar,
  fetchRules,
  fetchTiers,
  saveRules,
  updateCalendar,
  type CalendarPayload,
  type RuleSet,
} from "./api";
import type { RuleGroup } from "@/lib/api/models";

export const ruleKeys = {
  all: ["rules"] as const,
  tiers: ["rules", "tiers"] as const,
  calendar: ["rules", "calendar"] as const,
};

export function useRules() {
  return useQuery({ queryKey: ruleKeys.all, queryFn: fetchRules });
}

export function useSaveRules() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (payload: { groups: RuleGroup[]; expectedVersion: string }) => saveRules(payload),
    onSuccess: (data: RuleSet) => {
      client.setQueryData(ruleKeys.all, data);
      toast.success(`规则已生效（${data.version}）`);
    },
  });
}

export function useTiers() {
  return useQuery({ queryKey: ruleKeys.tiers, queryFn: fetchTiers, staleTime: 5 * 60_000 });
}

export function useCalendar() {
  return useQuery({ queryKey: ruleKeys.calendar, queryFn: fetchCalendar, staleTime: 5 * 60_000 });
}
export function useCreateCalendar() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (payload: CalendarPayload) => createCalendar(payload),
    onSuccess: () => {
      toast.success("校历条目已新增，宽限基准即时生效");
      void client.invalidateQueries({ queryKey: ruleKeys.calendar });
    },
  });
}

export function useUpdateCalendar() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: CalendarPayload }) =>
      updateCalendar(id, payload),
    onSuccess: () => {
      toast.success("校历条目已更新");
      void client.invalidateQueries({ queryKey: ruleKeys.calendar });
    },
  });
}

export function useDeleteCalendar() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteCalendar(id),
    onSuccess: () => {
      toast.success("校历条目已删除");
      void client.invalidateQueries({ queryKey: ruleKeys.calendar });
    },
  });
}