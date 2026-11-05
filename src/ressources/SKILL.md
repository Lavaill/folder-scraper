---
name: java-scala-project-handoff
description: analyze attached plain-text exports of java or scala projects and turn implementation tickets into verified, token-efficient plans and copy-pastable handoffs for low-context coding agents. use when a user supplies a packaged project export and asks for impact analysis, ticket clarification, implementation planning, or an agent handoff. inspect the export before proposing changes, resolve ambiguities with the user, account for inaccessible configuration files, and separate reviewed plans from final handoffs.
---
# Java and Scala project handoff

Analyze an attached text export of a Java or Scala project, clarify the ticket, produce a reviewable plan, and only after approval produce a self-contained handoff for a low-context implementation agent.

## Non-negotiable rules

- Open and read every attached project export before reasoning about the implementation.
- Treat the export as the source of truth. Do not assume framework versions, APIs, matchers, constants, paths, build tools, or conventions.
- Optimize the handoff for fewer agent exploration tokens, not for minimum handoff length.
- Include only verified project facts and decisions approved by the user. Label remaining uncertainty explicitly.
- Never claim that the implementation agent can edit protected configuration files.
- When configuration is required, tell the implementation agent which property names and values it may rely on, but instruct the user separately to add them manually, usually in `application.properties`.
- Support Java and Scala. Follow the language, framework, build tool, and test conventions found in the export.
- Do not produce the final agent handoff until the user accepts the plan.

## Workflow

### 1. Inspect the project export

Build a compact project map before designing:

1. Identify the project language or mixed-language structure.
2. Identify build files and exact dependency/plugin versions.
3. Locate the ticket's entry points, call path, domain models, validation, persistence, error mapping, configuration, and tests.
4. Locate all direct call sites of methods or constructors likely to change.
5. Read relevant configuration files if present, while recording that the implementation agent cannot access or modify them.
6. Detect duplicated modules, generated code, multiple application versions, excluded paths, and unusual test conventions.
7. Record exact class, object, trait, method, and file names.

Prefer targeted searches through the export, followed by reading complete relevant class sections. Do not summarize the whole project.

Use `references/inspection-checklist.md` while inspecting.

### 2. Resolve ticket ambiguities

Before proposing a plan, ask focused decision questions whenever an ambiguity could change behavior, compatibility, API contracts, tests, or file ownership.

Examples:

- inclusive versus exclusive limits;
- elapsed duration versus calendar-day calculation;
- expected error body and headers;
- configuration key, default, units, and invalid-value behavior;
- whether changes must preserve binary or source compatibility;
- whether validation belongs at controller, service, domain, or repository level;
- deployment or configuration changes requiring another owner.

For each ambiguity:

1. State why it matters.
2. Give two or three concrete options grounded in the project.
3. Recommend one option with a short rationale.
4. Ask the user to choose or consult the product owner.

Do not ask about choices already settled by the ticket or project conventions. Batch related questions and keep them answerable.

### 3. Produce a plan for review

After ambiguities are resolved, provide a concise but complete plan using `references/plan-template.md`.

The plan must:

- state verified current behavior;
- state approved target behavior and boundaries;
- list exact files/classes to add or modify;
- describe each change at method or member level;
- identify configuration additions as manual user actions;
- list tests and exact boundary cases;
- identify files that do not need changes when that prevents wasted exploration;
- include risks, compatibility constraints, and unresolved items;
- avoid speculative code snippets unless syntax is essential to validate the design.

Stop and request plan approval. Incorporate corrections before proceeding.

### 4. Produce the copy-pastable agent handoff

Only after explicit approval, output a standalone handoff using `references/handoff-template.md`.

Assume the implementation agent has low context and cannot access protected property files. The handoff must be sufficient to implement without broad repository exploration.

Requirements:

- Begin with task objective and acceptance criteria.
- Include verified project facts: versions, conventions, call path, and existing behavior relevant to the ticket.
- Provide ordered changes with exact file, class/object/trait, method/member, change, rationale, and associated tests.
- Give exact symbols only when verified in the export. Otherwise describe the required semantics and mark the symbol for local verification.
- List every known call site whose signature must be updated.
- Put configuration in a dedicated section named `Configuration contract and manual user action`.
- Tell the agent it may rely on the listed keys and values but must not attempt to open or edit protected configuration files.
- Tell the user, outside the agent handoff, exactly what to add manually and where.
- Include focused test commands only if the build tool and syntax are verified.
- End with a completion checklist and a `Do not change` section.
- Avoid repeating the same rationale in multiple sections.

### 5. Handle implementation discoveries

If the user returns with agent feedback or implementation deviations:

1. Compare each deviation with the verified export and approved decision.
2. Classify it as project fact missed, agent limitation, changed requirement, or implementation error.
3. Correct the plan or handoff narrowly.
4. Preserve approved decisions unless new evidence invalidates them.

## Output discipline

- During inspection, ask questions rather than dumping exploratory notes.
- Keep the review plan smaller than the final handoff.
- Make the handoff detailed where details eliminate exploration: exact locations, call sites, versions, conventions, boundaries, and tests.
- Omit generic Java, Scala, Spring, Maven, Gradle, sbt, or testing explanations.
- Distinguish labels consistently: `Verified`, `Approved decision`, `Manual user action`, `Agent constraint`, and `Unresolved`.
