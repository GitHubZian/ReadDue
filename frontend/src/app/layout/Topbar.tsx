import { Bell, ChevronDown, Command, LogOut, Search, UserRound } from "lucide-react";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { NAV_ITEMS } from "@/app/nav";
import { StatusPill } from "@/components/common/StatusPill";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { can, ROLE_LABEL } from "@/lib/auth/permissions";
import { clearSession, useSession } from "@/lib/auth/session";
import { logout as logoutRequest } from "@/features/auth/api";

export function Topbar() {
  const { user } = useSession();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);

  const canViewAudit = can(user, "audit:read");
  const matches = useMemo(() => {
    const accessible = NAV_ITEMS.filter((item) => can(user, item.permission));
    const q = query.trim();
    if (!q) return accessible.slice(0, 5);
    return accessible.filter((item) => item.label.includes(q) || item.path.includes(q));
  }, [query, user]);

  const submit = (event: React.FormEvent) => {
    event.preventDefault();
    const first = matches[0];
    if (first) {
      navigate(first.path);
      setQuery("");
      setSearchOpen(false);
    } else if (canViewAudit) {
      navigate(`/audit?keyword=${encodeURIComponent(query.trim())}`);
      setQuery("");
      setSearchOpen(false);
    }
  };

  const logout = async () => {
    // 后端 logout 会吊销 Redis 里的 jti，先尽力通知再清本地态
    await logoutRequest().catch(() => {});
    clearSession();
    navigate("/login", { replace: true });
  };

  return (
    <header className="flex h-14 shrink-0 items-center justify-between gap-4 border-b border-line bg-background/90 px-6 backdrop-blur">
      <Popover open={searchOpen} onOpenChange={setSearchOpen}>
        <PopoverTrigger asChild>
          <form onSubmit={submit} className="relative w-[240px]" role="search">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-ink-3" />
            <Input
              value={query}
              onChange={(event) => {
                setQuery(event.target.value);
                setSearchOpen(true);
              }}
              onFocus={() => setSearchOpen(true)}
              placeholder="搜索功能或单号"
              aria-label="全局搜索"
              className="h-[34px] rounded-lg border-line bg-card pl-8 pr-14 text-[11.5px] placeholder:text-ink-3/80"
            />
            <kbd className="pointer-events-none absolute right-2 top-1/2 flex -translate-y-1/2 items-center gap-0.5 rounded border border-line bg-wash px-1 text-[9px] text-ink-3">
              <Command className="size-2.5" />K
            </kbd>
          </form>
        </PopoverTrigger>
        <PopoverContent align="start" className="w-[300px] p-1.5">
          <p className="px-2 py-1 text-[9.5px] font-semibold tracking-wider text-ink-3">
            快速跳转
          </p>
          {matches.length ? (
            matches.map((item) => (
              <button
                key={item.path}
                type="button"
                onClick={() => {
                  navigate(item.path);
                  setQuery("");
                  setSearchOpen(false);
                }}
                className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-[11.5px] text-ink-2 hover:bg-wash"
              >
                <item.icon className="size-3.5 text-ink-3" />
                {item.label}
                <span className="ml-auto text-[9.5px] text-ink-3">{item.path}</span>
              </button>
            ))
          ) : (
            <p className="px-2 py-3 text-[11px] text-ink-3">
              {canViewAudit
                ? "未找到对应页面，回车将带关键词前往审计日志。"
                : "未找到可访问的页面。"}
            </p>
          )}
        </PopoverContent>
      </Popover>

      <div className="flex items-center gap-2">
        <StatusPill tone="mint" className="hidden md:inline-flex">
          8125 已连接
        </StatusPill>

        <Popover>
          <PopoverTrigger asChild>
            <Button variant="ghost" size="icon-sm" aria-label="通知" className="relative">
              <Bell className="size-4" strokeWidth={1.8} />
              <span className="absolute right-2 top-2 size-1.5 rounded-full bg-danger ring-2 ring-background" />
            </Button>
          </PopoverTrigger>
          <PopoverContent align="end" className="w-[280px] p-0">
            <div className="border-b border-line px-3.5 py-2.5">
              <p className="text-[11.5px] font-semibold text-ink">待处理通知</p>
              <p className="text-[9.5px] text-ink-3">2 条未读 · 审批与异常</p>
            </div>
            <ul className="divide-y divide-line-2">
              <li className="px-3.5 py-2.5">
                <p className="text-[11px] font-medium text-ink">1 单宽限已超 SLA</p>
                <p className="mt-0.5 text-[10px] text-ink-3">GY-3315 · 陈默 · 已升级值班主管</p>
              </li>
              <li className="px-3.5 py-2.5">
                <p className="text-[11px] font-medium text-ink">2 条提醒发送失败</p>
                <p className="mt-0.5 text-[10px] text-ink-3">NT-2211 · 号码停机，可重试</p>
              </li>
            </ul>
            <div className="border-t border-line px-3.5 py-2">
              <button
                type="button"
                onClick={() => navigate("/notify")}
                className="text-[10.5px] font-medium text-mint-deep hover:underline"
              >
                前往提醒中心
              </button>
            </div>
          </PopoverContent>
        </Popover>

        <span className="h-5 w-px bg-line" aria-hidden />

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button
              type="button"
              className="flex h-[34px] items-center gap-2 rounded-lg pl-1 pr-1.5 text-left hover:bg-wash"
            >
              <span className="grid size-7 place-items-center rounded-full bg-mint-soft text-[11px] font-bold text-mint-deep">
                {user?.name?.slice(0, 1) ?? "阅"}
              </span>
              <span className="hidden leading-tight sm:block">
                <span className="block text-[11px] font-semibold text-ink">{user?.name ?? "未登录"}</span>
                <span className="block text-[9px] text-ink-3">
                  {user ? ROLE_LABEL[user.role] : "—"}
                </span>
              </span>
              <ChevronDown className="size-3 text-ink-3" />
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-44">
            <DropdownMenuLabel className="text-[10px] font-normal text-ink-3">
              {user?.studentNo ?? "—"}
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            {can(user, "user:manage") ? (
              <DropdownMenuItem onClick={() => navigate("/users")} className="text-[11.5px]">
                <UserRound className="size-3.5" /> 用户与权限
              </DropdownMenuItem>
            ) : null}
            <DropdownMenuItem onClick={logout} className="text-[11.5px] text-danger">
              <LogOut className="size-3.5" /> 退出登录
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
