import { MidwayAppInfo, MidwayConfig } from '@midwayjs/core';
import { Context } from '@midwayjs/web';
import { CORS_ALLOW_ORIGINS } from '../constants/app';

export default (appInfo: MidwayAppInfo) => {
  console.log('[config] env:', appInfo.env);
  return {
    keys: 'ali_langengine_frontend_cookie_key',
    egg: {
      port: parseInt(process.env.PORT) || 7001,
    },

    // 安全
    // https://www.midwayjs.org/docs/extensions/security
    security: {
      // X-Frame-Options 已逐渐被废弃，所以我们直接关掉，改成用 CSP 来做限制
      // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options
      xframe: { enable: false },

      // 允许子应用通过 iframe 嵌入，另外由于 Cookie 已经设置 SameSite: None 所以被跨域嵌入的时候也可以读取到 Cookie
      // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/frame-ancestors
      csp: {
        enable: true,
        policy: {
          'frame-ancestors': ['self', ...CORS_ALLOW_ORIGINS],
        },
      },
    },

    // 允许子应用跨域调用接口
    // https://www.midwayjs.org/docs/extensions/cross_domain
    cors: {
      credentials: true,
      origin: (ctx: Context) => {
        const origin = ctx.get('origin');
        if (CORS_ALLOW_ORIGINS.includes(origin)) {
          return origin;
        }
      },
    },

    // 用于生成 guest token
    jwt: {
      secret: process.env.JWT_SECRET,
      // 不设置即代表永久有效
      // sign: {
      //   expiresIn: '30 days',
      // },
    },

    // 模版渲染
    // https://www.midwayjs.org/docs/extensions/render
    view: {
      defaultExtension: '.ejs',
      mapping: {
        '.ejs': 'ejs',
      },
    },
    ejs: {},
  } as MidwayConfig;
};
