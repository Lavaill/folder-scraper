# Review plan template

## Verified current behavior
- [Only facts evidenced by the export]

## Approved target behavior
- [Acceptance criteria and exact boundaries]

## Proposed changes
1. `<path>`: `<class/object/trait>`
   - Change: [member-level description]
   - Reason: [ticket-specific reason]
   - Tests: [affected tests]

## Configuration contract
- Key: `[exact key]`
- Default/unit: `[value and unit]`
- Agent constraint: the implementation agent cannot inspect or modify the protected property file.
- Manual user action: add `[exact line]` to `[file, usually application.properties]`.

## Tests
- [Boundary, error mapping, non-invocation, regression cases]

## Do not change
- [Relevant files/components intentionally unaffected]

## Risks or unresolved items
- [None, or explicit decisions still required]

End with: `Please approve or correct this plan. I will produce the copy-pastable implementation-agent handoff only after approval.`
