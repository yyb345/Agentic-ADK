// 配置文档
// https://commitlint.js.org/reference/rules.html

module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-empty': [2, 'never'],
    'scope-enum': [2, 'always', ['server', 'web', 'common']],
  },
};
