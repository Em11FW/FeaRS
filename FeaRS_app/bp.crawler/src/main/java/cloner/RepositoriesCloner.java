package cloner;

import db.PsqlDB;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RepositoriesCloner {
    private String repoClonePath;
    private int numberNewRepositories = 0;
    private int numberUpdatedRepositories = 0;

    public RepositoriesCloner(String path){
        this.repoClonePath = path;
    }

    public int getNumberNewRepositories() {
        return numberNewRepositories;
    }

    public int getNumberUpdatedRepositories() {
        return numberUpdatedRepositories;
    }

    private String getLatestCommitSHA(Git git){
        ObjectId latestCommit;
        try{
            latestCommit = git.getRepository().resolve(Constants.HEAD);
        }catch(Exception e){
            System.err.println("Cannot get latest commit SHA.");
            return null;
        }
        return latestCommit.name();
    }

    private void gitClone(String repoFullName){
        Path destinationPath = Paths.get(repoClonePath, repoFullName);
        if(!Files.exists(destinationPath)){
            try{
                Files.createDirectories(destinationPath);
            }catch(IOException e){
                System.err.println("Cannot create directory "+destinationPath.toString());
                e.printStackTrace();
            }
        }
        String defaultBranch = PsqlDB.getInstance().getDefaultBranch(repoFullName);
        try{
            Git git = Git.cloneRepository()
                    .setURI("https://github.com/" + repoFullName + ".git")
                    .setDirectory(destinationPath.toFile())
                    .setBranch(defaultBranch)
                    .call();
            String commitSHA = getLatestCommitSHA(git);
            PsqlDB.getInstance().updateRepositoryLatestCommit(repoFullName, commitSHA);
            git.close();
            ++numberNewRepositories;
        }catch(GitAPIException e){
            System.err.println("Cannot clone "+repoFullName);
            e.printStackTrace();
        }
    }

    private void gitPull(String repoFullName){
        Path repositoryPath = Paths.get(repoClonePath, repoFullName);
        try{
            Git git = Git.open(repositoryPath.toFile());
            git.pull().call();
            String commitSHA = getLatestCommitSHA(git);
            PsqlDB.getInstance().updateRepositoryLatestCommit(repoFullName, commitSHA);
            git.close();
            ++numberUpdatedRepositories;
        }catch(RepositoryNotFoundException e){
            System.err.println("Remote repository "+repoFullName+" not found.");
            PsqlDB.getInstance().deleteRepository(repoFullName);
        }catch(Exception e){
            System.err.println("Cannot pull repository "+repoFullName);
            e.printStackTrace();
        }
    }

    public void updateRepositories(){
        ResultSet repositoriesToUpdate = PsqlDB.getInstance().getRepositoriesToUpdate();
        if(repositoriesToUpdate == null){
            System.out.println("Up to date.");
            return;
        }
        try{
            int numberOfRepositoriesToUpdate = PsqlDB.getInstance().getNumberOfRepositoriesToUpdate();
            int count = 0;
            while ( repositoriesToUpdate.next() ) {
                count++;
                System.out.format("Cloned/Pulled repositories :\t %s/%s\n", count, numberOfRepositoriesToUpdate);
                String fullName = repositoriesToUpdate.getString(1);
                if(Files.exists(Paths.get(repoClonePath,fullName))){
                    System.out.format("Pulling repository %s\n",fullName);
                    gitPull(fullName);
                }else{
                    System.out.format("Cloning repository %s\n", fullName);
                    gitClone(fullName);
                }
            }
            repositoriesToUpdate.close();
            System.out.format("Cloned/Pulled repositories :\t %s/%s\n", count, numberOfRepositoriesToUpdate);
        }catch(SQLException e){
            System.err.println("Cannot update cloned repositories.");
            e.printStackTrace();
        }
    }
}
