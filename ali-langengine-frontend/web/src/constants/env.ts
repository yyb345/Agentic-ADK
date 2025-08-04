type Env = 'local' | 'prod';

export const ENV = ({
  ['http://localhost:8000']: 'local',
  ['http://localhost']: 'prod',
}[window.location.origin] || 'local') as Env;
