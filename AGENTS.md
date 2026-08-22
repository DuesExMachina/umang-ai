# UMANG AI - Project Instructions

## Product

UMANG AI is an independent hackathon prototype
that reimagines government service and scheme discovery.

## Core problem

A novice citizen cannot easily discover government
schemes they may be eligible for.

## Target platform

UMANG.

## Important constraints

- This is NOT an official UMANG product.
- Do not copy UMANG source code.
- Do not reverse engineer private APIs.
- Do not access private government systems.
- Use mock/curated scheme data for the prototype.
- Clearly identify prototype/mock data.
- Never claim final government eligibility.
- Final eligibility is determined by the relevant authority.

## Architecture

Frontend:
- React
- TypeScript

Backend:
- Java
- Spring Boot
- Spring AI

Database:
- PostgreSQL

Testing:
- JUnit
- Mockito
- integration tests where appropriate

## Engineering principles

- Keep business rules deterministic.
- Do not let the LLM directly determine eligibility.
- LLM handles intent extraction, conversation and explanation.
- Eligibility engine evaluates structured rules.
- Prefer simple architecture suitable for a hackathon.