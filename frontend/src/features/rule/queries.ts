import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { fetchRules, saveRules, type RuleSet } from "./api";
import type { RuleGroup } from "@/lib/api/models";

export const ruleKeys = { all: ["rules"] as const };

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
