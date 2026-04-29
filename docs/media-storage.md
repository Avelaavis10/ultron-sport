# Media Storage

## Purpose

The MVP media layer gives Ultron Sport an object-storage-ready upload boundary without introducing production storage infrastructure too early.

## Current Scope

- ATHLETE users upload media at `POST /api/media/upload`.
- Owner ATHLETE users and ADMIN users view metadata at `GET /api/media/{mediaId}`.
- ATHLETE users attach owned media to owned editable evidence at `POST /api/evidence/{evidenceId}/media/{mediaId}`.
- Evidence remains compatible with URL-only mode through `fileUrl` or `externalVideoLink`.
- Uploaded media stores metadata in `MediaAsset`, including owner, athlete profile, content type, file size, SHA-256 checksum, public URL, upload status, and scan status.

## Configuration

```yaml
storage:
  mode: LOCAL
  public-base-url: http://localhost:8080/media
  max-file-size-bytes: 52428800
  local:
    base-path: ./uploads/ultron-sport

spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

Supported MVP modes:

- `LOCAL`: writes bytes under the configured local folder.
- `MOCK`: stores metadata and returns a mock-style public URL without writing bytes.
- `S3_TODO` and `AZURE_BLOB_TODO`: reserved enum values for future adapters.

Supported MVP content types:

- `video/mp4`
- `video/quicktime`
- `image/jpeg`
- `image/png`

## Security Rules

- Only ATHLETE users can upload media.
- An athlete can upload media only for their own athlete profile.
- An athlete can attach media only to their own evidence.
- Evidence must be DRAFT or REJECTED before media can be attached or replaced.
- ADMIN users can view media metadata.
- API responses expose `mediaId` and `publicUrl`, not local filesystem paths or storage keys.

## MVP Boundaries

The MVP does not implement S3, Azure Blob, CDN delivery, malware scanning, transcoding, thumbnails, chunked upload, signed download URLs, background workers, or AI video analysis.

Future work can add these behind `MediaStorageService` without rewriting the evidence workflow.
