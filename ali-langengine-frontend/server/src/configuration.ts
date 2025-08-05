// 需要在所有代码之前执行
require('dotenv').config({ path: '.env' });

import * as axios from '@midwayjs/axios';
import { App, Configuration, ILifeCycle } from '@midwayjs/core';
import * as typeorm from '@midwayjs/typeorm';
import * as view from '@midwayjs/view-ejs';
import * as egg from '@midwayjs/web';
import { join } from 'path';
import { ErrorFilter } from './filters/error';
import { FormatMiddleware } from './middlewares/format';
import { FrontendMiddleware } from './middlewares/frontend';
import { UserMiddleware } from './middlewares/user';

@Configuration({
  imports: [egg, view, typeorm, axios],
  importConfigs: [join(__dirname, './config')],
})
export class MainConfiguration implements ILifeCycle {
  @App('egg')
  app: egg.Application;

  async onReady() {
    this.app.useMiddleware([UserMiddleware, FrontendMiddleware, FormatMiddleware]);
    this.app.useFilter([ErrorFilter]);
  }
}
