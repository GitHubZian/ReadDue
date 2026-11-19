import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { NotifyTask, NotifyTemplate, NotifyTopic } from "@/lib/api/models";

export const listTemplates = () => api.get<NotifyTemplate[]>(endpoints.notify.templates);

export const updateTemplate = (id: string, payload: Partial<NotifyTemplate>) =>
  api.put<NotifyTemplate>(`${endpoints.notify.templates}/${id}`, payload);

export const listNotifyTasks = (params: PageParams) =>
  api.get<PageResult<NotifyTask>>(endpoints.notify.tasks, { ...params });

export interface TopicDefaultsPayload {
  defaultFrequency: string;
  defaultChannels: string[];
  enabled: boolean;
}

export const listTopics = () => api.get<NotifyTopic[]>(endpoints.notify.topics);

export const updateTopicDefaults = (code: string, payload: TopicDefaultsPayload) =>
  api.put<NotifyTopic>(endpoints.notify.topic(code), payload);

export const retryNotifyTask = (id: string) =>
  api.post<NotifyTask>(endpoints.notify.retry(id));
