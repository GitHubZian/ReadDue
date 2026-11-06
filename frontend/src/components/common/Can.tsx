import { useSession } from "@/lib/auth/session";
import { can, type Permission } from "@/lib/auth/permissions";

/** 按钮级权限：无权限时不渲染（路由与菜单是另外两层）。 */
export function Can({
  code,
  children,
  fallback = null,
}: {
  code: Permission;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}) {
  const { user } = useSession();
  return can(user, code) ? <>{children}</> : <>{fallback}</>;
}
