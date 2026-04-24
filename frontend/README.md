# VideoMiner Frontend

Single-page frontend for the VideoMiner ecosystem:

- **Ingestion**: VimeoMiner and YouTubeMiner fetch/publish endpoints
- **Endpoint Studio**: dynamic runner for public endpoints across all microservices
- **Data Explorer**: paginated browsing for VideoMiner entities

## Scripts

```bash
npm install
npm run dev
npm run build
npm run preview
```

## Docker Development (Hot Reload)

From repository root:

```bash
cp .env.dev.example .env.dev
docker compose --env-file .env.dev -f docker-compose.yml -f docker-compose.dev.yml up --build
```

Then open `http://localhost:5173` (or `FRONTEND_PORT` from `.env.dev`).

In this mode, the frontend runs in a Docker container with:

- bind-mounted source code for live edits
- Vite dev server + HMR
- API proxy to compose services via internal hostnames

## Default Service Routes

The UI uses these base URLs by default:

- `videominer`: `/api/videominer`
- `vimeominer`: `/api/vimeominer`
- `youtubeminer`: `/api/youtubeminer`

In Vite development, each route is proxied to local ports:

- `/api/videominer` -> `http://localhost:8080`
- `/api/vimeominer` -> `http://localhost:8081`
- `/api/youtubeminer` -> `http://localhost:8082`

You can override targets with environment variables:

- `VITE_VIDEOMINER_TARGET`
- `VITE_VIMEOMINER_TARGET`
- `VITE_YOUTUBEMINER_TARGET`

## Docker Image

`frontend/Dockerfile` is a multistage build:

1. Build SPA with Node (`npm ci`, `npm run build`)
2. Serve static files with Nginx
3. Proxy `/api/*` to the internal Docker services

Standalone build example:

```bash
docker build -t videominer-frontend ./frontend
docker run --rm -p 8088:80 videominer-frontend
```
