/** @type {import('tailwindcss').Config} */
// Tailwind is used for the landing page; MUI styles the rest of the app.
// preflight is OFF so Tailwind's global reset never changes MUI components.
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  corePlugins: { preflight: false },
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Source Sans 3"', '"Segoe UI"', 'system-ui', 'sans-serif'],
        serif: ['"Source Serif 4"', 'Georgia', 'serif'],
      },
    },
  },
  plugins: [],
};
