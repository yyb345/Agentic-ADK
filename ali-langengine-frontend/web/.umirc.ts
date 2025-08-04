import { defineConfig } from '@umijs/max';

const config = defineConfig({
  npmClient: 'pnpm',
  jsMinifier: 'terser',
  model: {},

  // 支持 tailwind v4
  extraPostCSSPlugins: [require('@tailwindcss/postcss')],

  // 在 build 时让文件名带 hash 值
  hash: true,

  // 在 build 时生成额外的 manifest 文件
  manifest: {},

  // 设置静态资源地址
  publicPath: '/',

  // 解决 react 多实例问题（在后端模版里加载 react 资源）
  externals: { react: 'React', 'react-dom': 'ReactDOM' },

  // 避免一些疑难杂症
  mfsu: false,

  // 跟 nginx 配置一样，仅保留静态资源，其它路径（包括页面、接口）直接代理到后端服务器
  proxy: {
    '/': {
      target: 'http://localhost:7001',
      changeOrigin: true,
      bypass: (req) => {
        if (/\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot|json)$/.test(req.url)) {
          return req.url;
        }
      },
    },
  },
});

export default config;
