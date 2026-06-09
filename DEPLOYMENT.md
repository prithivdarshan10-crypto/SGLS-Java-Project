# SGLS — GitHub + Render Deployment Guide

---

## PART 1: Push to GitHub

### Step 1 — Configure Git (one-time setup)
```bash
git config --global user.name "Priyadarshan S V"
git config --global user.email "prithivdarshan10@gmail.com"
```

### Step 2 — Create the GitHub repo
1. Go to https://github.com/new
2. Name: `smart-global-logistics`
3. Set to Public
4. DO NOT check "Add README" — we already have one
5. Click Create repository

### Step 3 — Initialize and push
```bash
# Navigate into your project folder
cd path/to/smart-global-logistics

git init
git add .
git commit -m "feat: Module 1 - Authentication & JWT Security"
git remote add origin https://github.com/prithivdarshan10-crypto/smart-global-logistics.git
git branch -M main
git push -u origin main
```

---

## PART 2: Deploy on Render

### Step 1 — Free MySQL via PlanetScale
1. Go to https://planetscale.com and sign up free
2. New Database → name: `sgls_db`, region: ap-south-1
3. Connect → Java/JDBC → copy the connection string, username, password

### Step 2 — Deploy Java app on Render
1. Go to https://render.com → New Web Service
2. Connect your GitHub repo
3. Settings:

| Setting | Value |
|---------|-------|
| Runtime | Java |
| Build Command | `mvn clean package -DskipTests` |
| Start Command | `java -jar target/smart-global-logistics-1.0.0.jar` |
| Instance Type | Free |

### Step 3 — Environment Variables (Render → Environment tab)

| Key | Value |
|-----|-------|
| DB_URL | your PlanetScale JDBC URL |
| DB_USERNAME | your DB username |
| DB_PASSWORD | your DB password |
| JWT_SECRET | HnFvizkzMWougZug3MLhl/YwI+f5sVeN0bLYoew/ERg= |
| JWT_EXPIRATION | 86400000 |
| DDL_AUTO | update |
| PORT | 8080 |

### Step 4 — Test after deploy
```bash
curl -X POST https://YOUR-APP.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## PART 3: Professional GitHub Setup

Add Topics to your repo (⚙️ gear next to About):
`java spring-boot jwt mysql rest-api warehouse-management logistics`

Commit message format:
```
feat: add warehouse CRUD endpoints
fix: resolve JWT expiry bug
docs: update API documentation
```

---

## Common Errors

| Error | Fix |
|-------|-----|
| Access denied for user root | Wrong DB_PASSWORD in env vars |
| JWT signature does not match | JWT_SECRET env var mismatch |
| Table users does not exist | Set DDL_AUTO=update in env vars |
| Build fails: mvn not found | Set Runtime = Java in Render |
