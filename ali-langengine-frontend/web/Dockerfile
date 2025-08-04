# 第一阶段：构建静态资源

FROM node:20.18.0 AS builder

WORKDIR /app

COPY . .

RUN npm install -g pnpm

RUN pnpm install

RUN pnpm build

# 第二阶段：托管静态资源

FROM nginx:latest

COPY --from=builder /app/dist /usr/share/nginx/static

COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
