/* 用本机 Chrome 以 CDP 截图，做视觉自查：node scripts/shot.mjs <path> <out.png> [width] [height] */
const [, , path = "/workbench", out = "shots/page.png", w = "1440", h = "900"] = process.argv;

const res = await fetch("http://127.0.0.1:9222/json/new?about:blank", { method: "PUT" });
const target = await res.json();
const ws = new WebSocket(target.webSocketDebuggerUrl);

const problems = [];
let seq = 0;
const pending = new Map();
ws.addEventListener("message", (event) => {
  const msg = JSON.parse(event.data);
  if (msg.method === "Runtime.exceptionThrown") {
    const d = msg.params.exceptionDetails;
    problems.push(`[exception] ${d.exception?.description ?? d.text}`);
  }
  if (msg.method === "Runtime.consoleAPICalled" && msg.params.type === "error") {
    problems.push(`[console] ${msg.params.args.map((a) => a.value ?? a.description ?? "").join(" ")}`);
  }
  if (msg.id && pending.has(msg.id)) {
    pending.get(msg.id)(msg);
    pending.delete(msg.id);
  }
});
const send = (method, params = {}) =>
  new Promise((resolve) => {
    const id = ++seq;
    pending.set(id, resolve);
    ws.send(JSON.stringify({ id, method, params }));
  });

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
await new Promise((resolve) => ws.addEventListener("open", resolve));

await send("Page.enable");
await send("Runtime.enable");
await send("Emulation.setDeviceMetricsOverride", {
  width: Number(w),
  height: Number(h),
  deviceScaleFactor: 1,
  mobile: false,
});

const base = "http://127.0.0.1:5173";
await send("Page.navigate", { url: `${base}/login` });
await sleep(1800);

const role = process.env.SHOT_ROLE === "librarian" ? "LIBRARIAN" : "ADMIN";
const perms = role === "ADMIN" ? ["workbench:read","borrow:return","reservation:manage","overdue:read","grace:approve","appeal:handle","notify:manage","user:manage","rule:update","audit:read"] : ["workbench:read","borrow:return","reservation:manage","overdue:read","grace:approve","appeal:handle","notify:manage","audit:read"];
const session = JSON.stringify({
  token: "mock.admin.jwt",
  expiresAt: Date.now() + 8 * 3600 * 1000,
  user: {
    id: "U-1000",
    name: "陈立",
    role,
    studentNo: "STAFF100",
    permissions: perms,
  },
});
await send("Runtime.evaluate", {
  expression: `localStorage.setItem("readdue.admin.session", ${JSON.stringify(session)})`,
});

await send("Page.navigate", { url: `${base}${path}` });
await sleep(2500);
for (const label of [process.env.SHOT_CLICK, process.env.SHOT_CLICK2].filter(Boolean)) {
  await send("Runtime.evaluate", { expression: `(() => { const el = [...document.querySelectorAll("button,[role=tab]")].find((b) => b.textContent?.trim() === ${JSON.stringify(label)}); if (el) { el.click(); return "clicked"; } return "not-found"; })()` });
  await sleep(1000);
}
const shot = await send("Page.captureScreenshot", { format: "png" });
const fs = await import("node:fs/promises");
await fs.mkdir("shots", { recursive: true });
await fs.writeFile(out, Buffer.from(shot.result.data, "base64"));
console.log("saved", out);
for (const p of problems) console.log("  ! " + p);
ws.close();
await fetch(`http://127.0.0.1:9222/json/close/${target.id}`);
