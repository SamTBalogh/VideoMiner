import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, ".", "");

  const videoMinerTarget =
    env.VITE_VIDEOMINER_TARGET ?? "http://localhost:8080";
  const vimeoMinerTarget =
    env.VITE_VIMEOMINER_TARGET ?? "http://localhost:8081";
  const youTubeMinerTarget =
    env.VITE_YOUTUBEMINER_TARGET ?? "http://localhost:8082";

  return {
    plugins: [react()],
    server: {
      host: "0.0.0.0",
      port: 5173,
      proxy: {
        "/api/videominer": {
          target: videoMinerTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api\/videominer/, ""),
        },
        "/api/vimeominer": {
          target: vimeoMinerTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api\/vimeominer/, ""),
        },
        "/api/youtubeminer": {
          target: youTubeMinerTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api\/youtubeminer/, ""),
        },
      },
    },
  };
});
