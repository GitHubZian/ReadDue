import { useEffect } from "react";
import { Navigate, Outlet, createBrowserRouter, useLocation } from "react-router-dom";
import { AppShell } from "@/app/layout/AppShell";
import { updateUser, useSession } from "@/lib/auth/session";
import { fetchMe } from "@/features/auth/api";
import { can, type Permission } from "@/lib/auth/permissions";
import { LoginPage } from "@/features/auth/pages/LoginPage";
import { WorkbenchPage } from "@/features/workbench/pages/WorkbenchPage";
import { CirculationPage } from "@/features/circulation/pages/CirculationPage";
import { ReservationPage } from "@/features/reservation/pages/ReservationPage";
import { OverduePage } from "@/features/overdue/pages/OverduePage";
import { GracePage } from "@/features/grace/pages/GracePage";
import { AppealPage } from "@/features/appeal/pages/AppealPage";
import { NotifyPage } from "@/features/notify/pages/NotifyPage";
import { UsersPage } from "@/features/iam/pages/UsersPage";
import { RulesPage } from "@/features/rule/pages/RulesPage";
import { AuditPage } from "@/features/audit/pages/AuditPage";
import { ForbiddenPage } from "@/features/auth/pages/ForbiddenPage";
import { NotFoundPage } from "@/features/auth/pages/NotFoundPage";

function RequireAuth() {
  const { isAuthenticated } = useSession();
  const location = useLocation();

  // 启动时用 /me 复核权限：角色被调整或拉黑后，菜单与守卫立即收敛
  useEffect(() => {
    if (!isAuthenticated) return;
    fetchMe().then(updateUser).catch(() => {});
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    const from = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`/login?from=${from}`} replace />;
  }
  return <Outlet />;
}

function Guard({ code, children }: { code: Permission; children: React.ReactNode }) {
  const { user } = useSession();
  return can(user, code) ? <>{children}</> : <Navigate to="/403" replace />;
}

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  {
    element: <RequireAuth />,
    children: [
      {
        path: "/",
        element: <AppShell />,
        children: [
          { index: true, element: <Navigate to="/workbench" replace /> },
          {
            path: "workbench",
            element: (
              <Guard code="workbench:read">
                <WorkbenchPage />
              </Guard>
            ),
          },
          {
            path: "circulation",
            element: (
              <Guard code="borrow:return">
                <CirculationPage />
              </Guard>
            ),
          },
          {
            path: "reservations",
            element: (
              <Guard code="reservation:manage">
                <ReservationPage />
              </Guard>
            ),
          },
          {
            path: "overdue",
            element: (
              <Guard code="overdue:read">
                <OverduePage />
              </Guard>
            ),
          },
          {
            path: "grace",
            element: (
              <Guard code="grace:approve">
                <GracePage />
              </Guard>
            ),
          },
          {
            path: "appeals",
            element: (
              <Guard code="appeal:handle">
                <AppealPage />
              </Guard>
            ),
          },
          {
            path: "notify",
            element: (
              <Guard code="notify:manage">
                <NotifyPage />
              </Guard>
            ),
          },
          {
            path: "users",
            element: (
              <Guard code="user:manage">
                <UsersPage />
              </Guard>
            ),
          },
          {
            path: "rules",
            element: (
              <Guard code="rule:update">
                <RulesPage />
              </Guard>
            ),
          },
          {
            path: "audit",
            element: (
              <Guard code="audit:read">
                <AuditPage />
              </Guard>
            ),
          },
          { path: "403", element: <ForbiddenPage /> },
          { path: "*", element: <NotFoundPage /> },
        ],
      },
    ],
  },
]);
