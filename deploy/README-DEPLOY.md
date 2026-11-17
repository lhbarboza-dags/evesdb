# Deploying SIBOL-EVES to Oracle Cloud (Always Free) – runbook

The target is **one free ARM VM** on Oracle Cloud Infrastructure (OCI) running this Docker Compose stack:

```
Internet ──443/80──▶ caddy (HTTPS, Let's Encrypt)
                        └─▶ frontend (nginx: React SPA + /api proxy) ──▶ backend (Spring Boot) ──▶ db (PostgreSQL 17)
nightly: deploy/backup.sh → /opt/sibol-eves/backups (14 days) → OCI Object Storage bucket
```

**Cost:** $0/month inside the OCI Always Free limits. The domain is paid separately. Limits, verified against Oracle's docs on 2026-11-17:
- Ampere A1: 1,500 OCPU-hours + 9,000 GB-hours per month, i.e. **2 OCPU / 12 GB**. This is the maximum free size, so **do not create a larger A1 instance**; larger ones are reportedly terminated since 2026-08-18.
- 200 GB block storage (boot + data), 20 GB object storage, 10 TB/month outbound traffic.
- All Always Free resources must be in your **home region**.

**Files in `deploy/`:**

| File | Purpose |
|---|---|
| `compose.prod.yml` | Production stack. Only Caddy publishes ports; Postgres is internal. |
| `Caddyfile` | Automatic HTTPS for `$DOMAIN`, security headers, `www` → apex redirect, hides `/actuator/*` except health. |
| `prod.sh` | Wrapper: `docker compose --env-file ../.env.prod -f compose.prod.yml …`. |
| `backup.sh` | `pg_dump` → local folder (14 days) → optional upload to OCI Object Storage. |
| `restore-test.sh` | Restores the newest dump into a throwaway container and prints row counts. |
| `../.env.prod.example` | Every production setting. Copy it to `../.env.prod` on the server and never commit that copy. |

---

## 1. Decide which database is the master (before anything else)
The legacy Heroku app (`sibol-eves.herokuapp.com`) no longer exists, and Heroku deletes app databases with the app. The local `evesdb` (12 MB, last study coded and reviewed 2024-12-12) is therefore the best known copy.

1. Ask the team whether anyone holds a newer dump.
2. Freeze edits in the old environment.
3. Dump the master:
   ```bash
   pg_dump -h <host> -U postgres -Fc --no-owner --no-acl evesdb > evesdb-$(date +%Y%m%d).dump
   ```

## 2. Create the Oracle Cloud resources (one time)
Use the **menus** (☰ at the top left), not bookmarked console links. Links only work once the account is fully set up and the right region and compartment are selected, so otherwise they bounce you to other pages. Oracle renames menu items now and then; if a label differs, look for the closest match.

### 2.0 Sign up and get ready
1. Sign up at https://signup.cloud.oracle.com/ (a card is needed for identity verification; Always Free use is not charged).
   - **Home region: Singapore** (`ap-singapore-1`), or Tokyo. It **cannot be changed later**, and Always Free servers can only be created there.
2. **Wait for the e-mail saying your account is ready.** Provisioning can take from minutes to a few hours, and until then most console pages redirect.
3. Sign in at https://cloud.oracle.com/ with your **Cloud Account Name** (tenancy name), then your user and password.
4. Every time, check two things:
   - **top right:** the region shows your home region (Singapore);
   - **on list pages, the left "Compartment" picker:** the **root** compartment (your tenancy name) is selected.
5. Recommended:
   - **Upgrade to Pay As You Go** (☰ → Billing & Cost Management → *Upgrade and Manage Payment*). Always Free resources stay free. In 2023 Oracle told users that PAYG accounts are not reclaimed when idle, but the current docs **do not confirm** this, so treat it as likely, not guaranteed. It also tends to reduce *"Out of capacity"* errors for ARM servers.
   - **Budget alert:** ☰ → Billing & Cost Management → **Budgets** → Create budget → $1, with an e-mail alert.

### 2.1 Create the server *and* its network in one go
☰ → **Compute** → **Instances** → **Create instance**:
- **Name:** `sibol-eves`.
- **Image and shape** → *Edit*:
  - *Change image* → **Ubuntu** → **Canonical Ubuntu 24.04** (the aarch64 build is picked automatically for Ampere).
  - *Change shape* → **Ampere** → **VM.Standard.A1.Flex** → **2 OCPUs, 12 GB memory** (no more).
- **Networking** → choose **"Create new virtual cloud network"** and **"Create new public subnet"**, and keep **"Assign a public IPv4 address"** on. You do **not** need the separate VCN wizard.
- **Add SSH keys** → **Generate a key pair for me** → *Save private key* (keep that file safe), or upload your own public key.
- **Boot volume** → *Specify a custom boot volume size* → **100 GB**.
- Click **Create**. When it's RUNNING, note the **public IP** on the instance page.

### 2.2 Make the public IP permanent (free)
Instance page → **Attached VNICs** (or the *Networking* tab) → click the VNIC → **IPv4 addresses** → ⋮ → **Edit** → **Reserved public IP** → *Create a new reserved IP* → Update. Without this the IP can change after a stop/start.

### 2.3 Open web ports 80 and 443
Instance page → click the **Subnet** link → **Security Lists** (or the *Security* tab) → **Default Security List for …** → **Add Ingress Rules** (twice):
- Source CIDR `0.0.0.0/0`, IP protocol **TCP**, destination port range **80**
- Source CIDR `0.0.0.0/0`, IP protocol **TCP**, destination port range **443**

SSH (22) is already allowed. Later you can edit that rule to your own IP (`x.x.x.x/32`).

### 2.4 Backup bucket
- ☰ → **Storage** → **Buckets** (under Object Storage) → **Create bucket** `sibol-eves-backups` (keep it private).
- Open the bucket → **Lifecycle policy rules** → create: *Delete* objects older than **30 days**.
- To let the server upload without stored keys: ☰ → **Identity & Security** → **Domains** → Default → **Dynamic groups** → create `sibol-eves-vm` with rule `instance.id = '<your instance OCID>'`.
- Then ☰ → Identity & Security → **Policies** → create in the root compartment: `Allow dynamic-group sibol-eves-vm to manage objects in tenancy where target.bucket.name='sibol-eves-backups'`

### 2.5 DNS
At your domain registrar, create an **`A` record** for `eves.<your-domain>` (or the apex) pointing to the **reserved IP**, with TTL 300.

### Troubleshooting
- **"Out of capacity for shape VM.Standard.A1.Flex"**: common for free ARM servers. Try another **Availability Domain** in the *Placement* section of Create instance, retry later (capacity is freed during the day), or upgrade to PAYG.
- **Pages redirect or lists are empty**:
  - the account isn't ready yet (wait for the e-mail);
  - you're in the wrong region (top right);
  - the root compartment isn't selected (left picker).
- **Sign-in loops**: use the *Cloud Account Name* from the welcome e-mail on the first sign-in screen.

## 3. Prepare the VM (one time)
```bash
ssh ubuntu@<ip>
sudo apt update && sudo apt -y full-upgrade && sudo apt -y install unattended-upgrades git
sudo dpkg-reconfigure -f noninteractive unattended-upgrades
# Ubuntu images on OCI ship restrictive iptables rules: open 80/443 in the host firewall too
sudo iptables -I INPUT 6 -p tcp --dport 80 -j ACCEPT && sudo iptables -I INPUT 6 -p tcp --dport 443 -j ACCEPT && sudo iptables -I INPUT 6 -p udp --dport 443 -j ACCEPT
sudo netfilter-persistent save
# SSH: keys only
sudo sed -i 's/^#\?PasswordAuthentication .*/PasswordAuthentication no/' /etc/ssh/sshd_config && sudo systemctl reload ssh
# Docker Engine + compose plugin
curl -fsSL https://get.docker.com | sudo sh && sudo systemctl enable --now docker && sudo usermod -aG docker ubuntu && newgrp docker
# 2 GB swap (headroom for the Maven/npm image builds)
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile && echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
# OCI CLI for backups (uses the instance principal)
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)" -- --accept-all-defaults
echo 'export OCI_CLI_AUTH=instance_principal' >> ~/.bashrc
```

## 4. Deploy
```bash
sudo mkdir -p /opt/sibol-eves && sudo chown ubuntu: /opt/sibol-eves
git clone <bitbucket-repo-url> /opt/sibol-eves        # use a read-only access key (Repository settings → Access keys)
cd /opt/sibol-eves && git checkout master              # or the release branch / tag
cp SIBOL-EVES-NEXT/.env.prod.example SIBOL-EVES-NEXT/.env.prod && chmod 600 SIBOL-EVES-NEXT/.env.prod
nano SIBOL-EVES-NEXT/.env.prod                         # DOMAIN, passwords, AES values, NEW Gmail app password, reCAPTCHA + Maps keys
```
Before launch, create **new** credentials. The old Gmail app password, reCAPTCHA secret and Maps key are in the old repo's git history.
- **reCAPTCHA v2 checkbox:** register keys for your domain at google.com/recaptcha/admin.
- **Google Maps JavaScript API:** restrict the key to the HTTP referrer `https://<domain>/*`.

Load the data, then start everything:
```bash
cd /opt/sibol-eves/SIBOL-EVES-NEXT/deploy
./prod.sh up -d db                                     # empty database
scp evesdb-YYYYMMDD.dump ubuntu@<ip>:/tmp/              # (from your PC)
./prod.sh exec -T db sh -c 'pg_restore -U "$POSTGRES_USER" -d evesdb --no-owner --no-acl' < /tmp/evesdb-YYYYMMDD.dump
./prod.sh up -d --build                                # first ARM build ~5–10 min; Caddy gets the certificate once DNS resolves
./prod.sh ps                                           # all services running, backend (healthy)
```

Then set up backups and monitoring:
```bash
./backup.sh && ./restore-test.sh                       # first backup + restore check (counts must match production)
( crontab -l 2>/dev/null; echo '30 2 * * * OCI_CLI_AUTH=instance_principal /opt/sibol-eves/SIBOL-EVES-NEXT/deploy/backup.sh >> /home/ubuntu/sibol-backup.log 2>&1';
  echo '0 4 * * 0 /opt/sibol-eves/SIBOL-EVES-NEXT/deploy/restore-test.sh >> /home/ubuntu/sibol-backup.log 2>&1' ) | crontab -
```
For monitoring, use free UptimeRobot (or Better Stack): an HTTP keyword check on `https://<domain>/actuator/health` for `UP` every 5 min, with e-mail alerts.

## Keep it running (idle-stop risk)
Oracle may **stop** an Always Free server it considers idle: over 7 days, 95th-percentile CPU, network *and* (for A1) memory all under 20 %. A quiet research site can meet that. A stop is not a deletion: the disk, database and settings stay.
- **Get alerted:** the UptimeRobot check (section 4) e-mails you when the site is down.
- **Bring it back:** ☰ → Compute → Instances → `sibol-eves` → **Start**. Docker is enabled on boot, and every container has `restart: unless-stopped`, so the site comes back by itself within ~1–2 minutes.
- **Reduce the chance:** upgrade to Pay As You Go (see 2.0; likely but not guaranteed to exempt you).
- **Nightly off-server backups** (section 4) protect the data in any case.
- Don't run fake-load scripts to dodge the idle check; it isn't needed, and it goes against the free tier's terms of use.

## 5. Updates (CI/CD)
`bitbucket-pipelines.yml` runs on every push: backend tests, frontend tests + build, and the migration coverage check. On `master` it then SSHes to the VM, runs `git reset --hard origin/master` and `deploy/prod.sh up -d --build`.

One-time Bitbucket setup:
- Repository settings → Pipelines → SSH keys: generate a key and add the VM as a known host. Append the public key to `~ubuntu/.ssh/authorized_keys`.
- Repository settings → Deployments → Production: set the variables `DEPLOY_HOST` and `DEPLOY_USER`.

Because SSH (port 22) is allowed only from your admin IP, the deploy step from Bitbucket needs the port opened to Bitbucket's IP ranges. If you prefer to keep SSH closed, deploy by hand instead:
`ssh ubuntu@<ip> 'cd /opt/sibol-eves && git pull && SIBOL-EVES-NEXT/deploy/prod.sh up -d --build'`

## 6. Go-live checklist
- [ ] `curl -I https://<domain>` returns 200, a valid certificate and a `strict-transport-security` header.
- [ ] `https://<domain>/actuator/health` returns `{"status":"UP"}`; `/actuator/env` and other actuator paths return 404.
- [ ] Login with an existing account works (legacy password format).
- [ ] The search count equals `select count(*) from studysrc where isdeleted=false and status in (0,1)` on the source DB.
- [ ] Excel export downloads; the Map tab shows markers.
- [ ] Registering a test account delivers a **real** verification e-mail whose link opens `https://<domain>/verify-email?...`.
- [ ] Forgot password and Contact Us show reCAPTCHA and work.
- [ ] Only 22 (admin IP), 80 and 443 are open (`nmap <ip>`); Postgres is not reachable from outside.
- [ ] The session cookie is `Secure; HttpOnly; SameSite=Lax` (browser DevTools → Application → Cookies).
- [ ] A backup is in the bucket, and `restore-test.sh` counts match.
- [ ] Work through `docs/migration/EVENTS.md` on the live site.
- [ ] Later, once the old ZK app is retired for good: set `APP_AUTH_UPGRADE_HASH=true` in `.env.prod` and run `./prod.sh up -d`.

## Fallback if OCI signup or ARM capacity fails
Google Cloud Run (free tier, `--max-instances=1` so in-memory sessions keep working) for the backend, plus Neon free Postgres (1 GB per project). The frontend would need a static host with an `/api` rewrite to Cloud Run (e.g. Firebase Hosting) so it stays same-origin. Trade-off: 10–20 s cold starts after idle periods.

---
Sources (checked 2026-11-17):
- [Oracle – Always Free Resources](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm)
- [Neon – Free plan 1 GB per project](https://neon.com/blog/neon-free-plan-1-gb-per-project)
