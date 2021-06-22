# FeaRS App

This project involves two sub-projects: 

* the knowledge-base creator (`Crawler` and `Analyzer`) which can be run continuously using a helper project (`FeaRS`)

* the web service which can be set up to communicate between the plugin and knowledge base

We provide step-by-step instructions to run the project via Docker.


## How to setup and run Docker services

## Background
We provide three services:
- `fears-app`: the crawler and analyzer
- `fears-db`: A postgres image
- `fears-service`: the web service

## How to setup and run all services
### 1/2
1. `cd web_service`
2. Build the project: `./gradlew build`
   - Verify `build/libs/gs-rest-service-0.1.0.jar` exists.
3. Create `fears-service` image: `docker build -t fears-service:latest .`
   - The tailing `.` refers to `Dockerfile` file
4. Verify `fears-service` exists in the output of `docker image ls`

### 2/2
1. Run `./createJars.sh`   
    - Verify `./feaRS/build/libs/feaRS-1.0-SNAPSHOT.jar` is created
2. Goto `./feaRS/docker`
3. Run `docker-compose build`
4. For a clean start, delete `./feaRS/docker/db` folder
5. Run all services: `docker-compose up -d`

Enjoy!

## How to see database?
1. pgAdmin -> Create a server:
    - host: (the machine that docker is running)
    - port: `46000`
    - Maintaince db: `postgres`
    - Username: `POSTGRES_USER` at `./feaRS/docker/docker-compose.yaml`
    - Pass: `POSTGRES_PASSWORD` at `./feaRS/docker/docker-compose.yaml`
2. Database name: `POSTGRES_DB` at `./feaRS/docker/docker-compose.yaml`
