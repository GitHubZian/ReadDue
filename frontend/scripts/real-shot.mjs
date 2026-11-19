/* 用真实后端登录态截图：node scripts/real-shot.mjs <path> <out.png> [user] [pass] */
const [, , path = "/workbench", out = "shots/real.png", username = "ADMIN001", password = "123456"] =
  process.argv;

const res = await fetch("http://127.0.0.1:9222/json/new?about:blank", { method: "PUT" });
const target = await res.json();
const ws = new WebSocket(target.webSocketDebuggerUrl);

let seq = 0;
const pending = new Map();
const problems = [];
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
  width: 1440,
  height: 900,
  deviceScaleFactor: 2,
  mobile: false,
});

const base = "http://127.0.0.1:5173";
await send("Page.navigate", { url: `${base}/login` });
await sleep(1500);
const login = await send("Runtime.evaluate", {
  awaitPromise: true,
  returnByValue: true,
  expression: `fetch("/api/v1/auth/login", { method: "POST", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: ${JSON.stringify(username)}, password: ${JSON.stringify(password)} }) })
    .then((r) => r.json()).then((r) => r.data).catch((e) => ({ error: String(e) }))`,
});
const data = login.result?.result?.value;
if (!data?.token) {
  console.log("login failed", JSON.stringify(data));
  process.exit(1);
}
const session = JSON.stringify({
  token: data.token,
  user: data.user,
  expiresAt: Date.now() + Number(data.expiresIn) * 1000,
});
await send("Runtime.evaluate", {
  expression: `localStorage.setItem("readdue.admin.session", ${JSON.stringify(session)})`,
});
await send("Page.navigate", { url: `${base}${path}` });
await sleep(2600);
for (const label of [process.env.SHOT_CLICK, process.env.SHOT_CLICK2].filter(Boolean)) {
  const expression = `(() => {
    const target = [...document.querySelectorAll("button,[role=tab]")]
      .find((el) => el.textContent?.trim() === ${JSON.stringify(label)});
    if (!target) return "not-found";
    target.click();
    return "clicked";
  })()`;
  const clicked = await send("Runtime.evaluate", { expression, returnByValue: true });
  console.log("click", label, clicked.result?.result?.value);
  await sleep(1400);
}
if (process.env.SHOT_SCROLL) {
  const expression = `(() => {
    const heading = [...document.querySelectorAll("h2")]
      .find((el) => el.textContent?.trim() === ${JSON.stringify(process.env.SHOT_SCROLL)});
    if (!heading) return "not-found";
    heading.scrollIntoView({ block: "start" });
    return "scrolled";
  })()`;
  const scrolled = await send("Runtime.evaluate", { expression, returnByValue: true });
  console.log("scroll", process.env.SHOT_SCROLL, scrolled.result?.result?.value);
  await sleep(700);
}
if (process.env.SHOT_ROW) {
  const expression = `(() => {
    const row = document.querySelectorAll("tbody tr")[Number(${JSON.stringify(process.env.SHOT_ROW)})];
    if (!row) return "not-found";
    row.click();
    return "clicked-row";
  })()`;
  const rowClick = await send("Runtime.evaluate", { expression, returnByValue: true });
  console.log("row", process.env.SHOT_ROW, rowClick.result?.result?.value);
  await sleep(1200);
}
if (process.env.SHOT_EVAL) {
  const fs = await import("node:fs/promises");
  const expression = await fs.readFile(process.env.SHOT_EVAL, "utf8");
  const evaluated = await send("Runtime.evaluate", {
    expression,
    awaitPromise: true,
    returnByValue: true,
  });
  console.log("eval:", JSON.stringify(evaluated.result?.result?.value ?? evaluated.result, null, 2));
  await sleep(800);
}
const shot = await send("Page.captureScreenshot", { format: "png" });
const fs = await import("node:fs/promises");
await fs.mkdir("shots", { recursive: true });
await fs.writeFile(out, Buffer.from(shot.result.data, "base64"));
console.log("saved", out);
for (const p of problems) console.log("  ! " + p);
ws.close();
await fetch(`http://127.0.0.1:9222/json/close/${target.id}`);
