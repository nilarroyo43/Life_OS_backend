Esto es un readme compartido con todos los repositoriso, para poder generar el dockercompose desde cualquier repo
Docker-compose.yml:
services:

  # ── Base de datos ────────────────────────────────────────────────────────────
  db:
    image: postgres:16-alpine
    container_name: lifeos-db
    environment:
      POSTGRES_DB: lifeos_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - lifeos-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d lifeos_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ── Backend (Spring Boot) ────────────────────────────────────────────────────
  backend:
    build:
      context: ./Life_OS_backend
    container_name: lifeos-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/lifeos_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: admin
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
    depends_on:
      db:
        condition: service_healthy
    networks:
      - lifeos-network
    restart: on-failure

  # ── Frontend (Angular + Nginx) ───────────────────────────────────────────────
  frontend:
    build:
      context: ./Life_OS_frontend
    container_name: lifeos-frontend
    ports:
      - "4200:80"
    depends_on:
      - backend
    networks:
      - lifeos-network
    restart: on-failure

networks:
  lifeos-network:
    driver: bridge

volumes:
  postgres_data:


Makefile:
.PHONY: up down build rebuild logs ps clean

## Levantar todos los servicios en segundo plano
up:
	docker compose up -d

## Detener todos los servicios
down:
	docker compose down

## Construir las imágenes sin levantar los servicios
build:
	docker compose build

## Reconstruir las imágenes forzando el no uso de caché y levantar
rebuild:
	docker compose build --no-cache
	docker compose up -d

## Ver logs en tiempo real de todos los servicios
logs:
	docker compose logs -f

## Ver el estado de los contenedores
ps:
	docker compose ps

## Detener contenedores, eliminar volúmenes e imágenes generadas
clean:
	docker compose down -v --rmi local
