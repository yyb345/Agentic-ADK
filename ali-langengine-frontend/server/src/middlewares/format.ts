import { HttpStatus, IMiddleware, Middleware } from '@midwayjs/core';
import { Context, NextFunction } from '@midwayjs/web';
import { isPagePath } from '../utils/path';

@Middleware()
export class FormatMiddleware implements IMiddleware<Context, NextFunction> {
  static getName: () => 'FormatMiddleware';

  resolve() {
    return async (ctx: Context, next: NextFunction) => {
      if (isPagePath(ctx.path)) {
        return next();
      }

      return {
        code: HttpStatus.OK,
        success: true,
        message: '',
        data: await next(),
      };
    };
  }
}
