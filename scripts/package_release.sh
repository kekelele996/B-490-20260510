#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_NAME="$(basename "$ROOT_DIR")"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
RELEASE_DIR="$ROOT_DIR/release"
ARCHIVE_PATH="$RELEASE_DIR/${PROJECT_NAME}_release_${TIMESTAMP}.tar.gz"

mkdir -p "$RELEASE_DIR"

# Lightweight package mode:
# - Never run automated tests here.
# - Exclude local caches, VCS, temporary artifacts, and test-related files.
EXCLUDES=(
  "--exclude-vcs"
  "--exclude=release"
  "--exclude=.git"
  "--exclude=.github"
  "--exclude=.gitlab"
  "--exclude=.idea"
  "--exclude=.vscode"
  "--exclude=.DS_Store"
  "--exclude=*.tar.gz"
  "--exclude=node_modules"
  "--exclude=dist"
  "--exclude=coverage"
  "--exclude=.nyc_output"
  "--exclude=venv"
  "--exclude=.venv"
  "--exclude=target"
  "--exclude=targrt"
  "--exclude=__pycache__"
  "--exclude=.pytest_cache"
  "--exclude=.mypy_cache"
  "--exclude=.tox"
  "--exclude=*.py"
  "--exclude=*.pyc"
  "--exclude=*.pyo"
  "--exclude=backend/uploads"
  "--exclude=test"
  "--exclude=tests"
  "--exclude=__tests__"
  "--exclude=*.test.*"
  "--exclude=*.spec.*"
  "--exclude=*test*.sh"
  "--exclude=*pytest*"
  "--exclude=*vitest*"
  "--exclude=*jest*"
  "--exclude=*cypress*"
  "--exclude=*playwright*"
)

tar -czf "$ARCHIVE_PATH" \
  "${EXCLUDES[@]}" \
  -C "$ROOT_DIR" .

echo "Package created: $ARCHIVE_PATH"
