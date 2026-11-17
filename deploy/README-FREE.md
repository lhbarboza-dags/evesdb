# Free hosting: Render + Neon (no card) or Northflank Sandbox

Both options use the **single-container image** built from the root `Dockerfile`: Spring Boot serves the REST API **and** the React app. One service, one database, nothing to maintain on a server. Free-tier facts were checked on 2026-11-17 (sources at the bottom).

| | Northflank Sandbox | Render Free + Neon Free |
|---|---|---|
| Cost | $0, but signup asks for a card (verification) | $0; Neon needs no card, and Render's own article says no card |
| App | Always on, no sleeping (per Northflank's pricing page) | Sleeps after 15 min idle; first visit afterwards waits ~1 min; users are logged out |
| Database | 1 managed database included | Neon: suspends after 5 min idle, wakes in < 1 s automatically |
| Limits | Up to $20/month of resources, 2 services, 1 database, best-effort support, no SLA | 512 MB RAM / 0.1 CPU, 750 h/month |
| HTTPS + custom domain | Included | Included |

> Northflank describes the Sandbox as meant for hobby projects and testing, not production. For a small research database that's an acceptable trade-off; if the site becomes business-critical, move to a paid plan (Northflank ~$12/month for 0.5 vCPU / 1 GB) or a small VPS using `deploy/compose.prod.yml`.

---

## 0. Prerequisites
1. **The code is on GitHub**: `https://github.com/lhbarboza-dags/evesdb` (`main`). Both platforms build from it.
2. **A dump of the database** you want to publish. From the PC that has `evesdb`:
   ```bash
   pg_dump -h <host> -U postgres -Fc --no-owner --no-acl evesdb > evesdb.dump
   ```
3. **The secrets** (never committed):
   - `LEGACY_AES_SECRET` and `LEGACY_AES_SALT`: the constants from the old `AES256.java`; without them existing users can't log in.
   - Optional: Gmail app password, reCAPTCHA keys (registered for your domain) and Google Maps key.
   - Until you have mail and reCAPTCHA keys, set `MAIL_ENABLED=false` and `RECAPTCHA_ENABLED=false`.

## A. Northflank (always on; signup asks for a card)
Menu names below may differ slightly in Northflank's current UI; pick the closest match.

1. **Sign up** at https://northflank.com with GitHub (no card). Create a **project** (choose the region closest to the Philippines that is offered).
2. **Database:** in the project, **Add new → Addon → PostgreSQL**.
   - Pick the smallest plan and storage.
   - Pick the PostgreSQL version that matches your dump (17 if offered, otherwise the newest).
   - Create it, then open its **Connection details**: host, port, database, user, password.
3. **Load the data** into the addon. Use one of these routes:
   - **If the addon offers external access** (toggle "Publicly accessible" / external connection):
     ```bash
     pg_restore -h <external-host> -p <port> -U <user> -d <database> --no-owner --no-acl evesdb.dump
     ```
     Afterwards you can switch external access off again.
   - **Otherwise** install the Northflank CLI (`npm i -g @northflank/cli`, then `northflank login`). Run `northflank forward addon --projectId <project> --addonId <addon>`, which tunnels the database to `localhost`, then run `pg_restore` against `localhost`.
4. **App service:** **Add new → Service → Combined service** (build + deploy).
   - **Repository:** `lhbarboza-dags/evesdb`, branch `main`.
   - **Build:** Dockerfile, path `/Dockerfile`, context `/`.
   - **Resources:** the largest plan that keeps the project within the free $20/month allowance. Aim for **≥ 1 GB RAM**; 512 MB works but is slower.
   - **Networking:** port **8080**, HTTP, **public**.
   - **Health check:** HTTP `GET /actuator/health` on port 8080, with an initial delay of ~90 s.
   - **Environment / secrets** (prefer a Northflank *secret group* for the sensitive ones):
     ```
     SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>?sslmode=require
     SPRING_DATASOURCE_USERNAME=<user>
     SPRING_DATASOURCE_PASSWORD=<password>
     JPA_DDL_AUTO=validate
     LEGACY_AES_SECRET=...
     LEGACY_AES_SALT=...
     APP_AUTH_UPGRADE_HASH=false
     APP_BASE_URL=https://<your public URL or custom domain>
     SESSION_COOKIE_SECURE=true
     MAIL_ENABLED=false            # true once SMTP_USERNAME/SMTP_PASSWORD are set
     RECAPTCHA_ENABLED=false       # true once RECAPTCHA_SITE_KEY/RECAPTCHA_SECRET are set
     GOOGLE_MAPS_API_KEY=
     ```
     For the internal addon connection, the `sslmode=require` part may not be needed; remove it if the connection fails with an SSL error.
   - **Create.** The first build takes ~5–10 minutes (Maven + npm on Northflank's builders). Watch the build log, then the runtime log for `Started SibolEvesApplication`.
5. **Domain:** service → **Networking / Domains** → add `eves.<your-domain>`.
   - Create the DNS record Northflank shows (usually a CNAME) at your registrar; TLS is issued automatically.
   - Then update `APP_BASE_URL` to the domain and redeploy.

## B. Render + Neon (free; Neon needs no card)
Render runs the app (free web service: 512 MB RAM, 0.1 CPU, sleeps after 15 min idle, 750 h/month). Neon hosts PostgreSQL (free: suspends after 5 min idle, wakes in well under a second, 100 compute-hours per month).

### B1. Neon: create the database (~10 min)
1. Sign up at https://console.neon.tech/signup with GitHub or Google (no card).
2. **Create project**: name `sibol-eves`, Postgres version **17**, region **AWS Asia Pacific (Singapore)**.
3. On the project dashboard click **Connect**:
   - switch **Connection pooling OFF** (use the *direct* endpoint; the pooled one breaks the Java driver's prepared statements);
   - copy the connection string. It looks like
     `postgresql://<user>:<password>@<host>/<db>?sslmode=require&channel_binding=require`
   - Note the parts: `<host>` (e.g. `ep-xxxx.ap-southeast-1.aws.neon.tech`), `<db>` (usually `neondb`), `<user>`, `<password>`.
4. **Load the data from Windows** (PowerShell, on the PC that has the local `evesdb`; Windows' PostgreSQL 17 tools have internet access, Docker containers on that PC do not):
   ```powershell
   cd $HOME\Downloads
   $env:PGPASSWORD = "<local postgres password>"
   & "C:\Program Files\PostgreSQL\17\bin\pg_dump.exe" -h localhost -U postgres -Fc --no-owner --no-acl -f evesdb.dump evesdb
   Remove-Item Env:PGPASSWORD
   $NEON = "<the Neon connection string from step 3>"
   & "C:\Program Files\PostgreSQL\17\bin\pg_restore.exe" --no-owner --no-acl -d $NEON evesdb.dump
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" $NEON -c "select count(*) from studysrc"
   ```
   The count must be **139**, the same as the local database. If the restore prints a few warnings about schema `public` already existing, that's harmless; anything about tables failing is not, so stop and check.

### B2. GitHub
Render deploys from https://github.com/lhbarboza-dags/evesdb (branch `main`). Make sure the latest code is pushed there.

### B3. Render: create the web service (~15 min + build)
1. Sign up at https://dashboard.render.com/register with **GitHub**, and allow Render to access the `lhbarboza-dags/evesdb` repository.
2. **New → Web Service**, then pick `lhbarboza-dags/evesdb`:
   - **Name:** e.g. `evesdb` (this becomes `https://evesdb.onrender.com`; Render shows the real URL at the top of the service page). Use **your** URL everywhere below;
   - **Region:** Singapore;
   - **Branch:** `main`;
   - **Root directory:** leave empty;
   - **Language / runtime:** Docker (Render detects the root `Dockerfile`);
   - **Instance type:** Free.
3. **Environment variables.** Service → **Environment** → **Add from .env** lets you paste them all at once. For the database, use **one** of the two options below.
   - **Option A (simplest): paste Neon's connection string as is.** No conversion is needed: the app turns it into JDBC itself, drops `channel_binding`, and handles a pooled (`-pooler`) host with `prepareThreshold=0`. The direct (non-pooled) string is still the best choice.
     ```
     DATABASE_URL=postgresql://<user>:<password>@<host>/<db>?sslmode=require&channel_binding=require
     ```
   - **Option B: JDBC variables** (e.g. the first three lines of `.env.neon`):
     ```
     SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/<db>?sslmode=require
     SPRING_DATASOURCE_USERNAME=<user>
     SPRING_DATASOURCE_PASSWORD=<password>
     ```
   If both are set, `SPRING_DATASOURCE_URL` wins. The rest of the variables are the same either way:
   ```
   SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=0
   SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=60000
   SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=240000
   SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5
   JPA_DDL_AUTO=validate
   LEGACY_AES_SECRET=<SECRET_KEY constant from the old AES256.java>
   LEGACY_AES_SALT=<SALT constant from the old AES256.java>
   APP_AUTH_UPGRADE_HASH=false
   APP_BASE_URL=https://evesdb.onrender.com
   SESSION_COOKIE_SECURE=true
   MAIL_ENABLED=false
   RECAPTCHA_ENABLED=false
   GOOGLE_MAPS_API_KEY=
   ```
   The four `HIKARI` lines close idle database connections after a minute, so Neon can suspend and the free 100 compute-hours last the month.
4. **Advanced** → **Health Check Path** `/actuator/health`; **Auto-Deploy** Yes.
5. **Create Web Service** and watch **Logs**:
   - the build runs npm and Maven inside Render (~5–10 min);
   - startup ends with `Started SibolEvesApplication`. On 0.1 CPU the first start can take a few minutes.

### B4. Check it
Open `https://evesdb.onrender.com`:
- `/actuator/health` returns `{"status":"UP"}`;
- log in with an existing account;
- **Database** shows 99 studies (pending + approved);
- Export downloads an `.xlsx`; the Map tab lists the sites (the map itself needs `GOOGLE_MAPS_API_KEY`).

### B5. Custom domain (optional)
1. Render → service → **Settings → Custom Domains → Add Custom Domain** → `eves.<your-domain>`.
2. At your registrar: a **CNAME** record `eves` → `evesdb.onrender.com`. Render issues the TLS certificate automatically.
3. Change `APP_BASE_URL` to `https://eves.<your-domain>`; saving the variable redeploys the service.

### B6. Troubleshooting
| Symptom in the logs | Fix |
|---|---|
| `Missing required configuration: SPRING_DATASOURCE_URL … or DATABASE_URL …` or `Connection to localhost:5432 refused` | No database URL reached the Render service. Service → **Environment** → add `DATABASE_URL` (Neon's string, Option A) **or** `SPRING_DATASOURCE_URL` (JDBC, Option B) → **Save, rebuild, and deploy**. Names must match exactly, with no quotes around values. If other names (e.g. `LEGACY_AES_SECRET`) are also listed, those are missing too. |
| `Schema-validation: missing table …` | The restore into Neon didn't complete. Re-run step B1.4 and check the count. |
| `FATAL: password authentication failed` / `SSL` errors | Check the user and password variables and that the URL has `?sslmode=require` but **no** `channel_binding`. |
| `prepared statement "S_1" already exists` | A *pooled* Neon host (`-pooler`) was given as a JDBC `SPRING_DATASOURCE_URL`. Use the direct host, add `&prepareThreshold=0`, or switch to Option A (`DATABASE_URL`), which handles it. |
| `OutOfMemoryError` / instance restarts | Add `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1`. |
| Deploy times out before the app starts | The free 0.1 CPU is slow to start Spring Boot. Redeploy once; if it keeps failing, the paid Starter instance (more CPU) is the fix. |
| `Mail failed … no password specified?` / `Mail: SMTP_USERNAME and/or SMTP_PASSWORD are not set` | `SMTP_USERNAME`/`SMTP_PASSWORD` didn't reach the service. Set them to Brevo's SMTP **login** (`…@smtp-brevo.com`) and SMTP **key** (`xsmtpsib-…`), not the account password or API key. |
| `535 Authentication failed` | Wrong Brevo login or key; generate a new SMTP key and paste it again. |
| `550`/`sender … not valid` | `MAIL_FROM` isn't a verified Brevo sender (B7.A step 2-3). |
| `Mail failed … connect timed out` / "The e-mail could not be sent" | Render's free plan blocks SMTP ports 25, 465 and 587. Use `SMTP_PORT=2525` with Brevo (B7.A). |
| Map tab says "daily limit" | The app-side `MAPS_DAILY_LIMIT` was reached (B7.C). It resets at midnight Pacific time or when the app restarts. |
| Site slow on first visit | Normal on the free plan: it sleeps after 15 min without visitors and takes ~1 min to wake. |

### B7. E-mail, reCAPTCHA and Google Maps
All three are built in; they only need accounts and keys. Add the variables under **Environment**, then **Save, rebuild, and deploy**. Never commit the keys.

**A. E-mail (Brevo, free 300 mails/day, sent from your domain).** Render's free plan blocks the usual SMTP ports (25/465/587), so Gmail SMTP can't work there; Brevo also listens on **2525**.
1. Sign up at https://www.brevo.com (free plan). New accounts may need a short activation review before they can send.
2. **Senders, Domains & Dedicated IPs → Domains → Add a domain**: enter your domain, add the DNS records Brevo shows (Brevo code TXT, DKIM, DMARC) at your domain registrar, then click **Authenticate**. DNS changes can take up to a few hours.
3. **Senders → Add a sender**, e.g. `no-reply@<yourdomain>`.
4. **SMTP & API → SMTP → Generate a new SMTP key.** Note the **Login** shown there (`…@smtp-brevo.com`) and the key; the key is shown once.
5. Render variables:
   ```
   MAIL_ENABLED=true
   SMTP_HOST=smtp-relay.brevo.com
   SMTP_PORT=2525
   SMTP_USERNAME=<Brevo SMTP login>
   SMTP_PASSWORD=<Brevo SMTP key>
   MAIL_FROM=no-reply@<yourdomain>
   ```
   `SMTP_USER` and `SMTP_KEY` are accepted as alternative names for `SMTP_USERNAME` and `SMTP_PASSWORD`.
6. Contact-form messages go to the users whose contact categories are set to accepted (User admin). Make sure at least one admin has them.

**B. reCAPTCHA v2 ("I'm not a robot").**
1. https://www.google.com/recaptcha/admin/create → type **Challenge (v2) → "I'm not a robot" Checkbox**.
2. Domains: `evesdb.onrender.com`, your own domain (if any), `localhost`. Submit and copy the **site key** and **secret key**.
3. Render variables: `RECAPTCHA_ENABLED=true`, `RECAPTCHA_SITE_KEY=<site key>`, `RECAPTCHA_SECRET=<secret key>`.
4. Don't reuse the keys from the old ZK code; create new ones.

**C. Google Maps, with a safeguard against charges.** Google requires a billing account (card), but normal use stays inside the free monthly allowance (about 10,000 map loads). Two limits keep it there:
1. https://console.cloud.google.com → create project `sibol-eves` → **Billing**: link a billing account.
2. **APIs & Services → Library**: enable **Maps JavaScript API** (only this one).
3. **Google Maps Platform → Keys & Credentials → Create API key**, then restrict it:
   - Application restriction **Websites**: `https://evesdb.onrender.com/*`, `https://<yourdomain>/*`, `http://localhost:8088/*`;
   - API restriction: **Maps JavaScript API**.
4. **Hard cap (Google side):** **Google Maps Platform → Quotas → Maps JavaScript API → Map loads per day → 300** (≈ 9,000/month, under the free allowance). Over the cap, Google refuses the map instead of billing.
5. **Alert:** **Billing → Budgets & alerts → Create budget** of $1, alerts at 50/90/100 %. (A budget only warns; step 4 is the stop.)
6. Render variables: `GOOGLE_MAPS_API_KEY=<key>` and `MAPS_DAILY_LIMIT=250`. The app counts map loads per day and, past 250, shows the site list with "Open in Google Maps" links instead of Google's error page. This counter restarts when the app restarts, so the Google quota stays the real limit.

## C. After go-live (either platform)
- **Smoke test:** landing page, login with an existing account, search count equals the source DB, Excel export, map tab, `/actuator/health` returns `{"status":"UP"}`.
- **Uptime monitoring:** free UptimeRobot check on `/actuator/health`.
- **Backups:** free tiers have no managed backups, so dump weekly from your PC:
  ```bash
  pg_dump "<connection string>" -Fc --no-owner --no-acl > evesdb-$(date +%F).dump
  ```
- **Updates:** push to `main` and the platform rebuilds automatically (enable auto-deploy in the service settings).

---
Sources (checked 2026-11-17):
- [Northflank pricing](https://northflank.com/pricing)
- [Render free tier docs](https://render.com/docs/free)
- [Neon scale to zero](https://neon.com/docs/introduction/scale-to-zero)
- [Neon pricing](https://neon.com/pricing)
