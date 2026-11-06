import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { borrowBook, fetchTodayFlow, returnBook } from "./api";

export const circulationKeys = { today: ["circulation", "today"] as const };

export function useTodayFlow() {
  return useQuery({ queryKey: circulationKeys.today, queryFn: fetchTodayFlow });
}

export function useBorrowBook() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: borrowBook,
    onSuccess: (result) => {
      toast.success(`借出成功：《${result.book.title}》应还 ${result.dueAt.slice(0, 10)}`);
      void client.invalidateQueries({ queryKey: circulationKeys.today });
    },
  });
}

export function useReturnBook() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: returnBook,
    onSuccess: (result) => {
      toast.success(`归还成功：《${result.book.title}》`, {
        description: Number(result.fine) > 0 ? `产生滞纳金 ¥${result.fine}` : "无滞纳金",
      });
      void client.invalidateQueries({ queryKey: circulationKeys.today });
    },
  });
}
