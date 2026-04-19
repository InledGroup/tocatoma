// @ts-check
import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';

// https://astro.build/config
export default defineConfig({
  // Genera archivos .html planos (ej: settings.html) en lugar de carpetas.
  // Es mucho más fiable para apps nativas con Capacitor.
  build: {
    format: 'file'
  },
  vite: {
    plugins: [tailwindcss()],
    optimizeDeps: {
      include: ['lucide']
    }
  }
});
