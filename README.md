# Privacy-Aware Rescue Assistance PoC

Minimal proof-of-concept backend for the bachelor thesis:

**Concept and Prototype Implementation of a Privacy-Aware AI Assistance System for Emergency Medical Teams During Dispatches**

## Purpose

The prototype demonstrates a small, deterministic version of the proposed assistance pipeline:

1. receive simplified dispatch context;
2. validate the request;
3. retrieve relevant fictional knowledge;
4. consider the fictional team qualification;
5. return structured, prioritised and traceable recommendations.

The prototype deliberately does **not** integrate a real language model. Its purpose is to demonstrate the surrounding architecture and processing flow with a minimal and explainable implementation.

## Important limitations

This software:

- is not intended for real medical use;
- contains fictional and simplified guidance only;
- does not use real patient data;
- does not use real Red Cross dispatch data;
- does not use official internal Red Cross or RDmed content;
- is not medically validated;
- does not implement production authentication, HTTPS, persistent storage, audit logging or ESAPP integration.

## Requirements

- Java 21
- Maven 3.9+ for local execution
- Docker for container execution

## Run locally

Run tests:

```bash
mvn clean test
```

Start the backend:

```bash
mvn spring-boot:run
```

The service listens on:

```text
http://localhost:8080
```

## Health check

Request:

```text
GET /health
```

Example:

```powershell
Invoke-RestMethod http://localhost:8080/health |
    ConvertTo-Json
```

Response:

```json
{
  "status": "UP"
}
```

Spring Boot Actuator is also available at:

```text
GET /actuator/health
```

## Assistance endpoint

Preferred minimal-PoC endpoint:

```text
POST /assist
```

The previous development endpoint remains available as an alias:

```text
POST /api/v1/assistance
```

### Example request

```json
{
  "dispatchType": "chest pain",
  "urgency": "high",
  "teamQualification": ["RS", "NFS"],
  "symptoms": [
    "chest pressure",
    "shortness of breath"
  ],
  "notes": "patient is pale and sweating"
}
```

PowerShell:

```powershell
$body = @{
    dispatchType = "chest pain"
    urgency = "high"
    teamQualification = @("RS", "NFS")
    symptoms = @("chest pressure", "shortness of breath")
    notes = "patient is pale and sweating"
} | ConvertTo-Json

$response = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/assist" `
    -ContentType "application/json" `
    -Body $body

$response | ConvertTo-Json -Depth 10
```

### Example output shape

```json
{
  "recommendations": [
    {
      "priority": "high",
      "text": "Fictional PoC guidance: ...",
      "source": "KB-ESC-001",
      "requiredQualification": null,
      "requiresEscalation": false
    },
    {
      "priority": "normal",
      "text": "Fictional PoC guidance: ...",
      "source": "KB-ABCDE-001",
      "requiredQualification": null,
      "requiresEscalation": false
    }
  ]
}
```

The exact list depends on the deterministic retrieval score.

## Fictional qualification model

The proof of concept uses only:

```text
RS  <  NFS
```

For the prototype, `NFS` satisfies an `RS` requirement, while `RS` does not satisfy an `NFS` requirement.

This simplified model is not an authoritative description of Austrian EMS scope of practice.

## Example qualification-boundary scenario

```json
{
  "dispatchType": "qualification boundary",
  "urgency": "high",
  "teamQualification": ["RS"],
  "symptoms": ["advanced intervention"],
  "notes": "fictional scenario"
}
```

The qualification-bound knowledge should be returned as an escalation instead of exposing its restricted fictional content.

Expected fields include:

```json
{
  "source": "KB-QUAL-001",
  "requiredQualification": "NFS",
  "requiresEscalation": true
}
```

## Unknown-information scenario

An unrelated request for which no knowledge entry matches returns a conservative fallback instead of inventing a recommendation.

Example:

```json
{
  "dispatchType": "equipment issue",
  "urgency": "low",
  "teamQualification": ["RS"],
  "symptoms": ["broken tablet screen"],
  "notes": "charging cable unavailable"
}
```

The result uses:

```text
source = prototype-system
requiresEscalation = true
```

## Docker

Build from a clean project folder:

```bash
docker build -t rescue-ai-poc .
```

Run:

```bash
docker run --rm -p 8080:8080 rescue-ai-poc
```

Then test:

```powershell
Invoke-RestMethod http://localhost:8080/health |
    ConvertTo-Json
```

And test `/assist` with the example request above.

Stop the container and start it once more to confirm that the prototype has no hidden local runtime dependency.

## Prototype scope

Implemented:

- simplified dispatch input;
- request validation;
- fictional knowledge base;
- deterministic keyword/rule retrieval;
- fictional qualification checks;
- structured recommendations;
- priorities;
- traceable source IDs;
- escalation flags;
- safe no-match behaviour;
- containerised backend.

Intentionally not implemented:

- real LLM integration;
- real medical knowledge;
- real operational data;
- authentication;
- HTTPS;
- database;
- full audit logging;
- real offline functionality;
- ESAPP integration;
- Kubernetes.
