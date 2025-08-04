type Env = 'local' | 'prod';

export const ENV = ({
  local: 'local',
  production: 'prod',
}[process.env.NODE_ENV] || 'prod') as Env;
