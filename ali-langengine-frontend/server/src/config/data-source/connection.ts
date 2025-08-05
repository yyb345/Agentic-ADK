require('dotenv').config({ path: '.env' });

import { join } from 'path';
import { DataSource } from 'typeorm';
import { dataSourceOptions } from './options';

export default new DataSource({
  ...dataSourceOptions,

  // 给 migrations 创建连接的时候要使用绝对路径
  entities: [join(__dirname, '../../../entities/**/*.ts')],
  migrations: [join(__dirname, '../../../migrations/**/*.ts')],
});
