import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

const apiBaseUrl = process.env.API_BASE_URL ?? 'http://127.0.0.1:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    host: '127.0.0.1',
    port: 5173,
    strictPort: true,
    proxy: {
      '/actuator/health': apiBaseUrl,
      '/api': apiBaseUrl,
    },
  },
  preview: {
    host: '127.0.0.1',
    port: 4173,
    strictPort: true,
  },
});
