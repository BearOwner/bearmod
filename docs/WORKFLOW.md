# BearMod + Cascade Workflow

This document describes how we collaborate with Cascade (AI assistant) on BearMod.

## Principles
- Keep changes small, intentional, and documented.
- Prefer automated checks and reproducible steps.
- Always include documentation when adding new Activities or Classes.

## Roles
- You: product, decisions, approvals, and sensitive actions.
- Cascade: planning, drafts, code changes via diffs, CI integration, and docs.

## Daily Flow
1. Plan
   - Cascade proposes/updates a TODO plan and saves key decisions as memories.
   - Tasks are small and testable.
2. Implement
   - Cascade edits files via diffs; imports at top; runnable state preserved.
   - New Activities/Classes MUST include Javadoc/KDoc headers and README updates if user-facing.
3. Validate
   - Build locally and in CI.
   - Run unit and (when available) instrumentation tests.
4. Review
   - PRs use the template and checklist; link tasks.
   - Require green CI before merge.

## Definitions of Done
- Code compiles; CI passes assemble + lint + tests.
- Docs updated: Javadoc/KDoc and relevant README/CHANGELOG.
- For JNI changes: headers, function names, and signatures validated; add notes in `JNI_FIX_SUMMARY.md` if needed.

## CI Gates (expected)
- Android Lint
- Unit tests (JUnit/Mockito/robolectric as applicable)
- Formatting (Spotless/ktlint or google-java-format)

## Cascade Operating Rules
- Use TODO list to manage tasks; mark done immediately upon completion.
- Keep responses concise with Markdown formatting.
- Reference code by path/symbol (e.g., `app/src/main/java/...`).
- For new Activities/Classes, include a top-of-file documentation block describing purpose, inputs, outputs, and threading/lifecycle expectations.
