# Developer connection

## Getting Started

### Prerequisites
- Java 11 (because of caffeine classes in version >=3.0 were built on Java 11)
- Sbt

### How to run
1. Replace Twitter api token "my_bearer_token" with some valid one in attached .env file
2. Navigate to project dir where the build.sbt is present
3. source .env
4. sbt run

or:
4. sbt
5. reStart

### How to test
How to run tests:
1. sbt test

### Example request:
```commandline
curl --location --request GET 'localhost:8080/developers/connected/jdegoes/musk'
```

## Technology stack
- Http4s
- Cats-effects
- Cats
- Circe
- Derevo
- Newtype
- Scaffeine
- Weaver

## TODO:
- Add tracking id to requests to allow following request execution on central log store (e.g. Datadog or other) in case of multiple requests/api nodes.
- Add health check, which can include checking integrated APIs.
- Retrying of requests could be added as well in case of errors.
- Measure test code coverage with Scoverage and increase it.
- Add Swagger.
- If needed, integration tests with respective twitter/github test environments could be added.
- Instead of having cache in local memory (Caffeine) it could be external service e.g. Redis cluster.
- Docker image can be created for running the service.
- Circuit breakers could be added to avoid surpassing quota.
- Some logs could be silenced or their levels could be adjusted.
- Possibly use dedicated Twitter/Github libraries instead of directly using respective REST apis.
