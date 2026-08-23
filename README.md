# BA2 AI Assistance Prototype — Batch 1

This is the first implementation milestone for the proof-of-concept described in the thesis.

## Scope of this batch

This batch implements only:

1. Spring Boot backend skeleton
2. `POST /api/v1/assistance`
3. validation of the simplified dispatch request
4. structured recommendation response
5. health endpoint through Spring Boot Actuator
6. a Dockerfile for the backend

Retrieval and LLM integration are intentionally not included yet.

## Requirements

- Java 21
- Maven 3.9+
- Docker (optional for the first run)

## Run locally

```bash
mvn clean test
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

Health check:

```text
GET http://localhost:8080/actuator/health
```

## Test the assistance endpoint

```bash
curl -X POST http://localhost:8080/api/v1/assistance \
  -H "Content-Type: application/json" \
  -d '{
    "dispatchType": "chest pain",
    "urgency": "high",
    "teamQualification": ["RS", "NFS"],
    "symptoms": ["chest pressure", "shortness of breath"],
    "notes": "patient is pale and sweating"
  }'
```

Expected response shape:

```json
{
  "recommendations": [
    {
      "priority": "normal",
      "text": "Prototype request accepted. Retrieval and AI generation will be added in the next implementation steps.",
      "source": "prototype-backend",
      "requiredQualification": null,
      "requiresEscalation": false
    }
  ]
}
```

## Build the Docker image

First build the jar:

```bash
mvn clean package
```

Then:

```bash
docker build -t ba2-assistant:batch-1 .
docker run --rm -p 8080:8080 ba2-assistant:batch-1
```
