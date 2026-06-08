import type { Config } from 'tailwindcss'

export default {
  content: [
    './components/**/*.{js,vue,ts}',
    './layouts/**/*.vue',
    './pages/**/*.vue',
    './plugins/**/*.{js,ts}',
    './nuxt.config.{js,ts}',
    './app.vue',
  ],
  theme: {
    extend: {
      colors: {
        wa: {
          bg: '#0B141A',
          sidebar: '#111B21',
          header: '#202C33',
          panel: '#111B21',
          'bubble-sent': '#005C4B',
          'bubble-recv': '#202C33',
          text: '#E9EDEF',
          'text-muted': '#8696A0',
          green: '#00A884',
          border: '#222D34',
          hover: '#2A3942',
          search: '#1B2229',
          icon: '#8696A0',
          'system-msg': '#182229',
          link: '#53bdeb',
        },
      },
      fontFamily: {
        sans: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          'Roboto',
          '"Helvetica Neue"',
          'Arial',
          'sans-serif',
        ],
      },
    },
  },
  plugins: [],
} satisfies Config
