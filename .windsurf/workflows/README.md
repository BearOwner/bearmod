# Windsurf Workflows

This directory contains Windsurf (Cascade) collaboration workflows. These files are documentation-first guides that define how Cascade operates for common tasks. They complement CI in `.github/workflows/` and docs in `docs/`.

Contents:
- `development.yml` — daily dev cycle for features/bugfixes
- `jni_change.yml` — safe JNI modification flow
- `release.yml` — versioning + changelog + tagging

Notes:
- These files are human- and AI-readable playbooks, not GitHub Actions.
- All new Activities/Classes must include Javadoc/KDoc and docs updates as per `CONTRIBUTING.md`.
