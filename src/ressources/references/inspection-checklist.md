# Inspection checklist

Use this checklist for internal inspection, not as an output outline.
Do not include a discovered fact in the plan or handoff unless it affects the implementation, tests, compatibility, configuration, or scope.

## Project identity
- Java, Scala, or mixed
- Maven, Gradle, or sbt
- framework and exact versions
- modules and source roots
- test frameworks and mocking libraries

## Change path
- endpoint, consumer, job, or public entry point
- controller/resource/route
- service/use case
- validator/domain factory
- repository/client
- exception translation and response contract
- serialization models
- relevant configuration

## Compatibility checks
- all constructor and method call sites
- interfaces, traits, and implementations
- generated sources
- public API or library contracts
- framework symbols available in the pinned version
- test matchers and helpers available in pinned test libraries

## Security and ownership
- files visible in export but inaccessible to implementation agent
- secrets or environment overrides
- files requiring manual user changes
- generated, vendored, or excluded files

## Testing
- current naming and fixture conventions
- unit versus integration boundaries
- boundary and negative cases
- proof that rejected input does not reach expensive downstream code
- focused and full-suite commands, only when verified
