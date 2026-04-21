# VideoMiner System — G7

Monorepo that contains three Spring Boot microservices that together form the **VideoMiner** platform: a system for harvesting video content from external APIs and storing it in a unified format.

## Architecture

```
Vimeo API ──────► VimeoMiner   (port 8081) ──┐
                                                  ├──► VideoMiner (port 8080)
YouTube API ────► YoutubeMiner (port 8082) ──┘        (H2 database)
```

| Module | Description |
|---|---|
| `VideoMiner` | Central REST API. Stores channels, videos, comments, captions and users in an H2 database. |
| `VimeoMiner` | Adapter that fetches data from the **Vimeo API** and posts it to VideoMiner. |
| `YoutubeMiner` | Adapter that fetches data from the **YouTube Data API v3** and posts it to VideoMiner. |

## Prerequisites

- Java 17+
- Maven 3.9+

## Configuration

Before running the adapter services, set your API tokens in their `application.properties` files:

**`VimeoMiner/src/main/resources/application.properties`**
```properties
vimeo.token=YOUR_VIMEO_TOKEN
```

**`YoutubeMiner/src/main/resources/application.properties`**
```properties
youtube.api.token=YOUR_YOUTUBE_API_KEY
```

## Build & Run

### Build all modules from the root

```bash
mvn clean install
```

### Run each service (in separate terminals)

```bash
# 1 — Start the central storage service first
cd VideoMiner
mvn spring-boot:run

# 2 — Start the Vimeo adapter
cd VimeoMiner
mvn spring-boot:run

# 3 — Start the YouTube adapter
cd YoutubeMiner
mvn spring-boot:run
```

## API Documentation (Swagger UI)

| Service | URL |
|---|---|
| VideoMiner | http://localhost:8080/swagger-ui/index.html |
| VimeoMiner | http://localhost:8081/swagger-ui/index.html |
| YoutubeMiner | http://localhost:8082/swagger-ui/index.html |

## H2 Console

The VideoMiner database can be inspected at: http://localhost:8080/h2-ui
