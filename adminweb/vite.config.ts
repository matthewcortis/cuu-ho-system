import tailwindcss from "@tailwindcss/vite";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import svgr from "vite-plugin-svgr";
import { fileURLToPath, URL } from "node:url";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const serverBaseUrl =
    env.SERVER_BASE_URL ||
    env.VITE_SERVER_BASE_URL ||
    env.VITE_API_BASE_URL ||
    "http://localhost:8080";

  return {
    define: {
      "import.meta.env.SERVER_BASE_URL": JSON.stringify(
        serverBaseUrl.replace(/\/+$/, "")
      ),
    },
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    plugins: [
      tailwindcss(),
      react(),
      svgr({
        svgrOptions: {
          icon: true,
          // This will transform your SVG to a React component
          exportType: "named",
          namedExport: "ReactComponent",
        },
      }),
    ],
  };
});
