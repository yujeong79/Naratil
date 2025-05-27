# 1단계: Vue 빌드
FROM node:18 AS builder
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

# 2단계: Nginx에 정적 파일 복사
FROM nginx:latest
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
