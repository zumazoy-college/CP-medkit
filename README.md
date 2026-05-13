# Medkit - Medical Management System

## Структура проекта

```bash
medkit/
├── backend/         # Spring Boot API
├── frontend/        # React + TypeScript веб-приложение
└── medkit_app/      # Flutter мобильное приложение
```

## Настройка .env

### Backend

1. Скопируйте `.env.example` в `.env`:

   ```bash
   cd backend
   cp .env.example .env
   ```

2. Заполните реальные данные в `.env`

### Frontend

- Скопируйте `.env.example` в `.env`:

   ```bash
   cd frontend
   cp .env.example .env
   ```

## Быстрый старт

### Требования

- Java 21
- Node.js 16+
- PostgreSQL 14+
- Flutter 3.0+

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

### Mobile App

```bash
cd medkit_app
flutter pub get
flutter run
```

## API Documentation

После запуска backend:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
