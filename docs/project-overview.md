# Project Overview

## Purpose

This project is a Spring Boot demo application for Camunda 8 / Zeebe. It shows how to:

- start BPMN process instances through REST;
- correlate Zeebe messages into waiting receive tasks;
- run Zeebe job workers from Spring;
- call an external stub service from workers;
- shorten active job timeouts to force retry behavior during demos;
- run the local Camunda stack with Docker Compose.

## Runtime Components

- Spring Boot application: REST API, Zeebe client, job workers, OpenAPI UI.
- Zeebe: process engine and gateway.
- Elasticsearch: Zeebe exporter storage used by `ElasticJobService` to find active jobs.
- Operate: Camunda UI on host port `8081`.
- Tasklist: Camunda UI on host port `8082`.
- WireMock stub service: external service simulation on host port `8089`.

The Docker Compose file starts the Camunda infrastructure by default. The application and stub service are behind the `app` profile.

## Main Configuration

- Application port: `8080`.
- Zeebe gateway: `localhost:26500` for local app runs, `zeebe:26500` in Docker.
- Default process id: `demo-process`.
- OpenAPI JSON: `/api-docs`.
- Swagger UI: `/swagger-ui.html`.
- Stub service base URL: `http://localhost:8089` locally, `http://csharp-service:8080` in Docker.
- Job index pattern: configured through `app.camunda.job-index`.

## Process Definitions

### `demo-process`

Resource: `src/main/resources/bpmn/demo-process.bpmn`

Starts the main demo flow and calls `nested-approval-process` through a call activity. The default REST start endpoint uses this process when `processId` is omitted.

### `nested-approval-process`

Resource: `src/main/resources/bpmn/nested-approval-process.bpmn`

Flow responsibilities:

- waits for `external-data-received` correlated by `requestId`;
- evaluates `nested-approval-decision`;
- runs `demo-task`;
- calls the synchronous stub service through `stub-service-task`.

### `async-stub-callback-process`

Resource: `src/main/resources/bpmn/async-stub-callback-process.bpmn`

Flow responsibilities:

- submits an async request through `async-stub-submission-task`;
- waits for `async-stub-callback-received` correlated by `correlationKey`;
- continues when `/api/async-stub-callbacks/completed` publishes the callback message.

## Decision Definition

### `nested-approval-decision`

Resource: `src/main/resources/dmn/nested-approval-decision.dmn`

Returns a boolean approval result. The checked rules cover amounts up to and above the configured approval limit in the DMN table.

## REST Entry Points

Detailed controller method documentation lives in [controller-methods.md](controller-methods.md).

Important endpoints:

- `POST /api/process-instances`
- `POST /api/process/start`
- `POST /api/messages/external-data`
- `POST /api/async-stub-callbacks/completed`
- `POST /api/jobs/{jobKey}/timeout/1s`
- `POST /api/jobs/{jobKey}/retry`
- `POST /api/jobs/shorten-active-demo-task-timeouts`

## Workers

### `DemoTaskWorker`

Job type: `demo-task`

Returns demo completion variables:

- `workerStatus`
- `processedAt`
- `approved`

### `StubServiceTaskWorker`

Job type: `stub-service-task`

Builds a `StubServiceRequest` from process variables, posts it to the configured synchronous stub endpoint, and stores response variables under the `stubService*` prefix.

### `AsyncStubSubmissionWorker`

Job type: `async-stub-submission-task`

Creates `correlationKey`, submits async request data to the configured stub endpoint with that key as `requestId`, and stores response variables under the `asyncStub*` prefix.

## Stub Service

The `stub-service` directory contains a WireMock image definition and mappings:

- `POST /api/stub/external-service`
- `POST /api/stub/external-service/async`

Both endpoints return a templated JSON response with the incoming `requestId`, status, message, tracking id, and processed timestamp.

## Resume Checklist

1. Start infrastructure:

   ```powershell
   docker compose up -d
   ```

2. Start infrastructure plus app and stub service:

   ```powershell
   docker compose --profile app up -d --build
   ```

3. Use HTTP examples from `src/main/resources/http`:

   - start `demo-process`: `start-process-instance.http`;
   - continue nested approval: `continue-process-instance.http`;
   - start async callback process: `start-async-stub-callback-process.http`;
   - complete async callback: `complete-async-stub-callback-process.http`;
   - retry a stuck job: `retry-stuck-job.http`.

4. Inspect processes in Operate:

   ```text
   http://localhost:8081
   ```

5. Inspect API contract:

   ```text
   http://localhost:8080/swagger-ui.html
   ```

## Current Notes

- The local workspace does not contain a Maven Wrapper.
- `mvn` is also not available in the current shell environment, so tests cannot be run from this workspace until Maven is installed or a wrapper is added.
- Existing tests cover controllers, services, workers, and BPMN/DMN resource structure.
