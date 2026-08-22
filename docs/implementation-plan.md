# UMANG AI MVP — Proposed Implementation Plan

## 1. Scope and current repository state

This plan implements the product requirements as an independent hackathon prototype. It uses curated, mock scheme data only; it is not connected to UMANG or any government system. Results must always be presented as **potential matches**, with final eligibility determined by the relevant authority.

Repository review (2026-08-22): `backend/ai`, `backend/api`, `backend/eligibility`, `frontend/`, and `data/schemes/` exist but contain no implementation files. `docs/architecture.md`, `docs/demo-scenarios.md`, and `docs/hackathon-constraints.md` are empty. This document therefore defines the proposed baseline rather than documenting an existing implementation.

## 2. MVP outcome

A citizen can start an anonymous session, describe their situation in natural language, answer only the questions needed to assess the curated catalogue, and receive ranked potential matches. For each result the UI shows:

- matched facts and a plain-language explanation;
- unmet or unknown requirements, without asserting approval or rejection;
- required documents and a prototype next step;
- a visible mock-data and final-eligibility disclaimer.

The catalogue should initially contain 10–20 deliberately curated schemes that exercise the rule types below. Keep the first release focused on a small, defensible set of central/state examples and a single language (English), while storing a locale field for later expansion.

## 3. Architecture

Use a React + TypeScript single-page app, a Java/Spring Boot modular monolith, PostgreSQL, and one optional LLM integration behind an adapter. Docker Compose runs PostgreSQL and the backend locally; the frontend uses its normal development server.

```text
React web app
  ├─ chat/profile collection
  ├─ follow-up questions
  └─ match cards and scheme detail
           │ HTTPS / JSON
           ▼
Spring Boot API (api module)
  ├─ Conversation orchestration
  ├─ Scheme catalogue/query service
  └─ Results view-model assembly
       │                 │
       │                 ├────────► PostgreSQL (sessions, curated catalogue)
       ▼                 │
AI module                │
  └─ intent/attribute extraction adapter
       │                 │
       └── structured, validated profile patch
                         ▼
Eligibility module
  └─ deterministic rule evaluation, evidence, missing fields and scoring inputs
```

The LLM is not an authority and is never allowed to return eligibility decisions, rankings, or user-visible facts without server-side validation. The eligibility module is the sole source of match status and missing-requirement evidence.

### Request lifecycle

1. The client creates/resumes an anonymous conversation and sends a message.
2. The API passes the message plus the current canonical profile to the AI extractor.
3. The extractor returns a JSON profile patch, requested-field candidates, and a short conversational response; validation accepts only known fields and enumerated values.
4. The API merges valid values (with provenance and confidence) into the profile.
5. The eligibility engine evaluates every published curated scheme using that profile. It returns `MATCH`, `POSSIBLE_MATCH`, or `NOT_MATCHED` plus structured evidence.
6. The orchestration service selects the next question by expected value: ask only for a missing field that can change a `MATCH`/`POSSIBLE_MATCH` outcome. It stops when no material field remains or the user declines.
7. The API returns canonical profile changes, one optional follow-up question, provisional results, disclaimers, and the assistant text. The frontend renders structured results rather than parsing prose.

## 4. Module boundaries

Use Gradle or Maven multi-module layout only if it does not slow delivery; package-level modularity in one Spring Boot deployable is sufficient for the hackathon. The dependency direction is `api -> ai`, `api -> eligibility`; `eligibility` must not depend on `ai` or Spring web classes.

| Module | Responsibilities | Must not do |
|---|---|---|
| `backend/api` | REST controllers, request validation, orchestration, transaction boundary, persistence adapters, result DTOs | contain rule logic or prompt parsing |
| `backend/eligibility` | typed profile, rule AST/evaluator, match classifications, evidence, deterministic score inputs | call an LLM, database, or HTTP service |
| `backend/ai` | Spring AI client adapter, prompt templates, strict structured-output parsing, retries/timeouts, mock extractor for local/test use | set eligibility status or persist directly |
| `backend/catalog` (recommended addition) | scheme publication/query, mapping DB rows to rule definitions, catalogue validation | own conversations or make eligibility decisions |
| `frontend` | accessibility-first conversation UI, profile review/edit, result cards/details, API client and loading/error states | implement eligibility rules or retain sensitive data beyond the session |
| `data/schemes` | version-controlled JSON/YAML seed files and validation fixtures | live data scraping or government integration |

Keep domain types separate from transport types. Example core types are `CitizenProfile`, `ProfileFieldValue`, `SchemeDefinition`, `Requirement`, `EvaluationResult`, `EvidenceItem`, and `FollowUpQuestion`.

## 5. Deterministic eligibility model

Normalize user answers into a typed profile. Start with only attributes used by the curated catalogue:

```text
age (integer), gender (enum), stateCode (enum), district (string),
residentOfIndia (boolean), annualHouseholdIncome (decimal),
category (enum), occupation (enum), employmentStatus (enum),
farmer (boolean), landholdingHectares (decimal), disabilityPercent (integer),
student (boolean), educationLevel (enum), familyStatus (enum)
```

Each field carries `value`, `source` (`USER_CONFIRMED`, `LLM_EXTRACTED`, `USER_DECLINED`), `confidence`, and `updatedAt`. A user-confirmed value wins over an LLM-extracted value. Do not ask users for Aadhaar, bank account numbers, documents, full address, or other unnecessary sensitive identifiers.

Represent conditions as a small JSON rule DSL, rather than executable expressions. Support `all`, `any`, `not`, `equals`, `in`, `gte`, `lte`, `between`, `exists`, and `booleanIs`. This keeps evaluation inspectable and testable.

```json
{
  "all": [
    { "field": "residentOfIndia", "booleanIs": true },
    { "field": "age", "between": [18, 60] },
    { "field": "annualHouseholdIncome", "lte": 250000 }
  ]
}
```

Evaluate every leaf as `SATISFIED`, `UNSATISFIED`, or `UNKNOWN`. Combine with three-valued logic. A scheme is a `MATCH` only when its entire eligibility rule is satisfied; it is a `POSSIBLE_MATCH` if no requirement is disproved but one or more material values are unknown; otherwise it is `NOT_MATCHED`. Never use a label such as “eligible.”

Rank only `MATCH` and `POSSIBLE_MATCH` results, deterministically. Suggested sort keys: status (`MATCH` first), number of satisfied requirements descending, number of unknown material requirements ascending, explicit catalogue priority descending, then title ascending. This prevents opaque model-driven ranking.

## 6. Database schema

PostgreSQL is the operational store. Use UUID primary keys, UTC timestamps, Flyway migrations, and `jsonb` only where the schema deliberately supports a flexible rule/document structure. Seed scheme data through Flyway or a repeatable application seed process from the version-controlled catalogue.

| Table | Key columns / purpose |
|---|---|
| `scheme` | `id`, `code` (unique), `title`, `summary`, `level`, `state_code` nullable, `category`, `official_info_url` nullable, `catalogue_priority`, `status`, `version`, timestamps |
| `scheme_rule` | `id`, `scheme_id`, `rule_json jsonb`, `effective_from`, `effective_to` nullable; one active rule/version per scheme |
| `scheme_document` | `id`, `scheme_id`, `name`, `description`, `requiredness` (`COMMON`/`CONDITIONAL`), `condition_json jsonb` nullable, `display_order` |
| `scheme_next_step` | `id`, `scheme_id`, `label`, `url` nullable, `instructions`, `display_order` |
| `conversation` | `id`, `anonymous_token_hash`, `locale`, `status`, `expires_at`, timestamps; do not store a direct citizen identity |
| `profile_field_value` | `id`, `conversation_id`, `field_name`, `value_json jsonb`, `source`, `confidence`, `updated_at`; unique `(conversation_id, field_name)` |
| `message` | `id`, `conversation_id`, `role`, `content`, `extraction_json jsonb` nullable, timestamps; apply a short retention policy |
| `evaluation_snapshot` (optional) | `id`, `conversation_id`, `catalogue_version`, `profile_hash`, `result_json jsonb`, timestamp; useful for demo/debugging but not required for the first slice |

Store only necessary data and expire anonymous conversations (for example, 24 hours) with a scheduled cleanup job. Encrypt database volumes in deployed environments and avoid logging prompts, raw messages, or profiles at info level.

## 7. REST API contracts

Version endpoints under `/api/v1`. Return RFC 7807-style error bodies with a stable `code`, and include a correlation ID. Authentication is out of scope; an opaque, short-lived anonymous conversation token is returned at creation and supplied in an `X-Conversation-Token` header (or secure same-site cookie if the app is deployed together).

### Conversation endpoints

`POST /conversations`

Response `201`:

```json
{
  "conversationId": "uuid",
  "conversationToken": "opaque-token",
  "disclaimer": "Prototype using curated mock data. Final eligibility is determined by the relevant authority."
}
```

`POST /conversations/{conversationId}/messages`

Request:

```json
{ "text": "I am a 23-year-old student from Bihar and my family income is 2 lakh." }
```

Response `200`:

```json
{
  "assistantMessage": "I can check potential matches. One detail may help narrow them down.",
  "profile": {
    "age": { "value": 23, "source": "LLM_EXTRACTED", "needsConfirmation": true },
    "student": { "value": true, "source": "LLM_EXTRACTED", "needsConfirmation": true },
    "stateCode": { "value": "BR", "source": "LLM_EXTRACTED", "needsConfirmation": true }
  },
  "followUpQuestion": {
    "field": "category",
    "prompt": "Do you belong to any listed social category?",
    "answerType": "SINGLE_SELECT",
    "options": ["GENERAL", "SC", "ST", "OBC", "PREFER_NOT_TO_SAY"]
  },
  "results": [
    {
      "schemeCode": "MOCK-SCHOLARSHIP-001",
      "title": "Example Scholarship",
      "matchStatus": "POSSIBLE_MATCH",
      "whyMatched": ["You indicated that you are a student.", "Your reported income is within the configured threshold."],
      "unknownRequirements": ["Social category"],
      "unmetRequirements": [],
      "documents": [{ "name": "Income certificate", "requiredness": "COMMON" }],
      "nextSteps": [{ "label": "Review prototype scheme details", "url": null }],
      "disclaimer": "This is a potential match, not a final eligibility decision."
    }
  ]
}
```

`PUT /conversations/{conversationId}/profile/{field}` accepts an explicit answer (including `null`/decline where supported), validates its type, marks it `USER_CONFIRMED`, then re-evaluates. This powers edit/review controls without relying on natural-language extraction.

`GET /conversations/{conversationId}/results` returns the latest deterministic evaluation. `GET /schemes/{code}` returns public catalogue details, rule explanations in human-readable form, documents, next steps, provenance/version, and the prototype disclaimer; it must not expose raw internal rule JSON unless needed for a developer view.

## 8. AI workflow and guardrails

Use Spring AI through an interface such as `ProfileExtractionPort`. Provide two implementations: `SpringAiProfileExtractor` for configured model providers and `RuleBasedMockProfileExtractor` for local development, demos, and deterministic tests. If the external model is unavailable, preserve existing confirmed values, respond with a clear fallback message, and let the user use structured questions.

The extraction prompt should include: supported profile fields/types and valid enum values; current profile; instruction to extract only explicitly stated facts; instruction to return no eligibility conclusion, scheme recommendation, legal/official claim, or sensitive identifier; and a JSON schema. Expected output is constrained to:

```json
{
  "fieldUpdates": [{ "field": "age", "value": 23, "confidence": 0.92 }],
  "ambiguousFields": [],
  "assistantText": "..."
}
```

Server validation rejects unknown fields, wrong types, out-of-range values, inconsistent fields, and values below a conservative confidence threshold; uncertain extractions become a clarification question. Model output should be treated as untrusted input. Build explanations from the eligibility engine’s `EvidenceItem`s and catalogue copy, not generated free text. The model may phrase a brief neutral response, but the API must attach the fixed disclaimer and the UI must render it prominently.

For the MVP, do not use RAG: scheme rules and descriptions are curated, structured, and small enough to query directly. If catalogue-scale semantic search is later required, use it only to retrieve candidate schemes; pass candidates through the same deterministic evaluator.

## 9. Delivery slices

1. **Foundation:** initialize React/TypeScript and Spring Boot projects; configure PostgreSQL/Flyway, environment configuration, health endpoint, formatting/linting, and Docker Compose. Add the fixed prototype disclaimer to the UI shell.
2. **Catalogue and evaluator:** define the rule DSL and domain model; author 10–20 validated mock scheme files; load them into PostgreSQL; implement deterministic evaluation, evidence, ranking, and an API endpoint for a fixed test profile.
3. **Conversation/profile:** add anonymous conversations, explicit profile updates, material-question selection, result persistence, and the conversation/result REST contracts.
4. **AI adapter:** add structured extraction behind the port, mock fallback, schema/type validation, timeout/retry/cost limits, and a feature flag to disable external AI.
5. **Frontend:** build responsive chat, question controls, editable profile summary, match cards, scheme detail, empty/error states, and accessible disclaimer presentation.
6. **Hardening/demo:** add seed/demo scripts, observability with redaction, retention cleanup, deployment configuration, end-to-end smoke tests, and rehearsed demo scenarios.

## 10. Testing strategy

| Layer | Focus | Examples |
|---|---|---|
| Unit — eligibility | exhaustive deterministic correctness | boundary ages/income, `all`/`any`/`not`, unknown propagation, evidence, sort order |
| Unit — API/orchestration | validation and next-question selection | no unnecessary question, user-confirmed overwrite rules, token authorization, error mapping |
| Unit — AI adapter | untrusted structured output handling | invalid JSON, unknown field, enum/type error, low confidence, timeout fallback; never call a real model in ordinary tests |
| Repository/integration | PostgreSQL mappings and migrations | Flyway clean migration, scheme version selection, conversation expiry, seed idempotency |
| Contract | stable client/server payloads | OpenAPI validation and representative JSON fixture tests |
| Frontend | components and user flows | question widgets, evidence rendering, disclosure, edit/re-evaluate, network failures |
| End-to-end | one browser-to-database path | start a conversation, answer fields, see a potential match and missing requirement |
| Security/privacy | prevent unsafe regressions | secret/log scanning, dependency scan, no PII in logs, token isolation, input-size/rate-limit tests |

Maintain a table-driven eligibility fixture suite. Each curated scheme needs positive, negative, boundary, and incomplete-profile cases. Add a regression fixture whenever a rule or curated data issue is found. Use Testcontainers PostgreSQL for integration tests if development environments can support Docker; otherwise use a separately named local test database and run Testcontainers in CI.

## 11. Risks, mitigations, and assumptions

| Risk / assumption | Impact | MVP mitigation |
|---|---|---|
| Curated rules become stale or oversimplify real criteria | misleading results | version each scheme/rule, show source/update date and prototype disclaimer, use only reviewed mock data, never claim final eligibility |
| LLM invents facts or advice | unsafe/incorrect experience | schema-bound extraction, validation, profile confirmation, deterministic evaluator and evidence-based rendering |
| Sensitive data entered by users | privacy exposure | minimize fields, prohibit identifiers in UI/prompt, redacted logs, anonymous short-lived sessions and deletion job |
| Ambiguous language / multiple Indian languages | poor extraction | English-first scope, explicit form controls, retain a locale design for later translation; test common phrasing |
| Conflicting data supplied across turns | wrong results | source precedence, profile review screen, explicit confirmation, show values used in evaluation |
| Third-party model downtime/cost/latency | broken chat or demo | mock/rule-based fallback, strict timeout and token cap, feature flag, structured profile flow works without AI |
| Rules need geography-specific interpretation | incorrect broad matching | model state/district explicitly; mark state-specific schemes; only seed locations supported by curated data |
| Hackathon time limit | excessive architectural work | modular monolith, small catalogue, no authentication/RAG/event bus/microservices in MVP |
| Prototype mistaken for official UMANG service | trust and brand risk | independent branding, persistent “prototype/mock data” notice, no UMANG logos/source/API integrations, clear authority disclaimer |

Assumptions to validate before build: a model provider/API credential is available for the demo (otherwise demo uses mock extraction); the initial curated catalogue has an approved source/provenance note for every scheme; PostgreSQL and Docker are acceptable local dependencies; and deployment can serve the frontend and API over HTTPS from a controlled origin.

## 12. Definition of done

The MVP is ready to demo when a fresh browser session can complete an English-language scenario using seed data, see only deterministic potential-match outcomes with understandable evidence, review/edit the profile, see documents and next steps, and recover gracefully if the model is disabled. All output shows the prototype/final-authority disclaimer; no government/private API, submission, identity verification, or real transaction is implemented. Automated unit, integration, and one end-to-end smoke test pass from a clean environment.
