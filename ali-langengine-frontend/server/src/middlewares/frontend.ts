import { HttpService } from '@midwayjs/axios';
import { IMiddleware, Inject, Logger, Middleware } from '@midwayjs/core';
import { ILogger } from '@midwayjs/logger';
import { Context, NextFunction } from '@midwayjs/web';
import { ENV } from '../constants/env';
import { isPagePath } from '../utils/path';

const HOST = {
  local: 'http://localhost:8000',
  prod: 'http://ali-langengine-frontend-web', // 直接用 internal address 进行访问
}[ENV];

@Middleware()
export class FrontendMiddleware implements IMiddleware<Context, NextFunction> {
  static getName: () => 'FrontendMiddleware';

  @Inject()
  httpService: HttpService;

  @Logger()
  logger: ILogger;

  assets?: Record<string, string>; // 缓存一下前端资源列表
  assetsCacheTime = 0; // 缓存时间

  async fetchAssets() {
    // 缓存时间 10 秒
    const now = Date.now();
    if (now - this.assetsCacheTime > 1000 * 10) {
      const url = `${HOST}/asset-manifest.json`;
      const { status, data } = await this.httpService.get(url);
      if (status === 200) {
        this.assets = data;
        this.assetsCacheTime = now;
      }
      this.logger.info('fetchAssets url:', url);
      this.logger.info('fetchAssets data:', data);
    }
  }

  resolve() {
    return async (ctx: Context, next: NextFunction) => {
      if (!isPagePath(ctx.path)) {
        return next();
      }

      // 把用户信息注入到前端模版里
      const user = ctx.user;

      // 本地环境直接使用本地的前端资源
      if (ENV === 'local') {
        return await ctx.render('index', {
          cssFileName: `${HOST}/umi.css`,
          jsFileName: `${HOST}/umi.js`,
          user,
        });
      }

      // 服务器环境要拉取远程的前端资源
      await this.fetchAssets();
      await ctx.render('index', {
        cssFileName: this.assets?.['umi.css'],
        jsFileName: this.assets?.['umi.js'],
        user,
      });
    };
  }
}
