# This is the building fase 
FROM node:20 AS build
WORKDIR /app
COPY package*.json .
RUN npm ci
COPY . .
RUN npm run build

# This is the running fase
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]