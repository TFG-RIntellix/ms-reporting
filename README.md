# ms-reporting

Microservice that generates AI-powered risk reports for credit requests.

It consumes the `persistScoring` Kafka message produced by **ms-risk-engine**
(the same message **ms-core-data** consumes, but with its own consumer group),
asks an LLM acting as a financial-risk analyst (Google **Gemini**) to produce a
detailed risk report, renders it as a **PDF**, and persists the report document
through **ms-core-data** (`POST /api/reports`).

## Architecture

Hexagonal (ports & adapters), mirroring `ms-core-data` / `ms-risk-engine`:

```
domain/          # entities, enums, output ports, exceptions (framework-agnostic)
application/     # use cases (ReportGenerationService), input port, input DTOs
infraestructure/ # adapters (Kafka, Gemini, PDF, ms-core-data), config, mappers
utils/           # log message constants
```

### Flow

1. `ScoringKafkaConsumer` receives a `persistScoring` message (key = `requestId`).
2. `MsCoreDataScoringAdapter` fetches the scoring from
   `GET /api/requests/{requestId}/scoring`. A `404` means the scoring is not yet
   persisted (race with ms-core-data) → `ScoringNotAvailableException` → the
   Kafka message is retried with back-off. It also resolves `partyId`.
3. `GeminiReportAdapter` calls Gemini with an analyst system prompt and a forced
   JSON response schema, producing the report content (Spanish).
4. `PdfReportAdapter` renders the report to a PDF (OpenPDF).
5. `MsCoreDataReportAdapter` persists the report via `POST /api/reports`.

## Tech stack

- Java 17, Spring Boot 4.0.3, Maven
- Spring Kafka (consumer, manual ack, retry back-off)
- Spring `RestClient` for ms-core-data and Gemini
- OpenPDF 2.0.3 (PDF rendering — latest release compiled for Java 17)

## Configuration

Configuration lives in `src/main/resources/application.yaml` (git-ignored, holds
secrets). Supported properties / environment variables:

| Property | Env var | Default |
| --- | --- | --- |
| `spring.kafka.bootstrap-servers` | `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `scoring.kafka.topic.persist` | `SCORING_KAFKA_TOPIC_PERSIST` | `persistScoring` |
| `scoring.kafka.consumer.group-id` | `SCORING_KAFKA_CONSUMER_GROUP_ID` | `ms-reporting-group` |
| `scoring.kafka.consumer.retry.max-attempts` | `SCORING_KAFKA_CONSUMER_RETRY_MAX_ATTEMPTS` | `10` |
| `scoring.kafka.consumer.retry.initial-delay` | `SCORING_KAFKA_CONSUMER_RETRY_INITIAL_DELAY` | `3000` |
| `ms-core-data.base-url` | `MS_CORE_DATA_BASE_URL` | `http://localhost:8081` |
| `gemini.base-url` | `GEMINI_BASE_URL` | `https://generativelanguage.googleapis.com` |
| `gemini.api-key` | `GEMINI_API_KEY` | _(required)_ |
| `gemini.model` | `GEMINI_MODEL` | `gemini-2.5-flash` |
| `report.pdf.output-dir` | `REPORT_PDF_OUTPUT_DIR` | `reports` |

> **Note:** the report's `party_id` is resolved from ms-core-data. The current
> ms-core-data endpoints do not expose `party_id` yet, so a small change on the
> ms-core-data side is required for it to be populated end-to-end.

## Build & test

```bash
mvn clean test     # unit tests
mvn spring-boot:run # run (requires a configured application.yaml)
```
