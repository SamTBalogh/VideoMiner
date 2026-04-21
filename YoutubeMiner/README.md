# YoutubeMiner

A Spring Boot microservice that acts as a **data bridge** between the [YouTube Data API v3](https://developers.google.com/youtube/v3) and the [VideoMiner](../VideoMiner/README.md) storage service. It fetches channel, video, caption, and comment data from YouTube, assembles it into a unified model, and optionally publishes it to VideoMiner via its REST API.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Reference](#api-reference)
  - [v1 Endpoints (Search-based)](#v1-endpoints-search-based)
  - [v2 Endpoints (Uploads-playlist-based)](#v2-endpoints-uploads-playlist-based)
- [Data Model](#data-model)
- [Service Layer](#service-layer)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Interactive API Docs](#interactive-api-docs)

---

## Overview

YoutubeMiner exposes 8 REST endpoints under `/youTubeMiner`. Each endpoint retrieves structured content from YouTube and returns it in the VideoMiner-compatible JSON format. `POST` endpoints additionally forward the assembled data to a running VideoMiner instance.

Two API versions are provided:

| Version | Strategy | Notes |
|---------|----------|-------|
| **v1** | YouTube Search API (`/search`) | Retrieves videos via search results for a channel |
| **v2** | Uploads playlist API (`/playlistItems`) | Retrieves videos from the channel's official uploads playlist — more reliable and complete |

---

## Architecture

```
┌─────────────────────────────────────────┐
│             ChannelController           │
│         (GET / POST, v1 & v2)           │
└────────────────┬────────────────────────┘
                 │
     ┌───────────┴────────────┐
     │  ChannelAssemblerService│   ← orchestrates the full assembly pipeline
     └───────────┬────────────┘
                 │
     ┌───────────┼──────────────────────────┐
     │           │                          │
┌────▼────┐ ┌────▼────┐  ┌────────────┐  ┌──▼──────────┐  ┌──────────────┐
│Channel  │ │ Video   │  │  Caption   │  │   Comment   │  │   Upload     │
│Service  │ │ Service │  │  Service   │  │   Service   │  │   Service    │
└────┬────┘ └────┬────┘  └─────┬──────┘  └──────┬──────┘  └──────┬───────┘
     │           │             │                 │                 │
     └───────────┴─────────────┴─────────────────┴─────────────────┘
                              │
                   YouTube Data API v3
                (https://youtube.googleapis.com/youtube/v3)

                              │ (POST endpoints only)
               ┌──────────────▼──────────────┐
               │  VideoMinerPublisherService  │
               └──────────────┬──────────────┘
                              │
                    VideoMiner REST API
               (http://localhost:8080/videoMiner/v1)
```

---

## Requirements

- Java 17
- Maven 3.6+
- A valid **YouTube Data API v3** key
- A running **VideoMiner** instance (only required for `POST` endpoints)

---

## Configuration

All configuration lives in `src/main/resources/application.properties`:

```properties
spring.application.name=YoutubeMiner
server.port=8082

youtube.api.uri=https://youtube.googleapis.com/youtube/v3
videoMiner.url=http://localhost:8080/videoMiner/v1

server.error.include-message=always

# Inject your API key via environment variable
youtube.api.token=${YOUTUBE_API_TOKEN:}
```

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Port the service listens on | `8082` |
| `youtube.api.uri` | YouTube Data API v3 base URL | `https://youtube.googleapis.com/youtube/v3` |
| `videoMiner.url` | VideoMiner base URL for publishing | `http://localhost:8080/videoMiner/v1` |
| `youtube.api.token` | YouTube API key (read from `YOUTUBE_API_TOKEN` env var) | _(empty)_ |

Set your YouTube API key before starting:

```bash
# Linux / macOS
export YOUTUBE_API_TOKEN=your_api_key_here

# Windows (PowerShell)
$env:YOUTUBE_API_TOKEN = "your_api_key_here"
```

---

## Running the Application

```bash
cd YoutubeMiner
./mvnw spring-boot:run
```

Or from the repository root:

```bash
mvn spring-boot:run -pl YoutubeMiner
```

The service starts on **http://localhost:8082**.

---

## API Reference

All endpoints are under the base path `/youTubeMiner`. The `Authorization` header is optional on all `POST` endpoints and is forwarded to VideoMiner as-is.

### v1 Endpoints (Search-based)

Videos are retrieved using the YouTube Search API (`/search?channelId=...`).

---

#### `GET /youTubeMiner/v1/{id}`

Fetches a single channel by its YouTube channel ID, including videos, captions, and comments.

**Path parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | YouTube channel ID (e.g. `UCX6OQ3DkcsbYNE6H8uQQuVA`) |

**Query parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `maxVideos` | `Integer` | `10` | Maximum number of videos to retrieve |
| `maxComments` | `Integer` | `10` | Maximum number of comments per video |

**Responses**

| Status | Description |
|--------|-------------|
| `200 OK` | Channel JSON object |
| `403 Forbidden` | YouTube API quota exceeded |
| `404 Not Found` | Channel ID not found |

---

#### `POST /youTubeMiner/v1/{id}`

Same as `GET /youTubeMiner/v1/{id}` but also **publishes** the assembled channel to VideoMiner.

**Path parameters** — same as GET above.

**Query parameters** — same as GET above.

**Request headers**

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | No | Token forwarded to VideoMiner for authorization |

**Responses**

| Status | Description |
|--------|-------------|
| `201 Created` | Channel published and returned as JSON |
| `403 Forbidden` | YouTube quota exceeded or VideoMiner rejected the request |
| `404 Not Found` | Channel ID not found |

---

#### `GET /youTubeMiner/v1/channels`

Searches YouTube for channels matching a name and returns all of them assembled with videos, captions, and comments.

**Query parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `name` | `String` | _(required)_ | Search query for channel names |
| `maxChannels` | `Integer` | `3` | Maximum number of channels to return |
| `maxVideos` | `Integer` | `10` | Maximum videos per channel |
| `maxComments` | `Integer` | `10` | Maximum comments per video |

**Responses**

| Status | Description |
|--------|-------------|
| `200 OK` | JSON array of Channel objects |
| `403 Forbidden` | YouTube API quota exceeded |
| `404 Not Found` | No channels found for the given name |

---

#### `POST /youTubeMiner/v1/channels`

Same as `GET /youTubeMiner/v1/channels` but also **publishes** all assembled channels to VideoMiner.

**Query parameters** — same as GET above, plus:

**Request headers**

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | No | Token forwarded to VideoMiner |

**Responses**

| Status | Description |
|--------|-------------|
| `201 Created` | JSON array of published Channel objects |
| `403 Forbidden` | YouTube quota exceeded or VideoMiner rejected the request |
| `404 Not Found` | No channels found |

---

### v2 Endpoints (Uploads-playlist-based)

Videos are retrieved via the channel's **uploads playlist** (`/playlistItems?playlistId=...`), which gives access to the full upload history and avoids search result limitations.

---

#### `GET /youTubeMiner/v2/{id}`

Fetches a channel by ID using the uploads playlist strategy.

**Path / Query parameters** — identical to v1 single-channel GET.

**Responses**

| Status | Description |
|--------|-------------|
| `200 OK` | Channel JSON object |
| `403 Forbidden` | YouTube API quota exceeded |
| `404 Not Found` | Channel or uploads playlist not found |

---

#### `POST /youTubeMiner/v2/{id}`

Fetches and publishes a channel using the uploads playlist strategy.

**Path / Query / Header parameters** — identical to v1 single-channel POST.

**Responses**

| Status | Description |
|--------|-------------|
| `201 Created` | Channel published and returned as JSON |
| `403 Forbidden` | YouTube quota exceeded or VideoMiner rejected the request |
| `404 Not Found` | Channel or uploads playlist not found |

---

#### `GET /youTubeMiner/v2/channels`

Searches YouTube for channels by name and fetches each one's videos via their uploads playlist.

**Query parameters** — identical to v1 channel-list GET.

**Responses**

| Status | Description |
|--------|-------------|
| `200 OK` | JSON array of Channel objects |
| `403 Forbidden` | YouTube API quota exceeded |
| `404 Not Found` | No channels found |

---

#### `POST /youTubeMiner/v2/channels`

Searches, assembles via uploads playlist, and publishes all matching channels to VideoMiner.

**Query / Header parameters** — identical to v1 channel-list POST.

**Responses**

| Status | Description |
|--------|-------------|
| `201 Created` | JSON array of published Channel objects |
| `403 Forbidden` | YouTube quota exceeded or VideoMiner rejected the request |
| `404 Not Found` | No channels found |

---

## Data Model

The service maps YouTube API responses to the VideoMiner data model:

```
Channel
├── id           (YouTube channel ID)
├── name         (channel title)
├── description
├── createdTime  (ISO 8601)
└── videos[]
    ├── id           (YouTube video ID)
    ├── name         (video title)
    ├── description
    ├── releaseTime  (ISO 8601)
    ├── captions[]
    │   ├── id
    │   ├── language
    │   └── name
    └── comments[]
        ├── id
        ├── text        (top-level comment text)
        ├── createdOn
        ├── author
        └── authorUrl
```

---

## Service Layer

| Service | Responsibility | YouTube API endpoint(s) |
|---------|---------------|------------------------|
| `ChannelService` | Fetch channel metadata by ID or by name search | `/channels`, `/search?type=channel` |
| `VideoService` | Fetch videos by channel ID (search) or by video ID | `/search?type=video`, `/videos` |
| `CaptionService` | Fetch captions for a given video | `/captions` |
| `CommentService` | Fetch comment threads for a given video | `/commentThreads` |
| `UploadService` | Fetch video IDs from a channel's uploads playlist | `/playlistItems` |
| `ChannelAssemblerService` | Orchestrate the full v1/v2 assembly pipeline | _(delegates to above)_ |
| `VideoMinerPublisherService` | POST assembled channels to VideoMiner REST API | _(VideoMiner `/channels`)_ |

### v1 vs v2 Assembly Pipeline

**v1:**
```
ChannelService.findChannelById(id)
  └─ VideoService.findSearchVideosMaxChannelId(channelId, maxVideos)
       └─ per video:
            ├─ CommentService.findCommentsByVideoIdMax(videoId, maxComments)
            └─ CaptionService.findCaptionsByVideoId(videoId)
```

**v2:**
```
ChannelService.findChannelByIdContentDetails(id)   ← also reads uploadsPlaylistId
  └─ UploadService.findUploadsIdsMax(uploadsId, maxVideos)
       └─ per videoId:
            VideoService.findVideoById(videoId)
              └─ CommentService.findCommentsByVideoIdMax(videoId, maxComments)
              └─ CaptionService.findCaptionsByVideoId(videoId)
```

---

## Error Handling

Exceptions are mapped to HTTP status codes via `@ResponseStatus` annotations:

| Exception | HTTP Status | Trigger |
|-----------|-------------|---------|
| `ChannelNotFoundException` | `404 Not Found` | Channel ID not found in YouTube |
| `VideoNotFoundException` | `404 Not Found` | Video ID not found |
| `VideoNotFoundChannelIDException` | `404 Not Found` | Search returned no videos for a channel |
| `CaptionNotFoundException` | `404 Not Found` | YouTube returns 404 for captions |
| `CommentNotFoundException` | `404 Not Found` | YouTube returns 404 for comments |
| `UploadsNotFoundException` | `404 Not Found` | Uploads playlist ID is invalid |
| `ListChannelsNotFoundException` | `404 Not Found` | No channels match the search query |
| `ForbiddenException` | `403 Forbidden` | YouTube API quota exceeded, or VideoMiner rejects the token |

> **Note:** If a video has comments disabled on YouTube, `CommentService` silently returns an empty list (HTTP 403 from YouTube is swallowed). Only a true 404 from YouTube raises `CommentNotFoundException`.

---

## Testing

The test suite uses **JUnit 5 + Mockito** and does not require a real YouTube API key or a running VideoMiner instance.

```bash
# Run only YoutubeMiner tests
mvn test -pl YoutubeMiner
```

| Test class | Tests | Description |
|------------|-------|-------------|
| `ChannelControllerTest` | 10 | `@WebMvcTest` — all 8 endpoints + 2 error scenarios |
| `ChannelAssemblerServiceTest` | 4 | v1/v2 single and list assembly |
| `ChannelServiceTest` | 6 | findById, findByIdContentDetails, findByNameMax (success + not found) |
| `VideoServiceTest` | 4 | findSearchVideosMax, findVideoById (success + error) |
| `CaptionServiceTest` | 2 | success + 404 |
| `CommentServiceTest` | 3 | success, 403 (disabled), 404 |
| `UploadServiceTest` | 2 | success + 400 |
| `VideoMinerPublisherServiceTest` | 4 | publish success, with token, 403, publishAll |
| `YoutubeMinerApplicationTests` | 1 | Spring context loads |
| **Total** | **36** | All pass with `BUILD SUCCESS` |

---

## Interactive API Docs

Once the service is running, Swagger UI is available at:

```
http://localhost:8082/swagger-ui/index.html
```

The OpenAPI specification (JSON) is at:

```
http://localhost:8082/v3/api-docs
```
