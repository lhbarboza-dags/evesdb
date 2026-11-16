/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from 'tailwindcss';
import autoprefixer from 'autoprefixer';

// In development the API runs on :8080 (mvn spring-boot:run); in Docker nginx proxies /api.
export default defineConfig({
  plugins: [react()],
  // inline PostCSS config (Tailwind + autoprefixer): also stops Vite from picking up a postcss.config.js in a parent folder
  css: { postcss: { plugins: [tailwindcss(), autoprefixer()] } },
  server: {
    port: 5173,
    proxy: { '/api': { target: process.env.API_URL ?? 'http://localhost:8080', changeOrigin: false } },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
});
