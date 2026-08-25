#!/usr/bin/env bash
# Calcula a próxima versão semver (vX.Y.Z) com base nos commits Conventional
# Commits desde a última tag vX.Y.Z válida (ignora tags fora desse formato).
#
# Saída:
#   stdout = "vX.Y.Z" e exit 0  -> há o que liberar
#   exit 2                       -> nenhum commit feat/fix/breaking desde a última tag
#   exit 1                       -> erro (ex.: nenhuma tag anterior encontrada)
set -euo pipefail

LAST_TAG=$(git tag --list | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | sort -V | tail -n1 || true)

if [ -z "$LAST_TAG" ]; then
  echo "Nenhuma tag semver anterior (vX.Y.Z) encontrada. Crie a versão inicial manualmente." >&2
  exit 1
fi

BUMP="none"
for hash in $(git log "${LAST_TAG}..HEAD" --format='%H'); do
  subject=$(git log -1 --format='%s' "$hash")
  body=$(git log -1 --format='%b' "$hash")

  if echo "$subject" | grep -qE '^[a-zA-Z]+(\([^)]*\))?!:' || echo "$body" | grep -q 'BREAKING CHANGE:'; then
    BUMP="major"
  elif echo "$subject" | grep -qE '^feat(\([^)]*\))?:' && [ "$BUMP" != "major" ]; then
    BUMP="minor"
  elif echo "$subject" | grep -qE '^fix(\([^)]*\))?:' && [ "$BUMP" = "none" ]; then
    BUMP="patch"
  fi
done

if [ "$BUMP" = "none" ]; then
  echo "Nenhum commit feat/fix/breaking desde $LAST_TAG." >&2
  exit 2
fi

IFS='.' read -r MAJOR MINOR PATCH <<< "${LAST_TAG#v}"

case "$BUMP" in
  major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
  minor) MINOR=$((MINOR + 1)); PATCH=0 ;;
  patch) PATCH=$((PATCH + 1)) ;;
esac

echo "v${MAJOR}.${MINOR}.${PATCH}"
