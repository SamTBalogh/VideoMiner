# VideoMiner

VideoMiner is the central microservice of the system. It acts as the storage and orchestration layer: it receives video data from the adapter services (VimeoMiner, YoutubeMiner) and exposes it through a unified REST API with pagination, filtering, and token-based authentication.

## Table of Contents

- [Data Model](#data-model)
- [Authentication](#authentication)
- [Endpoints](#endpoints)
  - [Token](#token)
  - [Channels](#channels)
  - [Videos](#videos)
  - [Comments](#comments)
  - [Captions](#captions)
  - [Users](#users)
- [Error Reference](#error-reference)
- [Pagination, Filtering and Sorting](#pagination-filtering-and-sorting)
- [Configuration](#configuration)
- [Running the Service](#running-the-service)
- [Swagger UI / H2 Console](#swagger-ui--h2-console)

---

## Data Model

```
Channel
 └── videos: List<Video>
       ├── comments: List<Comment>
       │     └── author: User
       └── captions: List<Caption>
```

| Entity    | PK           | Key fields                                             |
|-----------|--------------|--------------------------------------------------------|
| `Channel` | `String id`  | `name`*, `description`, `createdTime`*                 |
| `Video`   | `String id`  | `name`*, `description`, `releaseTime`*                 |
| `Comment` | `String id`  | `text`, `createdOn`, `author` (User, NOT NULL)         |
| `Caption` | `String id`  | `name`, `language`                                     |
| `User`    | `Long id`    | `name`, `userLink`, `pictureLink` — auto-generated ID  |
| `Token`   | `String id`  | Primary key only; no additional fields                 |

> `*` required field (`@NotEmpty`)

Cascade (`ALL`) relationships mean that deleting a `Channel` deletes its `Video` records, deleting a `Video` deletes its `Comment` and `Caption` records, and deleting a `Comment` deletes its associated `User`.

---

## Authentication

All endpoints **except** `POST /videoMiner/v1/token` require the following header:

```
Authorization: <token>
```

**Flow:**

1. Register a token (no authentication required):
   ```http
   POST /videoMiner/v1/token
   Content-Type: application/json

   { "id": "my-secret-token" }
   ```
2. Include that token in all subsequent requests:
   ```http
   GET /videoMiner/v1/channels
   Authorization: my-secret-token
   ```

| Situation                          | HTTP  | Exception                  |
|------------------------------------|-------|----------------------------|
| `Authorization` header missing     | 403   | `TokenRequiredException`   |
| Token not registered in the DB     | 403   | `TokenNotValidException`   |

---

## Endpoints

Base path: `/videoMiner/v1`

### Token

| Method | Path     | Body              | Response | Status |
|--------|----------|-------------------|----------|--------|
| POST   | `/token` | `{ "id": "..." }` | `Token`  | 201    |

### Channels

| Method | Path               | Description                                        | Status |
|--------|--------------------|----------------------------------------------------|--------|
| GET    | `/channels`        | Paginated list of channels (optional filters)      | 200    |
| GET    | `/channels/{id}`   | Channel by ID                                      | 200    |
| POST   | `/channels`        | Create a channel (ID required in body)             | 201    |
| PUT    | `/channels/{id}`   | Update `name` and/or `description`                 | 204    |
| DELETE | `/channels/{id}`   | Delete channel (cascades to videos, etc.)          | 204    |

**Filter parameters for GET `/channels`:** `id`, `name`, `description`, `createdTime`

### Videos

| Method | Path                            | Description                                       | Status |
|--------|---------------------------------|---------------------------------------------------|--------|
| GET    | `/videos`                       | Paginated list of videos                          | 200    |
| GET    | `/videos/{id}`                  | Video by ID                                       | 200    |
| GET    | `/channels/{channelId}/videos`  | Videos belonging to a channel                    | 200    |
| POST   | `/channels/{channelId}/videos`  | Add a video to a channel                          | 201    |
| PUT    | `/videos/{id}`                  | Update `name` and/or `description`                | 204    |
| DELETE | `/videos/{id}`                  | Delete video (cascades to comments and captions)  | 204    |

**Filter parameters for GET `/videos`:** `id`, `name`, `description`, `releaseTime`

### Comments

| Method | Path                             | Description                           | Status |
|--------|----------------------------------|---------------------------------------|--------|
| GET    | `/comments`                      | Paginated list of comments            | 200    |
| GET    | `/comments/{id}`                 | Comment by ID                         | 200    |
| GET    | `/videos/{videoId}/comments`     | Comments belonging to a video         | 200    |
| POST   | `/videos/{videoId}/comments`     | Add a comment to a video              | 201    |
| PUT    | `/comments/{id}`                 | Update `text` and/or `createdOn`      | 204    |
| DELETE | `/comments/{id}`                 | Delete comment (cascades to user)     | 204    |

**Filter parameters for GET `/comments`:** `id`, `text`, `createdOn`

### Captions

| Method | Path                            | Description                       | Status |
|--------|---------------------------------|-----------------------------------|--------|
| GET    | `/captions`                     | Paginated list of captions        | 200    |
| GET    | `/captions/{id}`                | Caption by ID                     | 200    |
| GET    | `/videos/{videoId}/captions`    | Captions belonging to a video     | 200    |
| POST   | `/videos/{videoId}/captions`    | Add a caption to a video          | 201    |
| PUT    | `/captions/{id}`                | Update `name` and/or `language`   | 204    |
| DELETE | `/captions/{id}`                | Delete caption                    | 204    |

**Filter parameters for GET `/captions`:** `id`, `name`, `language`

### Users

| Method | Path                         | Description                                       | Status |
|--------|------------------------------|---------------------------------------------------|--------|
| GET    | `/users`                     | Paginated list of users                           | 200    |
| GET    | `/users/{id}`                | User by ID                                        | 200    |
| GET    | `/videos/{videoId}/users`    | Authors of the comments belonging to a video      | 200    |
| PUT    | `/users/{id}`                | Update `name`, `userLink` and/or `pictureLink`    | 204    |
| DELETE | `/users/{id}`                | Delete user (cascades to associated comment)      | 204    |

**Filter parameters for GET `/users`:** `id` *(must be numeric)*, `name`, `userLink`, `pictureLink`

> Users are not created directly. They are created automatically when a `Comment` is posted with a non-null `author` field.

---

## Error Reference

| Exception                   | HTTP | Cause                                                        |
|-----------------------------|------|--------------------------------------------------------------|
| `VideoNotFoundException`    | 404  | No video found with the given ID                             |
| `ChannelNotFoundException`  | 404  | No channel found with the given ID                           |
| `CommentNotFoundException`  | 404  | No comment found with the given ID                           |
| `UserNotFoundException`     | 404  | No user found with the given ID                              |
| `CaptionNotFoundException`  | 404  | No caption found with the given ID                           |
| `TokenRequiredException`    | 403  | `Authorization` header is missing                            |
| `TokenNotValidException`    | 403  | The provided token is not registered                         |
| `IdCannotBeNull`            | 400  | The `id` field in the request body is required for POST      |
| `BadRequestParameterField`  | 400  | More than one filter parameter was provided at the same time |
| `BadRequestIdParameter`     | 400  | The `id` filter for users must be a valid number             |

---

## Pagination, Filtering and Sorting

All list endpoints (`GET /channels`, `/videos`, `/comments`, `/captions`, `/users`) accept:

| Parameter | Type   | Default | Description                                                         |
|-----------|--------|---------|---------------------------------------------------------------------|
| `page`    | int    | `0`     | Page number (0-based)                                               |
| `size`    | int    | `10`    | Number of items per page                                            |
| `order`   | String | —       | Field to sort by. Prefix with `-` for descending order              |
| *filter*  | String | —       | Field name to filter on. Only one filter parameter at a time        |

Examples:
```
GET /videoMiner/v1/channels?page=1&size=5
GET /videoMiner/v1/videos?name=Tutorial&order=-releaseTime
GET /videoMiner/v1/comments?page=0&size=20&order=createdOn
```

---

## Configuration

| Property               | Value                        |
|------------------------|------------------------------|
| Server port            | `8080` (default)             |
| Database               | H2 in-memory (`testdb`)      |
| H2 console path        | `/h2-ui`                     |
| DDL auto               | `update`                     |
| SQL logging            | enabled                      |

The database is **in-memory**: data does not persist across restarts. To persist data, replace the datasource URL in `application.properties` with an external database (PostgreSQL, MySQL, etc.).

---

## Running the Service

From the `VideoMiner/` directory:

```bash
mvn spring-boot:run
```

Or from the monorepo root:

```bash
mvn spring-boot:run -pl VideoMiner
```

The service starts at [http://localhost:8080](http://localhost:8080).

---

## Swagger UI / H2 Console

| Resource       | URL                                                           |
|----------------|---------------------------------------------------------------|
| Swagger UI     | http://localhost:8080/swagger-ui/index.html                   |
| OpenAPI JSON   | http://localhost:8080/v3/api-docs                             |
| H2 Console     | http://localhost:8080/h2-ui  (JDBC URL: `jdbc:h2:mem:testdb`) |
