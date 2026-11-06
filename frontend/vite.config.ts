import path from "node:path";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react-swc";

// 后端：Spring Boot，server.port=8125，context-path=/api
// 因此 /api/v1/** 经 Vite proxy 原样转发到 http://127.0.0.1:8125/api/v1/**
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const target = env.VITE_PROXY_TARGET || "http://127.0.0.1:8125";

  return {
    plugins: [react()],
    resolve: {
      alias: { "@": path.resolve(__dirname, "./src") },
    },
    server: {
      port: 5173,
      // 编辑器的原子写会在源码目录留下 .xxx.tmpdir 临时目录，
      // 让 chokidar 监听它们会导致 Windows 上 EBUSY 崩溃，直接忽略。
      watch: {
        ignored: ["**/.*.tmpdir/**", "**/*.tmp"],
      },
      proxy: {
        "/api": {
          target,
          changeOrigin: true,
          configure: (proxy) => {
            proxy.on("error", (err) => {
              console.warn("[vite proxy]", err.message);
            });
          },
        },
      },
    },
    build: {
      sourcemap: false,
      chunkSizeWarningLimit: 900,
    },
  };
});
