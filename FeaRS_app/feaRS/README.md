## Setup (Database)
- Create database
  - Install postgresql : `brew install postgresql`
  - Connect to default PSQL database : `psql -h localhost -p 5432 -U <USER> postgres`
    - the `postgres` in command is just the default db name that comes built-in with postgres
  - Insert password if asked 
  - Create new database : `CREATE DATABASE DB_NAME;` 
  - Switch to new database : `\c DB_NAME;` 
  - Run the attached script `InitPSQLDB.sql` : `\i docker/db_scripts/InitPSQLDB.sql` 

## Setup (Others)
- Copy attached directory `asia-resources` in path `/usr/local/app/resources/asia-resources` folder
- Install [srcML](https://www.srcml.org/#download) on the machine
- Install [R.app](https://cran.r-project.org/bin/macosx/) on the machine
  - you can install in Mac like `brew install r`
  - Install [JRI](https://www.rforge.net/JRI/) (which is now part of rJava):
    - Go to `R` shell (run `R` command in terminal)
    - run `install.packages('rJava')`
      - if there was an error that Java support is not installed, as it hints, run `R CMD javareconf` in terminal first (not in `R` shell)
    - Verify everything by running in `R` shell: 1. `library(rJava)` 2. `.jinit()`
  - Install `arules`: `R -e "install.packages('arules')"` or alternatively run the command within `R` shell
  

- Run SchedulerMain.main() with arguments

## Run Configuration
- Set VM options: `-Djava.library.path=/usr/local/lib/R/4.1/site-library/rJava/jri/`
  - How to find the path? Run `R` and run `.libPaths()` command. Inspect shown paths to find `rJava` folder` in one of them.
- Set environment variables: `R_HOME=/usr/local/Cellar/r/4.1.0/lib/R`
  - This path is the output of running `R RHOME` in terminal
  
| Index | Args | Notes |
|-------|--------|-------|
| 0 | `../path/to/dir/of/cloned/repos` | Path to the location of the cloned repositories |
| 1 | `../path/for/temp/xml/files`     | Temporary path to the location where temporary xml files are created |
| 2 | `../path/of/methods/files`       | Temporary path to the location where files containing methods bodies are created  |
| 3 | `../path/of/clustering/out`      | Temporary path to the location where output files of clustering process are created |
| 4 | `x`                              | (`int`) Number of threads used for process of methods similarity comparison |
| 5 | `x.xx`                           | (`double`) Threshold value for clustering process |
| 6 | `x`                              | Minimum amount of new methods necessary to trigger clustering process |
| 7 | `x.xx`                           | (`double`) Association rule support (e.g., 0.005)|
| 8 | `x.xx`                           | (`double`) Association rule confidence (e.g., 0.1)|
| 9 | `x`                              | (`int`) Association rule max length (e.g., 3)|
| 10 | `databaseurl`                   | Example : `jdbc:postgresql://localhost:5432/` |
| 11 | `databasename`                  | Name of the PSQL database |
| 12 | `databaseuser`                  | Usually `postgres` |
| 13 | `databasepassword`              | (Optional) Password of the PSQL database |

# Build .jar
Move inside `feaRS` folder
#### Windows 
- cmd `gradlew clean`
- cmd `gradlew fatJar`
#### Mac
- cmd `./gradlew clean`
- cmd `./gradlew fatJar`

Find jar file in folder `feaRS/build/libs/`
