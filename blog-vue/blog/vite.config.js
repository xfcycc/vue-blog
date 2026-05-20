import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue2";

const rootVue = fileURLToPath(
  new URL("./node_modules/vue/dist/vue.runtime.esm.js", import.meta.url)
);

export default defineConfig({
  plugins: [vue()],
  resolve: {
    dedupe: ["vue"],
    extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"],
    alias: [
      {
        find: /^vue$/,
        replacement: rootVue
      },
      {
        find: /^vue-social-share$/,
        replacement: fileURLToPath(
          new URL("./src/compat/vue-social-share.js", import.meta.url)
        )
      },
      {
        find: /^vue-baberrage$/,
        replacement: fileURLToPath(
          new URL("./src/compat/vue-baberrage.js", import.meta.url)
        )
      },
      {
        find: /^tocbot$/,
        replacement: fileURLToPath(
          new URL("./src/compat/tocbot.js", import.meta.url)
        )
      },
      {
        find: "@",
        replacement: fileURLToPath(new URL("./src", import.meta.url))
      }
    ]
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8089",
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, "")
      }
    }
  },
  build: {
    sourcemap: false
  },
  css: {
    devSourcemap: false
  }
});
