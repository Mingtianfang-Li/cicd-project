# Useful Commands for the Project
**Version:** `1.0`  
**Last Updated:** `March 2025`  
**Author:** Yiwen

## Project Commands
### Building the project
- To build the project, you can run the following command:
```bash
./gradlew clean build
```
- or build without running tests:
```bash
./gradlew clean build -x test
```

### Running the modules
- After clean build with the whole project, you can run the modules by running the following command:
#### Running the CLI module
```bash
./gradlew :cli:run
```
#### Running the Backend module
```bash
./gradlew :backend:run
``` 
#### Running the Worker module
```bash
./gradlew :worker:run
```
#### No need to run the database module, as it is a PostgreSQL database inside the backend

#### No need to run common module, as it is a shared module for all other modules

### CLI Commands
- See [CICD_User_Guide](#) for more details

## Gradle Commands

### Basic Commands
1. Lists Available Tasks in the Project
```bash
./gradlew tasks
```

2. Displays the Gradle Version
```bash
./gradlew -v
```

3. Displays Gradle help.
```bash
./gradlew help
```

### Build and Run
1. Compiles and builds the project
```bash
./gradlew build
```

2. Deletes the build directory
```bash
./gradlew clean
```

3. Assembles outputs without running tests
```bash
./gradlew assemble
```

4. Runs a Spring Boot application (if applicable)
```bash
./gradlew bootRun
```

### Dependency Management
1. Displays the project’s dependency tree
```bash
./gradlew dependencies
```

2. Shows details about a specific dependency
```bash
./gradlew dependencyInsight --dependency <dependency-name>
```

3. Forces dependency refresh
```bash
./gradlew build --refresh-dependencies
```

### Testing
1. Runs unit tests
```bash
./gradlew test
```

2. Runs tests and other verification tasks
```bash
./gradlew check
```

3. Generates a JaCoCo test coverage report
```bash
./gradlew jacocoTestReport
```

### Multi-module Projects
1. Runs a task for a specific module
```bash
./gradlew :module-name:task-name
```

2. Lists all subprojects in a multi-module build
```bash
./gradlew projects
```

### Running and Debugging
1. Runs a Java application (if `application` plugin is applied)
```bash
./gradlew run
```

2. Enables debugging mode
```bash
./gradlew -Dorg.gradle.debug=true
```

3. Builds the project while skipping tests
```bash
./gradlew -x test build
```

### Performance Optimization
1. Runs tasks in parallel for better performance
```bash
./gradlew --parallel
```

2. Runs Gradle in daemon mode for faster builds
```bash
./gradlew --daemon
```

3. Disables daemon mode
```bash
./gradlew --no-daemon
```

### Wrapper
1. Generates `gradlew` and `gradlew.bat` scripts
```bash
./gradlew wrapper
```

2. Runs Gradle using the wrapper
```bash
./gradlew <task>
```

3. Updates the Gradle wrapper to a specific version
```bash
./gradlew wrapper --gradle-version <version>
```

## PostgreSQL Commands

### Basic Commands
1. Connect to PostgreSQL:
```bash
psql -U <username> -d <database_name>
```
- for our project, the default username is `postgres` and the default database name is `cicd_db`
```bash
psql -U postgres -d cicd_db -h localhost -p 5432
```

2. (inside the connection) List all databases:
```bash
\l
```

3. (inside the connection) List all tables in the current database:
```bash
\dt
```

4. (inside the connection) Describe a specific table:
```bash
\d <table_name>
```

5. (inside the connection) Exit PostgreSQL:
```bash
\q
```

### User and Database Management (inside the connection)
1. Create a new database:
```bash
CREATE DATABASE <database_name>;
```

2. Create a new user:
```bash
CREATE USER <username> WITH PASSWORD '<password>';
```

3. Grant privileges to a user:
```bash
GRANT ALL PRIVILEGES ON DATABASE <database_name> TO <username>;
```

4. Drop a database:
```bash
DROP DATABASE <database_name>;
```

5. Drop a user:
```bash
DROP USER <username>;
```

### Data Manipulation (inside the connection)
1. Insert data into a table:
```bash
INSERT INTO <table_name> (column1, column2) VALUES ('value1', 'value2');
```

2. Update data in a table:
```bash
UPDATE <table_name> SET column1 = 'new_value' WHERE condition;
```

3. Delete data from a table:
```bash
DELETE FROM <table_name> WHERE condition;
```

### Backup and Restore
1. Backup a database:
```bash
pg_dump -U <username> -d <database_name> -f backup.sql
```

2. Restore a database:
```bash
psql -U <username> -d <database_name> -f backup.sql
```

3. Backup entire PostgreSQL instance:
```bash
pg_dumpall -U <username> -f full_backup.sql
```

4. Restore entire PostgreSQL instance:
```bash
psql -U <username> -f full_backup.sql
```

