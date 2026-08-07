#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WITH_WEB=false
PIDS=()

usage() {
  cat <<'USAGE'
Usage: ./scripts/dev-local.sh [--web]

Starts the independent MuYunSpringApp local stack:
  PostgreSQL on 127.0.0.1:54322 and app-boot on 127.0.0.1:8081.

Options:
  --web     Also start app-web on http://127.0.0.1:5174/.

Environment:
  MUYUN_REPOSITORY  Optional local Maven repository containing a framework consumer build.
  MUYUN_SPRING_VERSION  Framework version used with MUYUN_REPOSITORY (required when MUYUN_REPOSITORY is set).
USAGE
}

cleanup() {
  for pid in "${PIDS[@]:-}"; do
    kill "$pid" 2>/dev/null || true
  done
}

wait_for_postgres() {
  local attempts=30
  while ((attempts > 0)); do
    if docker compose exec -T postgres pg_isready -U postgres -d muyun_spring_app >/dev/null 2>&1; then
      return
    fi
    sleep 1
    attempts=$((attempts - 1))
  done
  echo "PostgreSQL did not become ready within 30 seconds." >&2
  exit 1
}

verify_node_version() {
  node -e '
    const [major, minor, patch] = process.versions.node.split(".").map(Number);
    if (major < 22 || (major === 22 && (minor < 23 || (minor === 23 && patch < 0)))) {
      console.error(`Node.js >=22.23.0 is required, found ${process.versions.node}.`);
      process.exit(1);
    }
  '
}

while (($# > 0)); do
  case "$1" in
    --web) WITH_WEB=true ;;
    -h|--help) usage; exit 0 ;;
    *) usage >&2; exit 1 ;;
  esac
  shift
done

trap cleanup INT TERM EXIT
cd "$ROOT_DIR"

if [[ "$WITH_WEB" == true ]]; then
  verify_node_version
fi
if [[ -n "${MUYUN_REPOSITORY:-}" && -z "${MUYUN_SPRING_VERSION:-}" ]]; then
  echo "MUYUN_SPRING_VERSION is required when MUYUN_REPOSITORY is set." >&2
  exit 1
fi

if [[ ! -f application-local.yml ]]; then
  cp app-boot/src/main/resources/application-local.yml.example application-local.yml
  echo "Created ignored application-local.yml from the example."
fi

docker compose up -d
wait_for_postgres

PORTS=(8081)
if [[ "$WITH_WEB" == true ]]; then
  PORTS+=(5174)
fi
for port in "${PORTS[@]}"; do
  if lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port $port is already in use. Stop the existing process before starting the App stack." >&2
    exit 1
  fi
done

if [[ "$WITH_WEB" == true ]]; then
  npm ci --prefix app-web
fi

GRADLE_ARGS=()
if [[ -n "${MUYUN_REPOSITORY:-}" ]]; then
  GRADLE_ARGS+=("-PmuyunRepository=$MUYUN_REPOSITORY")
  GRADLE_ARGS+=("-PmuyunSpringVersion=${MUYUN_SPRING_VERSION}")
  GRADLE_ARGS+=("--refresh-dependencies")
fi
if ((${#GRADLE_ARGS[@]})); then
  ./gradlew "${GRADLE_ARGS[@]}" :app-boot:bootRun --args='--spring.profiles.active=local' &
else
  ./gradlew :app-boot:bootRun --args='--spring.profiles.active=local' &
fi
PIDS+=("$!")

if [[ "$WITH_WEB" == true ]]; then
  npm run dev --prefix app-web &
  PIDS+=("$!")
fi

echo "Backend:  http://127.0.0.1:8081"
if [[ "$WITH_WEB" == true ]]; then
  echo "Frontend: http://127.0.0.1:5174/"
fi
echo "Press Ctrl-C to stop application processes; PostgreSQL remains running."

wait "${PIDS[0]}"
