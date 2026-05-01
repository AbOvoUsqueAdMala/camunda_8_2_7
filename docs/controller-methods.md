# Controller Methods

## ProcessInstanceController

- [`startNewInstance`](../src/main/java/ru/abovousqueadmala/controller/ProcessInstanceController.java#L25)
  - Endpoints: `POST /api/process-instances`, `POST /api/process/start`
  - Purpose: starts a new process instance in Zeebe.
  - Request:
    - `processId` is optional.
    - all other top-level JSON fields are passed to the process as variables.
    - nested `"variables"` is not supported.
  - Response: `StartProcessResponse`
  - Example: [`start-process-instance.http`](../src/main/resources/http/start-process-instance.http#L1)

## MessageController

- [`continueProcess`](../src/main/java/ru/abovousqueadmala/controller/MessageController.java#L27)
  - Endpoint: `POST /api/messages/external-data`
  - Purpose: correlates external data into a waiting process instance.
  - Request:
    - `requestId` is used as the correlation key.
    - `variables` contains the payload that will be published to Zeebe.
  - Response: `ContinueProcessResponse`
  - Example: [`continue-process-instance.http`](../src/main/resources/http/continue-process-instance.http#L1)

## AsyncStubCallbackController

- [`completeAsyncRequest`](../src/main/java/ru/abovousqueadmala/controller/AsyncStubCallbackController.java#L27)
  - Endpoint: `POST /api/async-stub-callbacks/completed`
  - Purpose: accepts callback data from the external async stub flow and resumes the waiting process.
  - Request:
    - `requestId` is required.
    - `status`, `message`, `trackingId` and `variables` are forwarded to message correlation.
  - Response: `ContinueProcessResponse`
  - Example: [`complete-async-stub-callback-process.http`](../src/main/resources/http/complete-async-stub-callback-process.http#L1)

## JobController

- [`setTimeoutToOneSecond`](../src/main/java/ru/abovousqueadmala/controller/JobController.java#L25)
  - Endpoint: `POST /api/jobs/{jobKey}/timeout/1s`
  - Purpose: shortens timeout for a specific active job.
  - Response: `JobTimeoutUpdateResponse`

- [`triggerRetry`](../src/main/java/ru/abovousqueadmala/controller/JobController.java#L31)
  - Endpoint: `POST /api/jobs/{jobKey}/retry`
  - Purpose: forces retry for a stuck active job by shortening its timeout.
  - Response: `JobTimeoutUpdateResponse`
  - Example: [`retry-stuck-job.http`](../src/main/resources/http/retry-stuck-job.http#L1)

- [`shortenActiveTimeouts`](../src/main/java/ru/abovousqueadmala/controller/JobController.java#L37)
  - Endpoint: `POST /api/jobs/shorten-active-demo-task-timeouts`
  - Purpose: shortens timeout for all active `demo-task` jobs found through Elasticsearch.
  - Response: `BulkTimeoutUpdateResponse`
