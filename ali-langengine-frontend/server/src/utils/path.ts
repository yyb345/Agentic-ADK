// 接口路径
export function isApiPath(path: string) {
  return path.startsWith('/api/') || ['/healthz', '/debug'].includes(path);
}

// 页面路径
export function isPagePath(path: string) {
  return !isApiPath(path);
}

// 公开路径（即不需要登录就可以访问）
export function isPublicPath(path: string) {
  return [
    '/',
    '/welcome',
    '/healthz',
    '/debug',
    // ...
  ].includes(path);
}
