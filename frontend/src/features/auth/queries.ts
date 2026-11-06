import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { login } from "./api";
import { saveSession } from "@/lib/auth/session";

export function useLogin() {
  const navigate = useNavigate();
  return useMutation({
    mutationFn: login,
    onSuccess: (result) => {
      saveSession(result);
      const from = new URLSearchParams(window.location.search).get("from");
      navigate(from && from.startsWith("/") && !from.startsWith("//") ? from : "/workbench", { replace: true });
    },
  });
}
