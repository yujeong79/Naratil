import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import eslintConfigPrettier from 'eslint-config-prettier'

export default [
  {
    name: 'app/files-to-lint',
    files: ['**/*.{js,mjs,jsx,vue}'],
  },

  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/dist-ssr/**', '**/coverage/**', 'src/assets/prievue/**'],
  },

  js.configs.recommended, // ESLint 기본 규칙
  ...pluginVue.configs['flat/essential'], // Vue 3 규칙
  eslintConfigPrettier, // Prettier와 충돌하는 ESLint 규칙 비활성화
  {
    plugins: ['prettier'], // Prettier 플러그인 활성화
    rules: {
      'prettier/prettier': 'error', // Prettier 규칙을 ESLint에서 에러로 처리
    },
  },
]
