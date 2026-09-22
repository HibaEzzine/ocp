# ==========================================
# Étape 1 : Build Node.js (React + Vite)
# ==========================================
FROM node:20-alpine AS build
WORKDIR /app

# Installation des dépendances
COPY package*.json ./
RUN npm ci

# Copie des fichiers source et build de production
COPY . .
ENV VITE_API_BASE_URL=/api
RUN npm run build

# ==========================================
# Étape 2 : Serveur Nginx Production
# ==========================================
FROM nginx:alpine

# Copie du build statique React vers le dossier HTML Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copie de la configuration Nginx avec Reverse Proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
