# Git and review workflow

Use `<type>/<kebab-case-description>` branches such as `feature/design-system`.

Commits use `<type>: <Korean subject>` or `<type>(<scope>): <Korean subject>`. Allowed commit types are `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, and `perf`.

PR titles use `[Feature] 제목`, `[Bug] 제목`, `[Refactor] 제목`, `[Chore] 제목`, or `[Design] 제목`.

- Link completed issues with `Closes #123`.
- Describe behavior changes and verification.
- Attach screenshots for UI changes.
- Request review only after local build, style checks, and relevant tests pass.
- Keep PR changes at 200 lines or fewer. The workflow warns at 180 reviewable added lines and fails above 200, excluding documentation, generated files, and binary assets.

Review in this order: correctness, security/privacy, architecture/state ownership, tests/error handling, then readability/style. Automation does not replace human review.
