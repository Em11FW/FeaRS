package cloner;

import db.PsqlDB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args){
        String repositoriesDestinationPathname = args[0];
        String dburl = args[1];
        String dbname = args[2];
        String dbuser = args[3];
        String password = (args.length > 4) ? args[4] : "";

        PsqlDB.getInstance().connect(dburl, dbname,dbuser,password);

        File repositoriesDestinationDir = new File(repositoriesDestinationPathname);
        if(!repositoriesDestinationDir.exists()){
            repositoriesDestinationDir.mkdirs();
        }

        System.out.println("Cloning...");
        RepositoriesCloner cloner = new RepositoriesCloner(repositoriesDestinationPathname);
        cloner.updateRepositories();
        System.out.println("Cloning ended.");

        LocalDateTime latestRun = PsqlDB.getInstance().getLastRun();
        PsqlDB.getInstance().updateHistoryRecord(latestRun, cloner.getNumberNewRepositories(), cloner.getNumberUpdatedRepositories());
        PsqlDB.getInstance().disconnect();
    }
}
