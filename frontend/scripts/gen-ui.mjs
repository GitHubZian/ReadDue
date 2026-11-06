// 从 shadcn 官方 registry（new-york / Tailwind v3 版）拉取组件源码。
// 本机 shadcn CLI 在 Windows 上运行 add 会报 unknown command，故直接取 registry JSON。
// 用法：node scripts/gen-ui.mjs button card table ...
import { mkdir, writeFile } from "node:fs/promises";
import { dirname, join } from "node:path";

const STYLE = "new-york";
const ROOT = process.cwd();
const names = process.argv.slice(2);

const deps = new Set();
const written = new Set();
const seen = new Set();

async function fetchItem(name) {
  const url = `https://ui.shadcn.com/r/styles/${STYLE}/${name}.json`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`${name}: HTTP ${res.status}`);
  return res.json();
}

async function install(name) {
  if (seen.has(name)) return;
  seen.add(name);
  const item = await fetchItem(name);

  for (const dep of item.dependencies ?? []) {
    if (dep !== "cn") deps.add(dep);
  }

  for (const file of item.files ?? []) {
    const rel = file.path.replace(/^ui\//, "components/ui/").replace(/^lib\//, "lib/");
    if (rel.startsWith("components/ui/") || rel.startsWith("lib/") || rel.startsWith("hooks/")) {
      const target = join(ROOT, "src", rel);
      if (rel === "lib/utils.ts") continue;
      await mkdir(dirname(target), { recursive: true });
      await writeFile(target, file.content + (file.content.endsWith("\n") ? "" : "\n"), "utf8");
      written.add(rel);
    }
  }

  for (const dep of item.registryDependencies ?? []) {
    if (dep.startsWith("http")) continue;
    await install(dep);
  }
}

for (const name of names) {
  await install(name);
  console.log(`ok ${name}`);
}

console.log("\nfiles:", [...written].sort().join(", "));
console.log("deps:", [...deps].sort().join(" "));
