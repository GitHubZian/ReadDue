import { Bell, ChevronDown, ChevronRight, Command, LogOut, Search, UserRound } from "lucide-react";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
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
import { useDashboard } from "@/features/workbench/queries";
import { fromNow } from "@/lib/format/date";
import { maskNoticeText } from "@/lib/mask";

export function Topbar() {
  const { user } = useSession();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [noticeOpen, setNoticeOpen] = useState(false);
  const dashboard = useDashboard();
  const stats = dashboard.data?.stats;
  const notices = dashboard.data?.notices ?? [];

  const canViewAudit = can(user, "audit:read");
  const matches = useMemo(() => {
    const accessible = NAV_ITEMS.filter((item) => can(user, item.permission));
    const q = query.trim();
    if (!q) return accessible.slice(0, 5);
    return accessible.filter((item) => item.label.includes(q) || item.path.includes(q));
  }, [query, user]);

  const submit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!query.trim()) {
      setSearchOpen(false);
      return;
    }
    const first = matches[0];
    if (first) {
      navigate(first.path);
      setQuery("");
      setSearchOpen(false);
    } else if (canViewAudit) {
      navigate(`/audit?keyword=${encodeURIComponent(query.trim())}`);
      setQuery("");
      setSearchOpen(false);
    } else {
      // 馆员没有审计权限：给明确反馈，不再静默停在原地
      toast.error("未找到匹配的功能或单号", {
        description: "请换一个功能名称，或联系管理员查询单号。",
      });
      setSearchOpen(false);
    }
  };

  const noticeTarget = (notice: { id: string; title: string }) => {
    if (notice.title.includes("宽限")) return `/grace?id=${notice.id}`;
    if (notice.title.includes("申诉")) return `/appeals?id=${notice.id}`;
    if (notice.title.includes("逾期") || notice.title.includes("催还")) return "/overdue";
    return "/notify";
  };

  const openNotice = (notice: { id: string; title: string }) => {
    navigate(noticeTarget(notice));
    setNoticeOpen(false);
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
                className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-[11.5px] text-ink-2 hover:bg-wash focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint-deep/40"
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
                : "未找到匹配的功能或单号，换一个关键词试试。"}
            </p>
          )}
        </PopoverContent>
      </Popover>

      <div className="flex items-center gap-2">
        <StatusPill
          tone={dashboard.isError ? "slate" : "mint"}
          className="hidden md:inline-flex"
        >
          {dashboard.isPending ? "连接中" : dashboard.isError ? "服务未连接" : "服务在线"}
        </StatusPill>

        <Popover open={noticeOpen} onOpenChange={setNoticeOpen}>
          <PopoverTrigger asChild>
            <Button variant="ghost" size="icon-sm" aria-label="通知" className="relative">
              <Bell className="size-4" strokeWidth={1.8} />
              {stats?.pendingTotal ? (
                <span className="absolute right-2 top-2 size-1.5 rounded-full bg-danger ring-2 ring-background" />
              ) : null}
            </Button>
          </PopoverTrigger>
          <PopoverContent align="end" className="w-[300px] p-0">
            <div className="border-b border-line px-3.5 py-2.5">
              <p className="text-[11.5px] font-semibold text-ink">待处理通知</p>
              <p className="text-[9.5px] text-ink-3">
                {stats ? `${stats.pendingTotal} 项待处理 · 审批与逾期` : "正在同步待办…"}
              </p>
            </div>
            <ul className="divide-y divide-line-2">
              {notices.length ? (
                notices.slice(0, 4).map((notice) => (
                  <li key={notice.id}>
                    <button
                      type="button"
                      onClick={() => openNotice(notice)}
                      className="flex w-full items-start gap-2 px-3.5 py-2.5 text-left transition-colors hover:bg-wash focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-mint-deep/40"
                    >
                      <span className="min-w-0 flex-1">
                        <span className="block text-[11px] font-medium text-ink">{notice.title}</span>
                        <span className="mt-0.5 block text-[10px] text-ink-3">
                          {maskNoticeText(notice.body)} · {fromNow(notice.at)}
                        </span>
                      </span>
                      <ChevronRight className="mt-0.5 size-3 shrink-0 text-ink-3" />
                    </button>
                  </li>
                ))
              ) : (
                <li className="px-3.5 py-3 text-[10.5px] text-ink-3">暂无新通知</li>
              )}
            </ul>
            <div className="border-t border-line px-3.5 py-2">
              <button
                type="button"
                onClick={() => {
                  navigate("/notify");
                  setNoticeOpen(false);
                }}
                className="rounded text-[10.5px] font-medium text-mint-deep hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint-deep/40"
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
