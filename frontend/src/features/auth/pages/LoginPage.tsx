import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRight, BookMarked, CircleDot, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useLogin } from "@/features/auth/queries";
import { ApiError } from "@/lib/api/errors";

const schema = z.object({
  username: z.string().min(2, "请输入账号"),
  password: z.string().min(6, "密码至少 6 位"),
});

type LoginForm = z.infer<typeof schema>;

export function LoginPage() {
  const mutation = useLogin();
  const form = useForm<LoginForm>({
    resolver: zodResolver(schema),
    defaultValues: { username: "ADMIN001", password: "123456" },
  });

  const onSubmit = (values: LoginForm) => mutation.mutate(values);
  const error = mutation.error instanceof ApiError ? mutation.error : null;

  return (
    <div className="flex min-h-dvh bg-background">
      <section className="relative hidden flex-1 flex-col justify-between overflow-hidden bg-deep p-10 text-white lg:flex">
        <div className="flex items-center gap-2.5">
          <span className="grid size-9 place-items-center rounded-xl bg-mint text-[16px] font-bold text-deep">
            阅
          </span>
          <div className="leading-tight">
            <p className="text-[15px] font-extrabold tracking-tight">阅有期</p>
            <p className="text-[10px] text-white/50">馆藏流通管理台</p>
          </div>
        </div>

        <div className="max-w-[420px]">
          <p className="text-[10px] font-semibold tracking-[0.2em] text-mint">READDUE · ADMIN</p>
          <h1 className="mt-3 text-[34px] font-extrabold leading-[1.25] tracking-tight">
            让每一次借阅
            <br />
            都有期限，也有分寸。
          </h1>
          <p className="mt-4 max-w-[360px] text-[12px] leading-6 text-white/55">
            宽限、申诉、逾期、预约集中在一张工作台，审批留痕、规则可调、消息不过夜。
          </p>

          <dl className="mt-8 grid grid-cols-3 gap-3">
            {[
              { k: "在馆册数", v: "128,430" },
              { k: "今日到期", v: "8" },
              { k: "待裁决", v: "12" },
            ].map((item) => (
              <div key={item.k} className="rounded-xl border border-white/10 bg-white/[0.04] px-3 py-2.5">
                <dt className="text-[9.5px] text-white/45">{item.k}</dt>
                <dd className="tabular mt-1 text-[18px] font-bold text-white/90">{item.v}</dd>
              </div>
            ))}
          </dl>
        </div>

        <ul className="space-y-2 text-[10.5px] text-white/45">
          <li className="flex items-center gap-2">
            <ShieldCheck className="size-3.5 text-mint" /> 三层权限：路由、菜单、按钮逐级校验
          </li>
          <li className="flex items-center gap-2">
            <BookMarked className="size-3.5 text-mint" /> 所有写操作带幂等键与 traceId 留痕
          </li>
        </ul>
      </section>

      <section className="flex flex-1 items-center justify-center px-6 py-10">
        <div className="w-full max-w-[360px]">
          <div className="mb-7 lg:hidden">
            <span className="grid size-9 place-items-center rounded-xl bg-mint-deep text-white">阅</span>
          </div>
          <h2 className="text-[22px] font-extrabold tracking-tight text-ink">登录管理台</h2>
          <p className="mt-1 text-[11px] text-ink-3">使用馆员账号或管理员账号进入。</p>

          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="mt-6 space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-[11px] text-ink-2">账号</FormLabel>
                    <FormControl>
                      <Input
                        {...field}
                        autoComplete="username"
                        placeholder="ADMIN001"
                        className="h-10 bg-card text-[12px]"
                      />
                    </FormControl>
                    <FormMessage className="text-[10.5px]" />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-[11px] text-ink-2">密码</FormLabel>
                    <FormControl>
                      <Input
                        {...field}
                        type="password"
                        autoComplete="current-password"
                        placeholder="••••••••"
                        className="h-10 bg-card text-[12px]"
                      />
                    </FormControl>
                    <FormMessage className="text-[10.5px]" />
                  </FormItem>
                )}
              />

              {error ? (
                <p className="rounded-lg bg-danger-soft px-3 py-2 text-[10.5px] text-danger">
                  {error.message}
                </p>
              ) : null}

              <Button
                type="submit"
                className="h-10 w-full"
                disabled={mutation.isPending}
              >
                {mutation.isPending ? "登录中…" : "进入管理台"}
                <ArrowRight className="size-3.5" />
              </Button>
            </form>
          </Form>

          <div className="mt-6 rounded-xl border border-dashed border-line bg-wash/70 px-3.5 py-3">
            <p className="text-[9.5px] font-semibold tracking-wider text-ink-3">演示账号</p>
            <div className="mt-1.5 space-y-1 text-[10.5px] text-ink-2">
              <p className="flex items-center gap-1.5">
                <CircleDot className="size-2.5 text-mint-deep" /> 管理员 ADMIN001 / 123456
              </p>
              <p className="flex items-center gap-1.5">
                <CircleDot className="size-2.5 text-warn" /> 馆员 LIB001 / 123456
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
