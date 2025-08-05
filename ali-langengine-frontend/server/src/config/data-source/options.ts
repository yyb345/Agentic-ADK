import { DataSourceOptions } from 'typeorm';
import { SnakeNamingStrategy } from 'typeorm-naming-strategies';
import { ENV } from '../../constants/env';

const host = ENV === 'local' ? process.env.POSTGRES_HOST_LOCAL : process.env.POSTGRES_HOST;

console.log('[data-source] host:', host);

export const dataSourceOptions: DataSourceOptions = {
  type: 'postgres',
  host,
  port: parseInt(process.env.POSTGRES_PORT),
  username: process.env.POSTGRES_USER,
  password: process.env.POSTGRES_PASSWORD,
  database: process.env.POSTGRES_DATABASE,
  entities: ['entities/**/*.{js,ts}'],
  migrations: ['migrations/**/*.{js,ts}'],
  namingStrategy: new SnakeNamingStrategy(),
};
