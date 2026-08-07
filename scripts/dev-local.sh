#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WITH_WEB=false
WEB_LINKED=false
PIDS=()

usage() {
  cat <<'USAGE'
Usage: ./scripts/dev-local.sh [--web | --web-linked]

Starts the independent MuYunSpringApp local stack:
  PostgreSQL and App ports come from .env (defaults: 54322 and 8081).

Options:
  --web     Also start app-web on the port configured in .env (default: 5174).
  --web-linked  Start app-web from its existing npm link without running npm ci.

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
    if docker compose exec -T postgres pg_isready -U postgres -d "$MUYUN_APP_POSTGRES_DB" >/dev/null 2>&1; then
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
    --web-linked) WITH_WEB=true; WEB_LINKED=true ;;
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
if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Created ignored .env from the example. Set unique local ports and names when running multiple Apps."
fi

set -a
. ./.env
set +a

docker compose up -d
wait_for_postgres

PORTS=("$MUYUN_APP_SERVER_PORT")
if [[ "$WITH_WEB" == true ]]; then
  PORTS+=("$MUYUN_APP_WEB_PORT")
fi
for port in "${PORTS[@]}"; do
  if lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port $port is already in use. Stop the existing process before starting the App stack." >&2
    exit 1
  fi
done

if [[ "$WITH_WEB" == true && "$WEB_LINKED" == false ]]; then
  npm ci --prefix app-web
fi
if [[ "$WEB_LINKED" == true && ! -L app-web/node_modules/@ximatai/muyun-web-app ]]; then
  echo "--web-linked requires app-web/node_modules/@ximatai/muyun-web-app to be an npm link." >&2
  echo "Run npm link /path/to/MuYunSpring/build/consumer-npm/staging/web-app first." >&2
  exit 1
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
  if [[ "$WEB_LINKED" == true ]]; then
    npm run dev:linked --prefix app-web &
  else
    npm run dev --prefix app-web &
  fi
  PIDS+=("$!")
fi

echo "Backend:  http://127.0.0.1:$MUYUN_APP_SERVER_PORT"
if [[ "$WITH_WEB" == true ]]; then
  echo "Frontend: http://127.0.0.1:$MUYUN_APP_WEB_PORT/"
fi
echo "Press Ctrl-C to stop application processes; PostgreSQL remains running."

wait "${PIDS[0]}"
