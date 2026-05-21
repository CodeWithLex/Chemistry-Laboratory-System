# ChemLab System — Supabase Deployment Guide

This guide walks you through deploying the ChemLab System so that:
- The **Java desktop app** runs on any PC without needing a local database.
- The **Node.js web app** is accessible online for students from their homes.

---

## Step 1: Create a Free Supabase Project

1. Go to [https://supabase.com](https://supabase.com) and sign up for a free account.
2. Click **New Project**, give it a name (e.g. `chemlab-system`), and set a strong **Database Password**. Save this password — you'll need it.
3. Wait for the project to finish provisioning (~1-2 minutes).

---

## Step 2: Set Up the Database Schema

1. In the Supabase Dashboard, click **SQL Editor** in the left sidebar.
2. Click **New Query**.
3. Open the file `database/schema_postgres.sql` from this project.
4. Copy its entire contents and paste them into the SQL Editor.
5. Click **Run** (or press `Ctrl+Enter`).
6. You should see "Success. No rows returned." — the tables are now created.

---

## Step 3: Configure the Java Desktop Application

Open the following three Java files in your NetBeans editor:
- `src/chemlab_system/database/Connect_SQL.java`
- `src/chemlab_system/database/Connector_ChemSystem.java`
- `src/chemlab_system/util/DatabaseUtil.java`

> [!WARNING]
> **Important Host Note:** 
> Do **NOT** put `https://` in front of your host domain. The host should be the raw domain only.
> - **Incorrect**: `https://sqlfktgrqhthqalvfiai.supabase.co`
> - **Correct**: `sqlfktgrqhthqalvfiai.supabase.co`

### Example configuration:
```java
private static final String DB_HOST     = System.getProperty("supabase.host", "sqlfktgrqhthqalvfiai.supabase.co");
private static final String DB_PORT     = System.getProperty("supabase.port", "5432");
private static final String DB_NAME     = System.getProperty("supabase.dbname", "postgres");
private static final String DB_USER     = System.getProperty("supabase.user", "postgres");
private static final String DB_PASSWORD = System.getProperty("supabase.password", "361deathnote@27");
```

Then **Clean and Build** the project in NetBeans (`Run → Clean and Build Project`).
Copy the entire `dist/` directory (which contains the compiled `.jar` and the `lib/` folder) to any machine you wish to run the admin app on.

---

## Step 4: Run Node.js Web App Locally

To test the student portal locally on your computer:

1. Open a terminal / command prompt in the `web/` folder.
2. Install the node packages:
   ```bash
   npm install
   ```
3. Configure the environment values in the `web/.env` file. (We have pre-configured this with your details).
4. Start the server:
   ```bash
   npm start
   ```
   Or for hot-reload development:
   ```bash
   npm run dev
   ```
5. Open your browser to [http://localhost:3000](http://localhost:3000).

---

## Step 5: Host the Web App Online

The web app is optimized to run on any cloud platform supporting Node.js.

### Option A: Railway (Highly Recommended — Free tier available)

1. Go to [https://railway.app](https://railway.app) and sign in.
2. Click **New Project** → **Deploy from GitHub repo**.
3. Select your repository.
4. Go to project settings and set the **Root Directory** to `web/`.
5. Add the following **Environment Variables** in the Railway dashboard:
   ```
   SUPABASE_HOST  = sqlfktgrqhthqalvfiai.supabase.co
   SUPABASE_PORT  = 5432
   SUPABASE_DB    = postgres
   SUPABASE_USER  = postgres
   SUPABASE_PASS  = 361deathnote@27
   PORT           = 3000
   SESSION_SECRET = chemlab-super-secret-key-12345
   MAIL_HOST      = smtp.gmail.com
   MAIL_PORT      = 587
   MAIL_USERNAME  = lexmatondo2719@gmail.com
   MAIL_PASSWORD  = iirz chiv svjl fxjx
   MAIL_FROM      = lexmatondo2719@gmail.com
   MAIL_FROM_NAME = ChemLab System
   ADMIN_EMAIL    = lexmatondo@g.cjc.edu.ph
   ```
6. Railway will automatically build, deploy, and provide a public HTTPS domain.

### Option B: Render (Free Web Service)

1. Go to [https://render.com](https://render.com) and sign in.
2. Click **New** → **Web Service** → Connect your repository.
3. Set **Root Directory** to `web/`, and **Runtime** to `Node`.
4. Build Command: `npm install`
5. Start Command: `npm start`
6. Add the environment variables listed in Option A under the Environment tab.

---

## Step 6: Verify and Test

1. Open the student portal at your deployed URL or `http://localhost:3000`.
2. Login as a student group (e.g. username `groupa` / password `password123` or your configured user).
3. Check the interface, try requesting items, or managing group members.
