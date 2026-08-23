# BA2 Prototype — Batch 4 (Minimal Completion)

This batch deliberately stays within the minimal proof-of-concept scope.

It does NOT add:
- a real LLM
- Ollama
- a database
- authentication
- HTTPS
- ESAPP integration
- Kubernetes
- a separate frontend framework

## What this batch completes

- adds `GET /health`
- adds the checklist-compatible `POST /assist` endpoint
- keeps the existing `/api/v1/assistance` endpoint as an alias
- makes urgent/chest-pain/breathing/circulation requests explicitly favour the fictional ABCDE entry
- makes high-urgency requests explicitly favour the fictional escalation rule
- appends the fictional documentation reminder when relevant knowledge was found
- keeps the retrieval logic deterministic and explainable
- adds tests for the presentation-relevant retrieval behaviour
- replaces the Dockerfile with a clean multi-stage build
- replaces the README with a final minimal-PoC README

## Apply

Copy the files over the existing Batch 3 project while preserving the paths.

Then run:

```bash
mvn clean test
mvn spring-boot:run
```

Test:

```text
GET  http://localhost:8080/health
POST http://localhost:8080/assist
```

The old endpoint remains valid:

```text
POST http://localhost:8080/api/v1/assistance
```

## Docker

The new Dockerfile builds the Maven project inside Docker, so no pre-built JAR is required.

```bash
docker build -t rescue-ai-poc .
docker run --rm -p 8080:8080 rescue-ai-poc
```

Then test:

```text
http://localhost:8080/health
```
