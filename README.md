# VideoMiner System — G7

Monorepo with three Spring Boot microservices and one PostgreSQL database.

## Architecture

```
Vimeo API ──────► VimeoMiner   (8081) ──┐
                                        ├──► VideoMiner (8080) ───► PostgreSQL (5432)
YouTube API ────► YoutubeMiner (8082) ──┘
```

| Module | Description |
|---|---|
| `VideoMiner` | Central REST API. Persists channels, videos, comments, captions and users in PostgreSQL. |
| `VimeoMiner` | Adapter that fetches data from the Vimeo API and publishes it to VideoMiner. |
| `YoutubeMiner` | Adapter that fetches data from the YouTube Data API v3 and publishes it to VideoMiner. |

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

## Run Production

```bash
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

In production mode, PostgreSQL is internal-only (not exposed to host).

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

| Service | URL |
|---|---|
| VideoMiner | http://localhost:8080/swagger-ui/index.html |
| VimeoMiner | http://localhost:8081/swagger-ui/index.html |
| YoutubeMiner | http://localhost:8082/swagger-ui/index.html |
