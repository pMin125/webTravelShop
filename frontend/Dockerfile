# 1단계: Node 빌드 스테이지
FROM node:18 AS build
WORKDIR /app

# ✅ 소스맵 안 만들도록 환경 변수 설정
ENV GENERATE_SOURCEMAP=false

# ✅ 패키지 설치 시 devDependencies 제외
COPY package*.json ./
RUN npm ci --omit=dev  # ⬅️ npm install → npm ci + --omit=dev 로 변경

# ✅ 그 다음 전체 소스 복사
COPY . .

# ✅ 리액트 앱 빌드
RUN npm run build

# 2단계: Nginx
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html