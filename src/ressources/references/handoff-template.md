# Copy-pastable implementation-agent handoff

## Objective
[One precise paragraph]

## Acceptance criteria
- [Observable outcomes]

## Verified project context
- Language/build/framework versions: [exact]
- Relevant call path: `[entry] -> [service] -> [validation] -> [downstream]`
- Existing error/config/test conventions: [ticket-relevant facts]

## Approved decisions
- [Decision and exact semantics]

## Agent constraints
- Do not attempt to inspect or edit protected configuration files.
- Use the configuration contract below as authoritative.
- Do not replace verified project APIs with newer framework conveniences.
- Limit exploration to the listed files and call sites unless compilation reveals an unlisted dependency.

## Configuration contract and manual user action
### Values the agent may rely on
- `[key]`: `[default, unit, semantics]`

### Agent responsibility
- Wire and consume the property in code and tests as described.
- Do not edit the protected property file.

### Manual user action
- The user will add this line to `[application.properties or verified file]`:
  `[exact property line]`

## Ordered implementation changes
### 1. `[path]` | `[class/object/trait]`
- Location: `[method/member]`
- Change: [exact behavioral modification]
- Rationale: [one sentence]
- Call sites to update: [exhaustive verified list]
- Tests: [specific tests]

[Repeat without duplicating rationale]

## Test matrix
- [input/boundary] -> [expected behavior/status/exception]
- [invalid input] -> [expected behavior]
- [rejected request] -> [downstream collaborator not invoked]

## Verified commands
- `[focused command]`
- `[full command]`

Omit this section if commands were not verified.

## Do not change
- [Explicit exclusions]

## Completion checklist
- [ ] [Requirement]
- [ ] Protected configuration file was not edited by the agent.
- [ ] All listed call sites compile.
- [ ] Focused and full tests pass.
