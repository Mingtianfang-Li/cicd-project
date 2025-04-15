# **CI/CD Database Development Plan**

This plan outlines the **database setup, local & remote storage strategy, development workflow, and migration path** for the CI/CD system. It ensures a smooth transition from **local development** to a **cloud-hosted PostgreSQL database**.

---

## **🚀 Phase 1: Local Development Setup (Using PostgreSQL Locally)**

### **📍 Goal:**
Set up a **local PostgreSQL database** for development and testing, ensuring compatibility with the future cloud version.

### **🛠️ Step 1: Install PostgreSQL Locally**
#### **📍 MacOS (Homebrew)**
```sh
brew install postgresql
brew services start postgresql
```
#### **📍 Ubuntu/Debian**
```sh
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```
#### **📍 Windows**
Download and install from [PostgreSQL official website](https://www.postgresql.org/download/).

---

### **🛠️ Step 2: Create a Local PostgreSQL Database**
1. Open the **PostgreSQL shell (`psql`)**:
   ```sh
   psql -U postgres
   ```
2. Create a **new database**:
   ```sql
   CREATE DATABASE ci_local;
   ```
3. Create a **database user**:
   ```sql
   CREATE USER ci_user WITH PASSWORD 'password';
   ALTER ROLE ci_user SET client_encoding TO 'utf8';
   ALTER ROLE ci_user SET default_transaction_isolation TO 'read committed';
   ALTER ROLE ci_user SET timezone TO 'UTC';
   GRANT ALL PRIVILEGES ON DATABASE ci_local TO ci_user;
   ```

---

### **🛠️ Step 3: Define Local Database Schema**
Run this **SQL schema** in the local PostgreSQL database (`ci_local`):
```sql
-- Table for storing pipeline executions
CREATE TABLE IF NOT EXISTS pipeline_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_name TEXT NOT NULL,
    run_number SERIAL NOT NULL,
    commit_hash TEXT NOT NULL,
    status TEXT CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELED')),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP DEFAULT NULL
);

-- Table for storing stage executions
CREATE TABLE IF NOT EXISTS stage_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_execution_id UUID NOT NULL,
    stage_name TEXT NOT NULL,
    execution_order INT NOT NULL,
    status TEXT CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELED')),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (pipeline_execution_id) REFERENCES pipeline_executions(id) ON DELETE CASCADE
);

-- Table for storing job executions
CREATE TABLE IF NOT EXISTS job_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_execution_id UUID NOT NULL,
    job_name TEXT NOT NULL,
    status TEXT CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELED')),
    allows_failure BOOLEAN NOT NULL DEFAULT FALSE,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (stage_execution_id) REFERENCES stage_executions(id) ON DELETE CASCADE
);

-- Indexes for optimized query performance
CREATE INDEX idx_pipeline_executions ON pipeline_executions(pipeline_name, run_number);
CREATE INDEX idx_stage_executions ON stage_executions(pipeline_execution_id, execution_order);
CREATE INDEX idx_job_executions ON job_executions(stage_execution_id, job_name);
```

---

### **🛠️ Step 4: Configure Application to Use Local PostgreSQL**
Modify the application settings to connect to the local database:
```python
import psycopg2

LOCAL_DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "ci_local",
    "user": "ci_user",
    "password": "password"
}

def get_local_db_connection():
    return psycopg2.connect(**LOCAL_DB_CONFIG)
```

---

## **🚀 Phase 2: Cloud PostgreSQL Deployment**

### **📍 Goal:**
Set up a **remote PostgreSQL database in the cloud** for production use.

### **🛠️ Step 1: Choose a Cloud PostgreSQL Provider**
**Free options include:**
- **[Neon](https://neon.tech/)**
- **[Supabase](https://supabase.io/)**
- **[ElephantSQL](https://www.elephantsql.com/)**
- **[Render](https://render.com/)**

---

### **🛠️ Step 2: Create a Remote PostgreSQL Database**
1. Sign up for a free cloud PostgreSQL provider.
2. Create a new database (e.g., `cicd_main`).
3. Create a database user with access.

---

## **🚀 Phase 3: Implement Database Switching in CLI**
Modify CLI logic to **automatically choose** local or remote PostgreSQL based on `--local` flag.

```python
def get_db_connection(is_local):
    return get_local_db_connection() if is_local else get_remote_db_connection()

# Example CLI command
def fetch_pipeline_executions(is_local):
    conn = get_db_connection(is_local)
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM pipeline_executions")
    return cursor.fetchall()
```

---

## **🚀 Phase 4: CI/CD Integration**

### **📍 Goal:**
Ensure the **remote PostgreSQL** is used in CI/CD pipelines.

### **🛠️ Step 1: Store Database Credentials in Environment Variables**
```yaml
DATABASE_URL: "postgres://your_user:your_password@your-cloud-db.com:5432/cicd_main"
```

### **🛠️ Step 2: Automatically Migrate DB Schema in CI/CD**
Modify pipeline scripts to apply database migrations:
```sh
psql $DATABASE_URL < schema.sql
```

---

## **🎯 Final Summary**
| Phase | Task |
|-------|------|
| **Phase 1** | Set up local PostgreSQL for development |
| **Phase 2** | Deploy a cloud PostgreSQL database |
| **Phase 3** | Implement database switching (`--local` flag) |
| **Phase 4** | Integrate with CI/CD pipelines |

