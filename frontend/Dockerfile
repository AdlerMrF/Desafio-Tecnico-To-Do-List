# ---------- Etapa 1: build do Angular com Node ----------
FROM node:24-slim AS build
WORKDIR /app

# Dependências primeiro, para o Docker reaproveitar a camada enquanto o package-lock.json não mudar.
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
# Build de produção: usa o environment.ts, com apiUrl = '/api'.
RUN npx ng build

# ---------- Etapa 2: arquivos estáticos servidos pelo nginx ----------
FROM nginx:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80
