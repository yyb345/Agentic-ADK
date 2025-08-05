module.exports = {
  plugins: [
    require.resolve('prettier-plugin-organize-imports'),
    require.resolve('prettier-plugin-packagejson'),
    require.resolve('prettier-plugin-tailwindcss'),
  ],
  printWidth: 120,
  proseWrap: 'never',
  singleQuote: true,
  trailingComma: 'all',
  overrides: [
    {
      files: '*.md',
      options: {
        proseWrap: 'preserve',
      },
    },
  ],
};
