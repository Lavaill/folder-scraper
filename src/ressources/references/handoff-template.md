<!--
Reply with the sentence below, then the fenced block below filled in, and nothing else after the block.
Keep the headings and their order. Omit inapplicable sections.
State each requirement once. No nested code fences, links, or citations inside the block.
Do not reproduce these instructions. Size budgets are in SKILL.md.
-->

Paste the block below into the implementation agent:

```markdown
## Task
[One precise paragraph stating the objective and scope.]

## Acceptance criteria
- [Observable outcome]
- [Exact literals, boundaries, or compatibility requirement]

## Context
[Include only facts that prevent a wrong assumption or exploration: framework and library versions, test framework and naming conventions, relevant call path. Omit if none.]

## Changes

### 1. `<exact repository-relative path>` | `<class/object/trait>` | `<method/member>`
- [Exact required modification]
- Call sites: [list each affected caller by file and member; omit if the signature is unchanged]
- Tests: [specific test additions or updates]

[Repeat for each changed file.]

## Configuration
[Include only if applicable. Separate agent responsibility from manual user action.]

## Validation
- [Verified focused or full command]
- [Any required compile/search check not already covered by the build]

## Constraints
- [Only ticket-specific exclusions or protected-file constraints]

## Report back
- Files changed
- Test command and result
- Any deviation or unresolved compilation issue
```