# BA2 Prototype — Batch 2

Batch 2 replaces the placeholder response with deterministic retrieval from a small fictional local knowledge base.

## What this batch adds

- local JSON knowledge base
- stable source IDs
- fictional/non-official knowledge entries
- deterministic tag/keyword retrieval
- ranked top-N matches
- safe fallback when nothing relevant is found
- unit tests for retrieval
- service test for source-grounded output

No LLM is used yet.

## Apply this batch

Copy the files in this archive into the existing Batch 1 project, preserving the directory structure.

`AssistanceService.java` replaces the Batch 1 version.

No `pom.xml` changes are required.

Then run:

```bash
mvn clean test
mvn spring-boot:run
```

The knowledge base is deliberately fictional and must not be used for real medical decision-making.
