import { NavLink } from "react-router-dom";
import { MoonStar, ShieldCheck } from "lucide-react";
import { NAV } from "@/app/nav";
import { useSession } from "@/lib/auth/session";
import { can } from "@/lib/auth/permissions";
import { cn } from "@/lib/utils";

export function Sidebar() {
  const { user } = useSession();

  return (
    <aside className="flex w-[232px] shrink-0 flex-col border-r border-line bg-wash/60">
      <div className="flex h-14 items-center gap-2.5 px-5">
        <span className="grid size-8 place-items-center rounded-lg bg-mint-deep text-[15px] font-bold text-white">
          阅
        </span>
        <span className="leading-tight">
          <span className="block text-[14px] font-extrabold tracking-tight text-ink">阅有期</span>
          <span className="block text-[9.5px] tracking-wide text-ink-3">馆藏流通管理台</span>
        </span>
      </div>

      <nav className="scrollbar-slim flex-1 overflow-y-auto px-3 pb-4">
        {NAV.map((group) => {
          const items = group.items.filter((item) => can(user, item.permission));
          if (!items.length) return null;
          return (
            <div key={group.label} className="pt-4 first:pt-2">
              <p className="px-2.5 pb-1.5 text-[9px] font-semibold tracking-[0.14em] text-ink-3/85">
                {group.label}
              </p>
              <ul className="space-y-0.5">
                {items.map((item) => (
                  <li key={item.path}>
                    <NavLink
                      to={item.path}
                      className={({ isActive }) =>
                        cn(
                          "flex h-8 items-center gap-2.5 rounded-lg px-2.5 text-[12px] transition-colors",
                          isActive
                            ? "bg-mint-soft font-semibold text-mint-deep"
                            : "text-ink-2 hover:bg-wash hover:text-ink",
                        )
                      }
                    >
                      <item.icon className="size-4 shrink-0" strokeWidth={1.8} />
                      {item.label}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          );
        })}
      </nav>

      <div className="px-3 pb-4">
        <div className="rounded-xl bg-deep px-3.5 py-3 text-white/90">
          <p className="flex items-center gap-1.5 text-[10.5px] font-semibold text-white">
            <MoonStar className="size-3.5" strokeWidth={1.8} />
            夜间免打扰已开启
          </p>
          <p className="mt-1 text-[9.5px] leading-4 text-white/55">
            22:00–08:00 暂停外部推送，站内信正常送达。
          </p>
          <div className="mt-2 flex items-center gap-1 text-[9.5px] text-white/45">
            <ShieldCheck className="size-3" strokeWidth={1.8} />
            值班 · 王倩 · 内线 8021
          </div>
        </div>
      </div>
    </aside>
  );
}
