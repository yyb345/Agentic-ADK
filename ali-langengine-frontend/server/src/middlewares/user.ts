import { IMiddleware, Middleware, httpError } from '@midwayjs/core';
import { Context, NextFunction } from '@midwayjs/web';
import { isPagePath, isPublicPath } from '../utils/path';

@Middleware()
export class UserMiddleware implements IMiddleware<Context, NextFunction> {
  static getName: () => 'UserMiddleware';

  resolve() {
    return async (ctx: Context, next: NextFunction) => {
      ctx.user = null;

      // 已登录
      if (ctx.user) {
        return next();
      }

      // 未登录
      if (isPublicPath(ctx.path)) {
        return next();
      }
      if (isPagePath(ctx.path)) {
        return ctx.redirect('/welcome');
      }
      throw new httpError.UnauthorizedError(`Unauthorized (${ctx.hostname})`);
    };
  }
}
