// 拉取 8125 后端的 OpenAPI 文档，再交给 openapi-typescript 生成 schema.d.ts。
// 注意：context-path=/api，所以地址必须是 /api/v3/api-docs。
import { mkdir, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";

const target = process.env.OPENAPI_URL ?? "http://127.0.0.1:8125/api/v3/api-docs";
const out = resolve("src/lib/api/openapi.json");

const res = await fetch(target);
if (!res.ok) {
  throw new Error(`拉取 OpenAPI 失败：${target} -> HTTP ${res.status}`);
}
const doc = await res.json();
await mkdir(dirname(out), { recursive: true });
await writeFile(out, JSON.stringify(doc, null, 2), "utf8");
console.log(`OpenAPI 已保存：${out}（title=${doc.info?.title}，paths=${Object.keys(doc.paths ?? {}).length}）`);
