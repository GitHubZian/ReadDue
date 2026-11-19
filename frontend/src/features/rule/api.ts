import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { CalendarEntry, RuleGroup, TierRow } from "@/lib/api/models";

export interface RuleSet {
  groups: RuleGroup[];
  version: string;
}

export const fetchRules = () => api.get<RuleSet>(endpoints.rules.root);

export const saveRules = (payload: { groups: RuleGroup[]; expectedVersion: string }) =>
  api.put<RuleSet>(endpoints.rules.root, payload);

export const fetchTiers = () => api.get<TierRow[]>(endpoints.rules.tiers);

export const fetchCalendar = () => api.get<CalendarEntry[]>(endpoints.rules.calendar);
export interface CalendarPayload {
  term: string;
  scene: string;
  startDate: string;
  endDate: string;
  source?: string;
  note?: string;
}

export const createCalendar = (payload: CalendarPayload) =>
  api.post<CalendarEntry>(endpoints.rules.calendar, payload);

export const updateCalendar = (id: string, payload: CalendarPayload) =>
  api.put<CalendarEntry>(endpoints.rules.calendarItem(id), payload);

export const deleteCalendar = (id: string) =>
  api.delete<null>(endpoints.rules.calendarItem(id));