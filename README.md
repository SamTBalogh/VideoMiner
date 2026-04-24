# VideoMiner System — G7

Monorepo with three Spring Boot microservices, one PostgreSQL database, and one frontend SPA.

## Architecture

```text
Browser ──► Frontend (Nginx) ──► /api/vimeominer  ─► VimeoMiner   (8081) ──┐
                            └──► /api/youtubeminer ─► YoutubeMiner (8082) ──┤
                            └──► /api/videominer   ─► VideoMiner   (8080) ──┴──► PostgreSQL (5432)
```

| Module | Description |
|---|---|
| `VideoMiner` | Central REST API. Persists channels, videos, comments, captions and users in PostgreSQL. |
| `VimeoMiner` | Adapter that fetches data from the Vimeo API and publishes it to VideoMiner. |
| `YoutubeMiner` | Adapter that fetches data from the YouTube Data API v3 and publishes it to VideoMiner. |
| `frontend` | React SPA served by Nginx. Proxies `/api/*` calls to the internal microservices. |

## Prerequisites

- Docker
- Docker Compose (`docker compose`)

## Environments

- `docker-compose.yml`: shared/base services
- `docker-compose.dev.yml`: development overrides
- `docker-compose.prod.yml`: production overrides

## Environment Variables

Create env files from the templates:

```bash
cp .env.dev.example .env.dev
cp .env.prod.example .env.prod
```

Fill at least:

- `VIMEO_TOKEN`
- `YOUTUBE_API_TOKEN`
- `POSTGRES_PASSWORD` (especially in production)

## Run Development

```bash
docker compose --env-file .env.dev -f docker-compose.yml -f docker-compose.dev.yml up --build
```

In development, Java services run with source bind mounts plus `scripts/dev-java-runner.sh` in each microservice container.
This means source changes in `src/main` are compiled automatically and applied without restarting containers manually.

If you want to tweak the polling interval, set `DEV_WATCH_INTERVAL_SECONDS` in the container environment.

For best file-change detection in WSL, keep the repository inside the Linux filesystem (example: `~/projects/VideoMiner`) instead of `/mnt/c/...`.

If you moved the project or changed the compose setup, force recreation once:

```bash
docker compose --env-file .env.dev -f docker-compose.yml -f docker-compose.dev.yml up --build --force-recreate
```

Or use the helper script:

```bash
bash scripts/dev-recreate-clean.sh
```

To recreate clean and also reset DB volume:

```bash
bash scripts/dev-recreate-clean.sh --drop-db
```

To recreate only modified microservices (based on current git changes):

```bash
bash scripts/dev-recreate-changed.sh
```

To recreate only one specific microservice:

```bash
bash scripts/dev-recreate-changed.sh --service youtubeminer
```

`dev-recreate-changed.sh` recreates only target microservices (`--no-deps`).
If required dependencies are stopped, it warns and exits.

This starts:

- PostgreSQL exposed on `localhost:5432`
- VideoMiner on `localhost:8080`
- VimeoMiner on `localhost:8081`
- YoutubeMiner on `localhost:8082`
- Frontend (Vite + hot reload) on `localhost:5173` (or `FRONTEND_PORT`)

Frontend development now runs inside Docker too. The `frontend` dev container:

- mounts the repository source (`./` -> `/workspace`)
- installs dependencies with `npm ci` when `package-lock.json` changes
- runs Vite dev server with `--host 0.0.0.0` for browser access
- proxies `/api/*` to internal compose services (`videominer`, `vimeominer`, `youtubeminer`)

## Run Production

```bash
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

In production mode:

- PostgreSQL is internal-only (not exposed to host)
- Java microservices are internal-only (not exposed to host)
- Frontend is exposed on `http://localhost:${FRONTEND_PORT}` (default `80`)

## Stop Environment

```bash
docker compose --env-file .env.dev -f docker-compose.yml -f docker-compose.dev.yml down
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml down
```

To also remove PostgreSQL data volume:

```bash
docker compose --env-file .env.dev -f docker-compose.yml -f docker-compose.dev.yml down -v
```

## API Documentation (Swagger UI)

In development mode (`docker-compose.dev.yml`), Swagger UI is available at:

| Service | URL |
|---|---|
| VideoMiner | http://localhost:8080/swagger-ui/index.html |
| VimeoMiner | http://localhost:8081/swagger-ui/index.html |
| YoutubeMiner | http://localhost:8082/swagger-ui/index.html |

## Frontend (VideoMiner Console)

The repository now includes a lightweight React + TypeScript frontend in `frontend/`.
It provides:

- Ingestion workflows for VimeoMiner and YouTubeMiner (GET preview + POST publish)
- A dynamic endpoint studio covering public endpoints in all microservices
- A VideoMiner data explorer with pagination, filtering, and ordering

### Run Frontend in Development

You have two options:

1. **All Docker (recommended):** use the standard development compose command above and open `http://localhost:5173`.
2. **Local Node frontend + Docker backend:** run frontend manually as shown below.

Prerequisite for option 2: Node.js 20+ and npm.

```bash
cd frontend
npm install
npm run dev
```

By default, the app runs at `http://localhost:5173` and uses Vite dev proxies:

- `/api/videominer` -> `http://localhost:8080`
- `/api/vimeominer` -> `http://localhost:8081`
- `/api/youtubeminer` -> `http://localhost:8082`

If your ports differ, adjust the base URLs in the app settings panel or start Vite with:

```bash
VITE_VIDEOMINER_TARGET=http://localhost:8080 \
VITE_VIMEOMINER_TARGET=http://localhost:8081 \
VITE_YOUTUBEMINER_TARGET=http://localhost:8082 \
npm run dev
```

### Frontend in Production (Docker)

The production compose setup builds `frontend/Dockerfile` and serves the SPA with Nginx.
Nginx also proxies API calls to internal services:

- `/api/videominer/*` -> `videominer:8080`
- `/api/vimeominer/*` -> `vimeominer:8081`
- `/api/youtubeminer/*` -> `youtubeminer:8082`

Set `FRONTEND_PORT` in `.env.prod` to choose the exposed host port (default `80`).
