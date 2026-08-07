import vue from '@vitejs/plugin-vue';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '..', 'MUYUN_APP_');
  const backendUrl = env.MUYUN_APP_BACKEND_URL ?? 'http://127.0.0.1:8081';
  const webPort = Number(env.MUYUN_APP_WEB_PORT ?? 5174);

  return {
    plugins: [vue()],
  // npm link keeps the generated platform package outside this App's
  // node_modules tree. Resolve its peer dependencies from the App so local
  // linked development has the same dependency boundary as a normal install.
    resolve: {
      preserveSymlinks: true,
    },
    server: {
      port: webPort,
      strictPort: true,
      proxy: {
        '/iam.': backendUrl,
        '/demo.todo_item': backendUrl,
        '/platform.': backendUrl,
      },
    },
  };
});
