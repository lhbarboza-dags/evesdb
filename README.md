# SIBOL-EVES (React + Spring Boot)

This folder is the new version of the SIBOL-EVES ecosystem-services valuation database. The ZK UI of `../SIBOL-EVES_DB` is replaced by a React single-page app on top of a Spring Boot REST API. It uses the same PostgreSQL database and leaves the schema unchanged. The old app stays in `../SIBOL-EVES_DB` as the reference.

```
SIBOL-EVES-NEXT/
├── backend/            Spring Boot 2.7 (Java 17) REST API — JPA entities, repositories and services ported from the ZK app
├── frontend/           React 18 + TypeScript + Vite + MUI; nginx image serves it and proxies /api
├── docs/migration/     ZUL_MAPPING.md, EVENTS.md (generated), BEHAVIOUR_CHANGES.md, API.md, mapping.mjs (source)
├── scripts/            legacy-events.mjs (extracts every ZK event), verify-event-coverage.mjs (checks + generates docs)
├── docker-compose.yml  backend + frontend; connects to your existing PostgreSQL
└── .env.example        every setting / secret (copy to .env)
```

## Run with Docker

1. Copy the settings file and fill in the values:
   ```bash
   cp .env.example .env
   ```
   - **Database connection:** `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
   - **Legacy password cipher:** `LEGACY_AES_SECRET` and `LEGACY_AES_SALT`. These must be the `SECRET_KEY` and `SALT` constants of `SIBOL-EVES_DB/src/main/java/org/sibol/eves/app/AES256.java`, or existing users cannot log in.
   - **E-mail:** the SMTP settings, plus `APP_BASE_URL`, the public URL used in e-mailed links.
   - **reCAPTCHA and Google Maps:** the reCAPTCHA keys and `GOOGLE_MAPS_API_KEY`.
2. Make sure PostgreSQL accepts connections from Docker containers:
   - `postgresql.conf`: `listen_addresses = '*'` (or the host's Docker bridge address).
   - `pg_hba.conf`: allow the Docker network, for example `host all all 172.16.0.0/12 scram-sha-256`.
   - Inside the containers, `host.docker.internal` is the machine running Docker. `docker-compose.yml` maps it with `host-gateway`.
3. Build the jar and the React bundle on the host, then build and start the containers:
   ```bash
   ./scripts/build.sh          # mvn package + npm ci && npm run build (needs JDK 17+, Maven, Node 20+)
   docker compose up --build
   ```
   The default Dockerfiles only copy `backend/target/sibol-eves-api.jar` and `frontend/dist/`, so the Docker build needs no
   internet. Where Docker builds *can* reach Maven Central and npm (for example CI), `Dockerfile.multistage` in `backend/`
   and `frontend/` builds everything inside Docker instead (set `dockerfile: Dockerfile.multistage` in `docker-compose.yml`).
4. Open http://localhost:8080 (change it with `WEB_PORT`).

> **No internet inside containers (Docker Desktop on this WSL host).** Containers here can reach the Windows host
> (Postgres) but not the internet, so Gmail and reCAPTCHA calls cannot leave the backend container. For local testing set
> `MAIL_ENABLED=false` (mails are written to the backend log) and `RECAPTCHA_ENABLED=false`. To give containers internet,
> remove the `"dns"` override from Docker Desktop → Settings → Docker Engine, and/or configure the proxy for containers
> under Settings → Resources → Proxies.

The backend runs with `JPA_DDL_AUTO=validate`. If the database does not match the entities, it refuses to start; it never alters the schema.

> **Database name:** the old `persistence.xml` points at a database named `sibol-eves`. The local database used during this migration is called `evesdb`, so set the URL to whichever one you use.

## Free hosting

For a free single-container deployment (Spring Boot serving the API and the React app, plus a managed Postgres), see [deploy/README-FREE.md](deploy/README-FREE.md). It covers Northflank Sandbox (recommended) and, as a fallback, Render + Neon. A self-managed VM setup with Caddy is in [deploy/README-DEPLOY.md](deploy/README-DEPLOY.md).

## Local development (without Docker)

```bash
# backend (needs JDK 17+ and Maven)
cd backend
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/evesdb SPRING_DATASOURCE_PASSWORD=... \
       LEGACY_AES_SECRET=... LEGACY_AES_SALT=... MAIL_ENABLED=false RECAPTCHA_ENABLED=false
mvn spring-boot:run                       # http://localhost:8080/api

# frontend (needs Node 20+)
cd frontend
npm install
npm run dev                               # http://localhost:5173, proxies /api to :8080
```

`MAIL_ENABLED=false` logs e-mails instead of sending them. `RECAPTCHA_ENABLED=false` skips the captcha.

## Tests

```bash
cd backend && mvn test           # unit tests; *IT tests also run when SPRING_DATASOURCE_URL is set
cd frontend && npm test          # Vitest (study graph rules, DMS conversion, validation)
node scripts/verify-event-coverage.mjs           # migration coverage check (exit 1 if anything is unmapped)
node scripts/verify-event-coverage.mjs --write   # … and regenerate docs/migration/ZUL_MAPPING.md + EVENTS.md
```

The `*IT` tests run against the real database inside rolled-back transactions, so nothing is written:

- **`DatabaseIT`:** search, summary, map and export row counts, the full study lifecycle (create, edit, approve, delete), and dictionary terms.
- **`SecurityIT`:** the role rules for every area.

## How the migration is tracked

- **[ZUL_MAPPING.md](docs/migration/ZUL_MAPPING.md):** all 93 `.zul` files, each with its controller, its React route or component, and its status (Migrated / Fixed / Merged / Dropped with a reason).
- **[EVENTS.md](docs/migration/EVENTS.md):** all 455 UI event sources of the old app, grouped by zul page. Each row has the legacy `file:line`, the React handler, the status, and a checkbox for manual verification. The sources are `@Listen`, renderer `addEventListener`, zul `on*` attributes, links, and every button or menu item with an id.
- **[BEHAVIOUR_CHANGES.md](docs/migration/BEHAVIOUR_CHANGES.md):** every legacy bug that was fixed, and everything that intentionally stayed the same.
- **[API.md](docs/migration/API.md):** the REST endpoints, their roles, and the ZK code each one replaces.

`verify-event-coverage.mjs` re-extracts the events from `../SIBOL-EVES_DB` on every run. It fails if a page or event has no mapping, or if a React reference no longer exists in the code. That way the mapping cannot silently drift.

## Switch-over notes

- Both apps can run against the same database at the same time. Passwords stay in the legacy AES format until `APP_AUTH_UPGRADE_HASH=true`.
- Old URLs (`search.zul`, `validate.zul?token=…` and so on) redirect to the new routes, so bookmarks and e-mailed links keep working.
- Change `APP_BASE_URL` once the new app has its public address. It is used in verification, reset and invitation e-mails.
