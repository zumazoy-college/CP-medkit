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

1. Файл `.env` уже настроен для локальной разработки

### Mobile App

- Для локальной разработки настройка не требуется

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
