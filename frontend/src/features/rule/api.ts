import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { RuleGroup } from "@/lib/api/models";

export interface RuleSet {
  groups: RuleGroup[];
  version: string;
}

export const fetchRules = () => api.get<RuleSet>(endpoints.rules.root);

export const saveRules = (payload: { groups: RuleGroup[]; expectedVersion: string }) =>
  api.put<RuleSet>(endpoints.rules.root, payload);
