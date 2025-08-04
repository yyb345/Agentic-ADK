FROM node:20.18.0

WORKDIR /app

COPY . .

RUN npm install -g pnpm

RUN pnpm install

RUN pnpm build

ENV PORT=10000

EXPOSE 10000

CMD ["pnpm", "start"]
