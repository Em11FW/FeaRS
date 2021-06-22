# 1/2 Analyzer
# EXECUTION
- Run bp.crawler `crawler.Main.main()` and `cloner.Main.main()` 
- Download [srcML](https://www.srcml.org/#download)
- Extract srcML and move `bin` folder and `lib` folder to `/usr/local/fears-app/resources/srcml/` (if intermediate folders do not exist `mkdir -p`)
- Move ASIA project resources to `/usr/local/fears-app/resources/asia-resources` folder
- Run `analyser.Main.main()` 

#### analyser.Main.main() arguments array : 
| Index | Args | Notes |
|-------|--------|-------|
| 0 | `../path/to/dir/of/cloned/repos` | Path to the location of the cloned repositories |
| 1 | `../path/for/temp/xml/files`     | Path to the location where temporary xml files are created |
| 2 | `../path/of/methods/files`       | Path to the location where files containing methods bodies are created  |
| 3 | `../path/of/clustering/out/files/`| Path to the location where output files of clustering process are created. !! `/` at the end of the path is necessary!!|
| 4 | `x`                              | Number (Int) of threads used for process of methods similarity comparison |
| 5 | `x.xx`                           | Threshold value (Double) for clustering process |
| 6 | `x`                              | Minimum amount of new methods necessary to trigger clustering process |
| 7 | `databaseurl`                    | Example: `jdbc:postgresql://localhost:5432/` |
| 8 | `databasename`                   | Name of the PSQL database |
| 9 | `databaseuser`                  | Usually `postgres` |
| 10| `databasepassword`               | (Optional) Password of the PSQL database | 

---
# 2/2 Rule Extraction (arules)
## Add `arules` table to database
- Connect to database Psql using cmd `psql -h localhost -p 5432 -U postgres DB_NAME`
- Insert password if asked
- Run script `PsqlAddARules.sql` using `\i filepath/PsqlAddARules.sql` cmd
## R
- Make sure to have R.app or RStudio installed
    - you can install in Mac like `brew install r`
- Install [JRI](https://www.rforge.net/JRI/) (which is now part of rJava):
    - Go to `R` shell (run `R` command in terminal)
    - run `install.packages("rJava")`
        - if there was an error that Java support is not installed, as it hints, run `R CMD javareconf` in terminal first (not in `R` shell)
    - Verify everything by running in `R` shell: 1. `library(rJava)` 2. `.jinit()`
- Install arules by running `install.packages("arules")` in `R` shell
- Set VM options: `-Djava.library.path=/usr/local/lib/R/4.1/site-library/rJava/jri/`
    - How to find the path? Run `R` and run `.libPaths()` command. Inspect shown paths to find `rJava` folder` in one of them.
- Set environment variables: `R_HOME=/usr/local/Cellar/r/4.1.0/lib/R`
    - This path is the output of running `R RHOME` in terminal

## Run arule.ARuleMain.main() with arguments: 
|Index|Args|Notes|
|-----|----|-----|
| 0   | `x.xx`             | (`double`) Association rule support (e.g., 0.005)|
| 1   | `x.xx`             | (`double`) Association rule confidence (e.g., 0.1)|
| 2   | `x`                | (`int`) Association rule max length (e.g., 3)|
| 3   | `databaseurl`      | Example: `jdbc:postgresql://localhost:5432/` |
| 4   | `databasename`     | Name of PSQL database |
| 5   | `databaseuser`     | Usually `postgres` |
| 6   | `databasepassword` | (Optional) Password of PSQL database |

## Create jar 
Move inside `bp.analyser` folder
#### Windows
- cmd `gradlew clean`
- cmd `gradlew fatJar`
#### Mac
- cmd `./gradlew clean`
- cmd `./gradlew fatJar` 

Find the jar file in project folder `bp.anlyser/build/libs/`



## Trouble Shooting

Q. I get the following error, what's wrong?  `java.lang.UnsatisfiedLinkError: no jri in java.library.path`
A.
1. Verify `R_HOME` is set correctly:
2. JRI library must be in the current directory or any directory listed in java.library.path. Alternatively you can specify its path with
   `-Djava.library.path=` when starting the JVM. When you use the latter, make sure you check `java.library.path` property first such that you won't break your Java.