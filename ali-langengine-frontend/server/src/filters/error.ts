import { Catch, MidwayHttpError } from '@midwayjs/core';
import { Context } from '@midwayjs/web';

@Catch()
export class ErrorFilter {
  async catch(e: MidwayHttpError, ctx: Context) {
    ctx.logger.error('ErrorFilter: ', e);
    return {
      code: e.status || e.code || 500,
      success: false,
      message: e.message || '服务器出错了',
      data: null,
    };
  }
}
