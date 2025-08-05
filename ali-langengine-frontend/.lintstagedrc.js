// lint-staged 会忽略 eslint 等工具的 ignore 文件，所以需要我们主动过滤一下
// https://github.com/lint-staged/lint-staged/issues/584

// 忽略 ali-modules 目录
const getFileArgs = (files) => {
  return files.filter((file) => !/\/ali-modules\//.test(file)).join(' ');
};

// 使用函数
// https://github.com/lint-staged/lint-staged?tab=readme-ov-file#using-js-configuration-files
module.exports = {
  '*.{ts,tsx}': (files) => {
    const fileAgrs = getFileArgs(files);
    if (fileAgrs) {
      return [
        `eslint --fix ${fileAgrs}`,
        `prettier --parser=typescript --write ${fileAgrs}`,
      ];
    }
    return [];
  },
  '*.{js,jsx}': (files) => {
    const fileAgrs = getFileArgs(files);
    if (fileAgrs) {
      return [`eslint --fix ${fileAgrs}`, `prettier --write ${fileAgrs}`];
    }
    return [];
  },
  '*.{css,less}': (files) => {
    const fileAgrs = getFileArgs(files);
    if (fileAgrs) {
      return [`stylelint --fix ${fileAgrs}`, `prettier --write ${fileAgrs}`];
    }
    return [];
  },
  '*.{json,md}': (files) => {
    const fileAgrs = getFileArgs(files);
    if (fileAgrs) {
      return [`prettier --write --no-error-on-unmatched-pattern ${fileAgrs}`];
    }
    return [];
  },
};
