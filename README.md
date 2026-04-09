# IPL Player Analysis Backend

A comprehensive backend service for analyzing IPL (Indian Premier League) players, matches, and generating AI-powered Fantasy XI predictions. Built with Spring Boot, PostgreSQL, Redis, and integrated with Grok AI and CricAPI.

## 🚀 Features

- **Live Match Tracking**: Fetches live scores and match schedules for the IPL directly from CricAPI v1.
- **Player Statistics**: Automatically parses and caches T20 and IPL-specific player statistics.
- **AI Fantasy Analyst**: Integrates with `xAI`'s **Grok-3** model to analyze match scenarios, ground conditions, and player form to generate expert Fantasy XI recommendations.
- **Caching & Performance**: Employs Redis caching for API limit preservation and instant endpoint responses.
- **1-Click Cloud Deployment**: Packaged with a native `render.yaml` for a zero-configuration deployment to Render, complete with databases and auto-linking.

## 🛠 Tech Stack

- **Java 21**
- **Spring Boot 3.3.4** (Web, Data JPA, Validation, WebFlux)
- **PostgreSQL** (Primary database)
- **Flyway** (Database migrations)
- **Redis / Valkey** (Caching layer via Spring Data Redis)
- **CricAPI (v1)** - External sports data provider
- **xAI Grok** - Grok-3 API for NLP Fantasy AI Generation
- **Docker** - Multi-stage build for easy compilation and hosting

## 🔑 Environment Variables

To run the application locally or on a server, you must provide the following environment variables:

| Variable | Description | Default / Fallback |
|----------|-------------|--------------------|
| `DATABASE_URL` | PostgreSQL connection string (`jdbc:postgresql://...`) | *None (Required)* |
| `GROK_API_KEY` | Your xAI API Key for Grok predictions | `changeme` |
| `CRICAPI_KEY` | CricAPI v1 Lifetime Free API Key | `changeme` |
| `REDIS_HOST` | Redis Server Host | `localhost` |
| `REDIS_PORT` | Redis Server Port | `6379` |
| `REDIS_PASSWORD`| Redis Password (if applicable) | *(Empty)* |
| `FRONTEND_URL` | Allowed origin for CORS (React App URL) | `http://localhost:5173` |
| `JWT_SECRET` | 256-bit secret for Auth JWT token signing | `fallbacksecretfordevonly...`|

*(Note: If you deploy via Render Blueprint, all variables except the API keys and Frontend URL are securely auto-generated and linked for you.)*

## 🌩 Deployment (Render)

This project has native support for **Render's Infrastructure as Code (IaC)**. 

1. Simply login to Render and use this 1-click deploy link:
   👉 **[Deploy to Render](https://render.com/deploy?repo=https://github.com/Divine124/ipl-player-analysis-backend)**
2. Provide a name, and paste in your `CRICAPI_KEY`, `GROK_API_KEY`, and `FRONTEND_URL` (`*` allows all).
3. Click **Apply**. 

Render will automatically provision the PostgreSQL instance, the Redis instance, and build your Spring Boot Docker container.

## 💻 Local Setup

1. **Prerequisites**: Ensure you have Java 21, PostgreSQL, and Redis running locally.
2. **Database Setup**: Create an empty Postgres database named `ipl-db`.
3. **Set Environment**: Run this in your terminal or configure your IDE run profile:
   ```bash
   export DATABASE_URL="jdbc:postgresql://localhost:5432/ipl-db"
   export DATABASE_USERNAME="your-postgres-user"
   export DATABASE_PASSWORD="your-postgres-password"
   export CRICAPI_KEY="your-cricapi-key"
   export GROK_API_KEY="your-grok-key"
   ```
4. **Compile & Run**:
   ```bash
   ./mvnw spring-boot:run
   ```
   
Flyway migrations will automatically build the tables (`matches`, `players`, `player_stats`, etc.) on the first successful application boot!
