# BA2 Prototype — Batch 3

Batch 3 adds qualification-aware recommendation handling.

## What this batch adds

- explicit `Qualification` enum
- simplified fictional qualification hierarchy (`RS` < `NFS`)
- qualification-aware knowledge entries
- `QualificationService`
- safe escalation response when a retrieved entry requires a qualification the team does not have
- tests for qualification matching and qualification-boundary behavior
- validation of qualification values in incoming JSON

## Apply this batch

Copy the files in this archive into the existing project and preserve the directory structure.

Several files replace earlier versions:

- `AssistanceRequest.java`
- `KnowledgeEntry.java`
- `AssistanceService.java`
- `knowledge-base.json`
- `RetrievalServiceTest.java`
- `AssistanceServiceTest.java`

No `pom.xml` changes are required.

Then run:

```bash
mvn clean test
```

and afterwards:

```bash
mvn spring-boot:run
```

## Qualification model

The prototype deliberately uses only two fictional qualification levels:

```text
RS  -> level 10
NFS -> level 20
```

For the purpose of this proof of concept, an `NFS` qualification satisfies an `RS`
requirement, while an `RS` qualification does not satisfy an `NFS` requirement.

This is a simplified prototype rule and is not an authoritative description of
real-world Austrian EMS scope of practice.

## Qualification-boundary test

Request with insufficient fictional qualification:

```json
{
  "dispatchType": "qualification boundary",
  "urgency": "high",
  "teamQualification": ["RS"],
  "symptoms": ["advanced intervention"],
  "notes": "fictional scenario"
}
```

The response should include `KB-QUAL-001`, but the restricted knowledge text must
not be exposed. Instead the response should contain:

```json
{
  "source": "KB-QUAL-001",
  "requiredQualification": "NFS",
  "requiresEscalation": true
}
```

Now send the same request with:

```json
"teamQualification": ["NFS"]
```

The recommendation may expose the fictional knowledge entry and should contain:

```json
{
  "source": "KB-QUAL-001",
  "requiredQualification": "NFS",
  "requiresEscalation": false
}
```

## Invalid qualification

Values outside the supported prototype set, for example:

```json
"teamQualification": ["DOCTOR"]
```

are rejected as a bad request rather than silently interpreted.

All knowledge in this prototype is fictional/non-official and must not be used for
real medical decision-making.
