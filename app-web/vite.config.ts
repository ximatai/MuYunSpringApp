import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [vue()],
  // npm link keeps the generated platform package outside this App's
  // node_modules tree. Resolve its peer dependencies from the App so local
  // linked development has the same dependency boundary as a normal install.
  resolve: {
    preserveSymlinks: true,
  },
  server: {
    strictPort: true,
    proxy: {
      '/iam.': 'http://127.0.0.1:8081',
      '/demo.todo_item': 'http://127.0.0.1:8081',
      '/platform.': 'http://127.0.0.1:8081',
    },
  },
});
