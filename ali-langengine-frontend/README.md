# 前后端分离项目

本仓库（Monorepo）由 `pnpm` 管理，请务必使用 `pnpm@9.15.0` 进行安装依赖。

```bash
# 安装或更新 pnpm 版本
$ curl -fsSL https://get.pnpm.io/install.sh | env PNPM_VERSION=9.15.0 sh -
```

## 安装依赖

```bash
# 首次安装
$ pnpm install

# 重新安装
$ pnpm reinstall
```

## 本地直接启动

```bash
$ pnpm start
```

## 本地开发

需要先创建 `server/.env` 文件。

```
POSTGRES_HOST_LOCAL=localhost
POSTGRES_HOST=ali-langengine-frontend-postgres
POSTGRES_PORT=5432
POSTGRES_DATABASE=ali_langengine_frontend_database
POSTGRES_USER=ali_langengine_frontend_user
POSTGRES_PASSWORD=ali_langengine_frontend_password
```

然后启动本地数据库：

```bash
$ pnpm start:db
```

然后启动本地应用：（同时启动 umi 和 midway 项目）

```bash
$ pnpm dev
```

启动后，访问 [http://localhost:8000](http://localhost:8000) 进行前端预览调试，后端接口已配置 proxy 转发。
