import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import {
  listNotifyTasks,
  listTemplates,
  retryNotifyTask,
  updateTemplate,
} from "./api";
import type { NotifyTemplate } from "@/lib/api/models";

export const notifyKeys = {
  templates: ["notify", "templates"] as const,
  tasks: (page: number, size: number) => ["notify", "tasks", page, size] as const,
};

export function useTemplates() {
  return useQuery({ queryKey: notifyKeys.templates, queryFn: listTemplates });
}

export function useUpdateTemplate() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: Partial<NotifyTemplate> }) =>
      updateTemplate(id, payload),
    onSuccess: () => {
      toast.success("模板已保存");
      void client.invalidateQueries({ queryKey: ["notify", "templates"] });
    },
  });
}

export function useNotifyTasks(page = 1, size = 20) {
  return useQuery({
    queryKey: notifyKeys.tasks(page, size),
    queryFn: () => listNotifyTasks({ page, size }),
    placeholderData: (prev) => prev,
  });
}

export function useRetryNotifyTask() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => retryNotifyTask(id),
    onSuccess: () => {
      toast.success("已重新入队发送");
      void client.invalidateQueries({ queryKey: ["notify", "tasks"] });
    },
  });
}
