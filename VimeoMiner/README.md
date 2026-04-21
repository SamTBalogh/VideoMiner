# VimeoMiner

VimeoMiner is a Spring Boot microservice that acts as a **data collector bridge** between the [Vimeo API](https://developer.vimeo.com/) and the VideoMiner API. It fetches channel, video, caption, and comment data from Vimeo, normalises it into VideoMiner's data model, and optionally publishes the assembled result to the VideoMiner API.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Data Model](#data-model)
- [API Endpoints](#api-endpoints)
- [Services](#services)
- [Exception Handling](#exception-handling)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Running the Tests](#running-the-tests)
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui)

---

## Architecture Overview

```
Client
  │
  ▼
ChannelController          (REST layer — thin, no business logic)
  │           │
  ▼           ▼
ChannelAssemblerService    VideoMinerPublisherService
  │                             │
  ├── ChannelService            └── RestTemplate → VideoMiner API
  ├── VideoService                   (POST /channels)
  ├── CaptionService
  └── CommentService
        │
        ▼
    RestTemplate → Vimeo API (https://api.vimeo.com)
```

The controller only orchestrates; all Vimeo interaction is handled by dedicated services. The `ChannelAssemblerService` composes the full channel object (channel → videos → captions + comments per video), and `VideoMinerPublisherService` sends that object to the VideoMiner backend.

---

## Project Structure

```
VimeoMiner/
├── src/main/java/aiss/vimeominer/
│   ├── VimeoMinerApplication.java          # Entry point; defines the RestTemplate bean
│   ├── controller/
│   │   └── ChannelController.java          # GET and POST endpoints
│   ├── service/
│   │   ├── ChannelService.java             # Fetches channel metadata from Vimeo
│   │   ├── VideoService.java               # Fetches videos for a channel
│   │   ├── CaptionService.java             # Fetches text-tracks (captions) for a video
│   │   ├── CommentService.java             # Fetches comments for a video
│   │   ├── ChannelAssemblerService.java    # Orchestrates assembly of the full channel
│   │   └── VideoMinerPublisherService.java # POSTs the assembled channel to VideoMiner
│   ├── model/
│   │   ├── VimeoMiner/                     # Raw Vimeo API response models
│   │   │   ├── channel/VimeoChannel.java
│   │   │   ├── video/VimeoVideo.java, VimeoVideoSearch.java
│   │   │   ├── comment/VimeoComment.java, VimeoCommentSearch.java, VimeoUser.java, Picture.java
│   │   │   └── caption/VimeoCaption.java, VimeoCaptionSearch.java
│   │   └── VideoMiner/                     # VideoMiner normalised output models
│   │       ├── Channel.java
│   │       ├── Video.java
│   │       ├── Caption.java
│   │       ├── Comment.java
│   │       └── User.java
│   └── exception/
│       ├── ChannelNotFoundException.java   # 404
│       ├── VideosNotFoundException.java    # 404
│       ├── CaptionsNotFoundException.java  # 404
│       ├── CommentsNotFoundException.java  # 404
│       ├── ForbiddenException.java         # 403
│       └── ResponseException.java         # 429 (Too Many Requests)
└── src/test/java/aiss/vimeominer/
    ├── controller/
    │   └── ChannelControllerTest.java      # @WebMvcTest — 7 scenarios
    └── service/
        ├── ChannelServiceTest.java
        ├── VideoServiceTest.java
        ├── CaptionServiceTest.java
        ├── CommentServiceTest.java
        ├── ChannelAssemblerServiceTest.java
        └── VideoMinerPublisherServiceTest.java
```

---

## Data Model

### Vimeo API models (`model/VimeoMiner/`)

These classes mirror Vimeo API JSON responses. Jackson annotations handle field name mapping (e.g. `uri` → `id`, `created_time` → `createdTime`).

| Class | Vimeo endpoint |
|---|---|
| `VimeoChannel` | `GET /channels/{id}` |
| `VimeoVideoSearch` | `GET /channels/{id}/videos` |
| `VimeoCommentSearch` | `GET /videos/{id}/comments` |
| `VimeoCaptionSearch` | `GET /videos/{id}/texttracks` |

### VideoMiner output models (`model/VideoMiner/`)

These are the normalised objects sent to the VideoMiner API and returned to the caller.

```
Channel
  ├── id          (channel URI stripped of "/channels/")
  ├── name
  ├── description
  ├── createdTime
  └── videos: List<Video>
        ├── id          (video URI stripped of "/videos/")
        ├── name
        ├── description
        ├── releaseTime
        ├── captions: List<Caption>
        │     ├── id
        │     ├── name
        │     └── language
        └── comments: List<Comment>
              ├── id
              ├── text
              ├── createdOn
              └── author: User
                    ├── name
                    ├── user_link
                    └── picture_link
```

---

## API Endpoints

Base URL: `http://localhost:8081/vimeoMiner/v1`

### `GET /{id}`

Fetches and returns a fully assembled `Channel` object from Vimeo **without** sending it to VideoMiner.

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `id` | path | yes | — | Vimeo channel ID |
| `maxVideos` | query | no | `10` | Max number of videos to retrieve |
| `maxComments` | query | no | `10` | Max comments to retrieve per video |

**Responses:**

| Code | Description |
|---|---|
| `200 OK` | Channel JSON returned successfully |
| `404 Not Found` | Channel, videos, captions, or comments not found |
| `429 Too Many Requests` | Vimeo rate limit exceeded |

---

### `POST /{id}`

Fetches a fully assembled `Channel` from Vimeo and **publishes it to the VideoMiner API**.

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `id` | path | yes | — | Vimeo channel ID |
| `maxVideos` | query | no | `10` | Max number of videos to retrieve |
| `maxComments` | query | no | `10` | Max comments to retrieve per video |
| `Authorization` | header | no | — | Bearer token forwarded to VideoMiner |

**Responses:**

| Code | Description |
|---|---|
| `201 Created` | Channel published and returned |
| `403 Forbidden` | VideoMiner rejected the request (invalid/missing token) |
| `404 Not Found` | Channel, videos, captions, or comments not found |
| `429 Too Many Requests` | Vimeo rate limit exceeded |

---

## Services

### `ChannelService`
Calls `GET https://api.vimeo.com/channels/{id}`, maps the `VimeoChannel` response to a `Channel`, and throws `ChannelNotFoundException` on 404.

### `VideoService`
Calls `GET /channels/{id}/videos` (with optional `?per_page=N`), maps each `VimeoVideo` to `Video`, and throws `VideosNotFoundException` on 404.

### `CaptionService`
Calls `GET /videos/{id}/texttracks`, maps each `VimeoCaption` to `Caption`, and throws `CaptionsNotFoundException` on 404.

### `CommentService`
Calls `GET /videos/{id}/comments` (with optional `?per_page=N`), maps each `VimeoComment` to `Comment`, and throws `CommentsNotFoundException` on 404.

### `ChannelAssemblerService`
Orchestrates the full data assembly:
1. Fetch channel via `ChannelService`
2. Fetch videos via `VideoService`
3. For each video, fetch captions and comments in parallel calls to `CaptionService` and `CommentService`
4. Attach videos (with their captions and comments) to the channel

### `VideoMinerPublisherService`
Sends the assembled `Channel` via `POST` to the VideoMiner API (`${videoMiner.url}/channels`). Forwards the `Authorization` header if provided. Throws `ForbiddenException` on a 403 response from VideoMiner.

All Vimeo services authenticate via a `Bearer` token injected from `${vimeo.token}` and use the shared `RestTemplate` bean defined in `VimeoMinerApplication`.

---

## Exception Handling

| Exception | HTTP Status | Trigger |
|---|---|---|
| `ChannelNotFoundException` | `404 Not Found` | Vimeo channel ID does not exist |
| `VideosNotFoundException` | `404 Not Found` | No videos found for that channel |
| `CaptionsNotFoundException` | `404 Not Found` | No captions found for that video |
| `CommentsNotFoundException` | `404 Not Found` | No comments found for that video |
| `ForbiddenException` | `403 Forbidden` | VideoMiner API rejected the request |
| `ResponseException` | `429 Too Many Requests` | Vimeo rate limit hit |

---

## Configuration

### `application.properties`

```properties
spring.application.name=VimeoMiner
server.port=8081
vimeo.token=${VIMEO_TOKEN:}
vimeo.uri=https://api.vimeo.com
videoMiner.url=http://localhost:8080/videoMiner/v1
server.error.include-message=always
```

| Property | Description |
|---|---|
| `server.port` | VimeoMiner listens on port **8081** |
| `vimeo.token` | Vimeo API access token. Read from the `VIMEO_TOKEN` environment variable |
| `vimeo.uri` | Vimeo REST API base URL |
| `videoMiner.url` | Base URL of the VideoMiner API (the downstream consumer) |

### Local development

To avoid committing your token, create `src/main/resources/application-local.properties` (already git-ignored):

```properties
vimeo.token=YOUR_VIMEO_PERSONAL_ACCESS_TOKEN
```

Then run the application with the `local` profile active:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Obtaining a Vimeo token

1. Log in to [https://developer.vimeo.com](https://developer.vimeo.com)
2. Go to **My Apps** → create or select an app
3. Under **Authentication**, generate a **Personal Access Token** with at least the `public` scope

---

## Running the Application

**Prerequisites:** Java 17, Maven 3.x, and a valid Vimeo API token.

```bash
# From the VimeoMiner directory
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or from the root of the monorepo
mvn spring-boot:run -pl VimeoMiner -Dspring-boot.run.profiles=local
```

The service starts at `http://localhost:8081`.

### Example calls

```bash
# Retrieve channel 28359 (preview, not sent to VideoMiner)
curl http://localhost:8081/vimeoMiner/v1/28359?maxVideos=5&maxComments=3

# Fetch and publish channel 28359 to VideoMiner (with VideoMiner auth token)
curl -X POST "http://localhost:8081/vimeoMiner/v1/28359?maxVideos=5&maxComments=3" \
     -H "Authorization: Bearer <videoMiner-token>"
```

---

## Running the Tests

All 27 tests are **fully mocked** — no real HTTP calls to Vimeo or VideoMiner are made. They use Mockito to stub `RestTemplate` and Spring's `@WebMvcTest` slice for controller tests.

```bash
# From the monorepo root
mvn test -pl VimeoMiner

# From within the VimeoMiner directory
mvn test
```

Expected output:
```
Tests run: 7  — ChannelControllerTest
Tests run: 4  — ChannelAssemblerServiceTest
Tests run: 2  — ChannelServiceTest
Tests run: 4  — VideoServiceTest
Tests run: 4  — CommentServiceTest
Tests run: 2  — CaptionServiceTest
Tests run: 3  — VideoMinerPublisherServiceTest
Tests run: 1  — VimeoMinerApplicationTests
-----------------------------------------------
Total: 27 tests, 0 failures, 0 errors
```

---

## API Documentation (Swagger UI)

Once the application is running, the interactive API documentation is available at:

```
http://localhost:8081/swagger-ui/index.html
```

The OpenAPI JSON spec is at:

```
http://localhost:8081/v3/api-docs
```
