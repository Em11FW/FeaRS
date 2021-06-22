# Setup PSQL database
- Install postgresql : `brew install postgresql`
- Connect to default PSQL database : `psql -h localhost -p 5432 -U <USER> postgres`
	- the `postgres` in command is just the default db name that comes built-in with postgres
- Insert password if asked 
- Create new database : `CREATE DATABASE DB_NAME;` 
- Switch to new database : `\c DB_NAME;` 
- Run the attached script `psqlDB.sql` : `\i filepath/psqlDB.sql` 

# Add Your GitHub Access Token
- In `bp.crawler/src/main/java/crawler/HTTPUtility.java`, add valid GitHub access tokens to `tokens` array in the following format: `token <TOKEN>`.

# EXECUTION
- Run `crawler.Main.main()` with arguments

| Index | Args | Notes |
|-------|------|-------| 
| 0 | `databaseurl`  | Example `jdbc:postgresql://localhost:5432/` |
| 1 | `databasename` | Name of the PSQL database we initialized before |
| 2 | `databaseuser` | Usually `postgres` |
| 3 | `databasepassword` | (Optional) Password of the PSQL database |

- Run `cloner.Main.main()` with arguments

| Index | Args | Notes |
|-------|------|-------| 
| 0 | `../path/to/destination/dir` | Path to cloned repositories destination directory | 
| 1 | `databaseurl`  | Example `jdbc:postgresql://localhost:5432/` |
| 2 | `databasename` | Name of the PSQL database we initialized before |
| 3 | `databaseuser` | Usually `postgres` |
| 4 | `databasepassword` | (Optional) Password of the PSQL database |

# Build .jar 
Move to `bp.crawler` folder
#### Windows
- cmd `gradlew clean`
- cmd `gradlew fatJar`
#### Mac
- cmd `./gradlew clean`
- cmd `./gradlew fatJar`

Find the jar file in project folder `bp.crawler/build/libs/`
