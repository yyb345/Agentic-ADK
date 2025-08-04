import { Controller, Get, Inject } from '@midwayjs/core';
import { Context } from '@midwayjs/web';

@Controller('/')
export class IndexController {
  @Inject()
  ctx: Context;

  @Get('/healthz')
  async healthz() {
    return 'ok';
  }

  @Get('/debug')
  async debug() {
    return {
      method: this.ctx.method,
      origin: this.ctx.origin,
      protocol: this.ctx.protocol,
      hostname: this.ctx.hostname,
      path: this.ctx.path,
      query: this.ctx.query,
      headers: this.ctx.headers,
      ctx: this.ctx,
    };
  }
}
