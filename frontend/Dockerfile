# ----------------------------
#  Frontend – React (Vite) + Nginx
# ----------------------------
# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json .
COPY pnpm-lock.yaml .
COPY vite.config.ts .
COPY tsconfig.json .
COPY public ./public
COPY src ./src
# Install pnpm (outside sandbox rule 6 – we already have pnpm installed globally on host)
RUN npm i -g pnpm && pnpm install && pnpm run build

# Runtime stage – serve with nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Expose default HTTP port
EXPOSE 80
# No entrypoint needed – nginx runs by default

