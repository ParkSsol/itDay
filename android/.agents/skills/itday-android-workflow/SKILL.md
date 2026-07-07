---
name: itday-android-workflow
description: Implement, plan, debug, refactor, review, test, document, and prepare GitHub work for the It-Day Android app. Use for It-Day feature development, API integration, Compose UI, architecture, testing, issues, pull requests, and CI workflow tasks. Apply Hilt or other optional tools only when they are configured.
---

# It-Day Android workflow

Read `references/project-context.md` for the full project context before broad project work.

Load only the detailed reference required for the task:

- Architecture: `references/rules/architecture.md`
- API/network: `references/rules/api-integration.md`
- Kotlin conventions: `references/rules/coding-conventions.md`
- Commits and PRs: `references/rules/commit-conventions.md`
- Hilt: `references/rules/hilt-di.md`
- Tests: `references/rules/testing.md`
- UI: `references/rules/ui-patterns.md`
- Feature, bug, API, issue, PR, test, and CI procedures: `references/workflows/`
- Planning, implementation, review, refactoring, bug analysis, and issue roles: `references/roles/`

All UI guidance in the preserved references has been converted for Compose. Follow `android/AGENTS.md`, repository-root `docs/libraries.md`, and repository-root `docs/coding-conventions.md`. Treat detailed library and architecture examples as proposals, inspect the current Gradle configuration first, and never generate code for an unconfigured library.

Always inspect nearby code, preserve unrelated changes, run the relevant Gradle checks, and report any verification that could not run.
