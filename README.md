# Privacy-Aware Rescue Assistance PoC

Minimal proof-of-concept backend for the bachelor thesis:

**Concept and Prototype Implementation of a Privacy-Aware AI Assistance System for Emergency Medical Teams During Dispatches**

## Purpose

The prototype demonstrates the core assistance flow described in the thesis:

1. receive a simplified fictional dispatch context;
2. validate the request;
3. retrieve relevant entries from a small fictional knowledge base;
4. consider the fictional qualification level of the team;
5. return structured, prioritised and traceable recommendations.

The implementation is intentionally small and deterministic. It uses explainable keyword/rule-based retrieval and does **not** integrate a real language model.

This prototype demonstrates architecture and processing flow only. It is not intended to demonstrate medical correctness or production readiness.

## Important limitations

This software:

- is **not intended for real medical use**;
- contains only fictional and simplified guidance;
- does not use real patient data;
- does not use real Red Cross dispatch data;
- does not use official internal Red Cross or RDmed content;
- is not medically validated;
- does not implement production authentication or authorisation;
- does not implement HTTPS;
- does not use persistent database storage;
- does not implement complete audit logging;
- does not implement ESAPP integration;
- does not provide real offline assistance;
- does not integrate a real LLM;
- does not use Kubernetes or other production orchestration.

## Technology and requirements

The prototype uses:

- Java 21
- Spring Boot
- Maven
- Docker

The backend listens on TCP port `8080`.

For local execution, install Java 21 and Maven 3.9+.

For Docker execution, only Docker is required; the supplied multi-stage Dockerfile performs the Maven build inside the container build process.

---

## Running locally

From the project root, run the test suite:

```bash
mvn clean test
```

Then start the backend:

```bash
mvn spring-boot:run
```

The service is available at:

```text
http://localhost:8080
```

### Health check

```text
GET /health
```

Example:

```bash
curl http://localhost:8080/health
```

Expected response:

```json
{
  "status": "UP"
}
```

Spring Boot Actuator is also available at:

```text
GET /actuator/health
```

---

## Assistance endpoint

Main endpoint:

```text
POST /assist
```

Compatibility alias:

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

Example with `curl`:

```bash
curl -X POST http://localhost:8080/assist \
  -H "Content-Type: application/json" \
  -d '{
    "dispatchType": "chest pain",
    "urgency": "high",
    "teamQualification": ["RS", "NFS"],
    "symptoms": ["chest pressure", "shortness of breath"],
    "notes": "patient is pale and sweating"
  }'
```

### Response format

The response contains a list of structured recommendations:

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

Possible priorities are:

```text
critical
high
normal
low
```

The exact list depends on the deterministic retrieval rules and the request content.

---

## Prototype behaviour

### Knowledge retrieval

The prototype contains a small fictional knowledge base with entries representing:

- a simplified ABCDE-style assessment reminder;
- chest-pain-related information;
- breathing-related information;
- qualification-dependent information;
- escalation rules;
- documentation reminders.

Retrieval is intentionally simple and explainable.

Examples:

- chest-pain, breathing, circulation and high-urgency requests favour the ABCDE entry;
- high-urgency requests favour the escalation entry;
- qualification-related cases can retrieve qualification-bound knowledge;
- a documentation reminder can be added when relevant knowledge is found.

Recommendations reference stable source IDs such as:

```text
KB-ABCDE-001
KB-ESC-001
KB-QUAL-001
```

### Fictional qualification model

The prototype uses only two simplified qualification levels:

```text
RS < NFS
```

Internally:

```text
RS  -> level 10
NFS -> level 20
```

For this proof of concept:

- `RS` satisfies an `RS` requirement;
- `NFS` satisfies an `RS` requirement;
- `NFS` satisfies an `NFS` requirement;
- `RS` does not satisfy an `NFS` requirement.

This model exists only to demonstrate qualification-aware processing. It is **not an authoritative description of Austrian EMS scope of practice**.

### Qualification-boundary example

Request:

```json
{
  "dispatchType": "qualification boundary",
  "urgency": "high",
  "teamQualification": ["RS"],
  "symptoms": ["advanced intervention"],
  "notes": "fictional qualification-boundary scenario"
}
```

For the qualification-bound entry, the response contains:

```json
{
  "source": "KB-QUAL-001",
  "requiredQualification": "NFS",
  "requiresEscalation": true
}
```

The restricted fictional knowledge text is not returned to an insufficiently qualified team. The backend returns an escalation-oriented explanation instead.

If the same request is sent with:

```json
"teamQualification": ["NFS"]
```

the entry can be returned normally with:

```json
{
  "requiredQualification": "NFS",
  "requiresEscalation": false
}
```

### Unknown-information behaviour

If no knowledge entry matches the request, the prototype does not invent a recommendation.

Example request:

```json
{
  "dispatchType": "equipment issue",
  "urgency": "low",
  "teamQualification": ["RS"],
  "symptoms": ["broken tablet screen"],
  "notes": "charging cable unavailable"
}
```

The fallback response uses:

```text
source = prototype-system
requiresEscalation = true
```

---

## Postman collection

If the submitted project contains the accompanying Postman collection, it can be imported into Postman to execute the prepared prototype requests directly.

Use this base address for a local backend:

```text
http://localhost:8080
```

For a backend running on another machine in the same local/private network:

```text
http://<SERVER-IP>:8080
```

The collection can then be used to verify the health endpoint and the prepared example scenarios.

---

# Running with Docker

## Build

From the project root:

```bash
docker build -t rescue-ai-poc .
```

## Run locally

```bash
docker run --rm \
  --name rescue-ai-poc \
  -p 8080:8080 \
  rescue-ai-poc
```

Test:

```bash
curl http://localhost:8080/health
```

Expected:

```json
{"status":"UP"}
```

---

# Deploying to a Linux server

The Docker container can also run on a Linux machine elsewhere on the same local or private network.

The Linux server requires:

- Docker Engine;
- network connectivity from the client to the server;
- inbound TCP port `8080` allowed by the server firewall.

## Build and run directly on the Linux server

Copy or clone the project onto the server, enter the project directory, and build:

```bash
docker build -t rescue-ai-poc .
```

Run:

```bash
docker run -d \
  --name rescue-ai-poc \
  --restart unless-stopped \
  -p 8080:8080 \
  rescue-ai-poc
```

Verify:

```bash
docker ps
curl http://localhost:8080/health
```

From another computer or phone on the same network:

```text
http://<SERVER-IP>:8080/health
```

The assistance endpoint is:

```text
http://<SERVER-IP>:8080/assist
```

## Bind only to a private interface

If the server has multiple network interfaces and the PoC should be reachable only through one private interface, bind Docker to that interface's IP:

```bash
docker run -d \
  --name rescue-ai-poc \
  --restart unless-stopped \
  -p <PRIVATE-SERVER-IP>:8080:8080 \
  rescue-ai-poc
```

This is useful when the prototype should only be accessible over a LAN, VPN or private overlay network.

---

## Linux firewall

The server must permit inbound TCP connections to port `8080` on the interface used for the demonstration.

Docker daemon ports such as `2375` and `2376` are **not required** and should not be exposed.

### firewalld

On systems such as CentOS Stream, Rocky Linux or Fedora:

```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

Verify:

```bash
sudo firewall-cmd --list-ports
```

For a more restrictive setup, assign the private network interface to a dedicated firewalld zone and open TCP `8080` only in that zone.

### UFW

On systems using UFW:

```bash
sudo ufw allow 8080/tcp
```

For a private deployment, allowing the port only on the desired network interface is preferable.

---

## Docker administration

View the container:

```bash
docker ps
```

View logs:

```bash
docker logs rescue-ai-poc
```

Follow logs:

```bash
docker logs -f rescue-ai-poc
```

Restart:

```bash
docker restart rescue-ai-poc
```

Stop:

```bash
docker stop rescue-ai-poc
```

Start again:

```bash
docker start rescue-ai-poc
```

Remove:

```bash
docker rm -f rescue-ai-poc
```

The prototype has no persistent database or other external runtime state, so restarting the container does not change its behaviour.

---

# Suggested verification

After starting the prototype, the following checks are sufficient to verify the demonstration environment.

## 1. Health

```text
GET /health
```

Expected:

```json
{
  "status": "UP"
}
```

## 2. Chest-pain scenario

Send the example chest-pain request and verify that the response:

- is structured JSON;
- contains prioritised recommendations;
- contains source IDs;
- includes general assessment and escalation-related information;
- does not produce a definitive diagnosis.

## 3. Qualification-boundary scenario

Send the qualification-boundary request with:

```json
"teamQualification": ["RS"]
```

Verify that `KB-QUAL-001` contains:

```text
requiredQualification = NFS
requiresEscalation = true
```

Repeat with:

```json
"teamQualification": ["NFS"]
```

and verify:

```text
requiresEscalation = false
```

## 4. Unknown-information scenario

Send an unrelated request and verify that the backend returns the conservative `prototype-system` fallback instead of inventing knowledge.

## 5. Invalid input

Omit a required field or provide an unsupported qualification. The backend should reject the request with HTTP `400 Bad Request`.

---

## Network availability

The PoC focuses on the normal online client-server flow.

If the backend server is stopped or unreachable, the client receives a connection failure and no alternative local recommendation is generated. Full offline behaviour is outside the prototype scope.

A simple availability test is:

```bash
docker stop rescue-ai-poc
```

Attempt a request from the client, then restore the service:

```bash
docker start rescue-ai-poc
```

---

# Implemented scope

Implemented:

- simplified fictional dispatch input;
- JSON request validation;
- `/health`;
- `/assist`;
- fictional local knowledge base;
- deterministic keyword/rule retrieval;
- fictional qualification checking;
- qualification-aware escalation;
- priorities;
- traceable source IDs;
- structured JSON recommendations;
- conservative no-match fallback;
- automated tests;
- Docker containerisation;
- access over a local/private network.

Intentionally outside the prototype:

- real patient or operational data;
- official medical knowledge;
- real LLM integration;
- production authentication and authorisation;
- HTTPS;
- persistent database storage;
- complete audit logging;
- real offline operation;
- ESAPP integration;
- production monitoring;
- Kubernetes or other distributed deployment infrastructure.

The prototype should therefore be understood as a technical demonstration of the assistance pipeline described in the thesis, not as a deployable emergency medical system.
