package analyser;

import com.google.common.collect.Iterables;
import git.GitUtility;
import analyser_db.PsqlDB;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import parser.RepositoryNewMethodsExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class LogAnalyser {
    private RepositoryNewMethodsExtractor parser;
    private String repositoriesPath;
    private String currentRepository = null;
    private GitUtility currentGit = null;

    private static final int MAX_NEW_METHOD_AT_A_COMMIT = 10;

    LogAnalyser(String repositoriesPath, String xmlFilesDestinationPath){
        this.repositoriesPath = repositoriesPath;
        this.parser = new RepositoryNewMethodsExtractor(repositoriesPath, xmlFilesDestinationPath);
    }

    /**
     * @return 0: successful, 1: if failed to parse (needs to be cloned), 2: repository does not meet our filtering criteria
     */
    private int loopOverCommits() {
        Iterable<RevCommit> commits = currentGit.gitLog();
        if(commits==null)
        {
            System.err.println("***********************************");
            System.err.println("Can't fetch commits from filesystem!");
            System.err.println("***********************************");
            return 1;
        }

        long nTotalCommits = Iterables.size(commits); // Part of these commits (up to `latestAnalysedCommit`)  are already analyzed before
        if (nTotalCommits < 100 || nTotalCommits >= 50000) {
            System.out.println("Skipping repo due to too low/high number of commits: "+nTotalCommits);
            return 2;
        }

        commits = currentGit.gitLog();
        String latestAnalysedCommit = PsqlDB.getInstance().getLatestAnalysedCommit(currentRepository);
        int counter = 1;
        for (RevCommit commit : commits) {
            if(commit.getParentCount()>0) {
                System.out.format("[%d/%d] analysing commit %s of repository %s \n", counter++,nTotalCommits, commit.getId().getName(),currentRepository);
                if (commit.getId().getName().equals(latestAnalysedCommit)) {
                    System.out.println("Reached previously analyzed commit : "+latestAnalysedCommit);
                    break;
                }
                try {
                    PsqlDB.getInstance().insertCommit(currentRepository, commit.getId().getName(), LocalDateTime.ofEpochSecond(commit.getCommitTime(), 0, ZoneOffset.UTC));
                    loopOverDiffs(commit);
                } catch (Exception e)
                {
                    System.err.println("ERROR: Failed analyzing commit: "+currentRepository+" > "+commit.getId().getName());
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private void loopOverDiffs(RevCommit commit) {
        List<DiffEntry> changedFiles = currentGit.gitDiff(commit);
        if(changedFiles==null) {
            System.out.print('\n');
            return;
        }

        if(changedFiles.size()>50) {
            System.out.print("** Skipping commits with more than 50 files *\n");
            return;
        }

        List<DiffEntry> desiredChangedFiles = new ArrayList<>();
        for (DiffEntry file : changedFiles) {
            if (file.getNewPath().endsWith(".java")) {
                if ((file.getChangeType() == DiffEntry.ChangeType.ADD) || (file.getChangeType() == DiffEntry.ChangeType.MODIFY)) {
                    desiredChangedFiles.add(file);
                }
            }
        }

        System.out.println("["+desiredChangedFiles.size()+"] ");
        List<RepositoryNewMethodsExtractor.ExtractedMethodInfo> allExtractedMethods = new ArrayList<>();
        for(DiffEntry file:desiredChangedFiles)
        {
            System.out.println("file: "+file);
            List<RepositoryNewMethodsExtractor.ExtractedMethodInfo> res = this.parser.extractMethodsFromFile(currentRepository,commit, file);
            if(res == null)
                continue;
            allExtractedMethods.addAll(res);
            // Only add methods if overall we have <=10 new methods at this "commit"
            if(allExtractedMethods.size() > MAX_NEW_METHOD_AT_A_COMMIT )
                break;
        }
        System.out.print('\n');

        if(allExtractedMethods.size() <= MAX_NEW_METHOD_AT_A_COMMIT )
        {
            Long commitId = PsqlDB.getInstance().getCommitId(currentRepository, commit.getId().getName());
            PsqlDB.getInstance().insertBatchOfMethods(commitId, allExtractedMethods);
        }


        try{ currentGit.gitCheckoutDefaultBranch();
        }catch(JGitInternalException | GitAPIException e){
            System.err.println("Cannot checkout to default-branch of repository "+currentRepository);
            e.printStackTrace();
        }
    }

    public void analyseChangeLog(){
        ResultSet repositoriesNames = PsqlDB.getInstance().getRepositoriesToAnalyse();
        if(repositoriesNames==null){ return; }
        try {
            int numberOfRepositoriesToAnalyse = PsqlDB.getInstance().getNumberOfRepositoriesToAnalyse();
            int counter = 0;
            while(repositoriesNames.next()) {
                this.currentRepository = repositoriesNames.getString("full_name");
                counter++;
                System.out.format("%s/%s Analyzing repositories :\t %s\n", counter, numberOfRepositoriesToAnalyse, currentRepository);
                try{
                    Path repoPath = Paths.get(repositoriesPath, currentRepository, ".git");
                    if(!Files.exists(repoPath))
                    {
                        System.err.println("Analyzer can't find repository: "+repoPath);
                        continue;
                    }
                    Path gitlock = Paths.get(repositoriesPath, currentRepository, ".git","index.lock");
                    if(Files.exists(gitlock))
                        gitlock.toFile().delete();

                    currentGit = new GitUtility(repositoriesPath,currentRepository);

                    try{
                        currentGit.gitCheckoutDefaultBranch();
                    }  catch(JGitInternalException | GitAPIException e) {
                        System.err.println("Cannot checkout to default-branch of repository "+currentRepository);
                        e.printStackTrace();
                    }

                    int res = loopOverCommits();
                    if(res==0 || res==2)
                        PsqlDB.getInstance().updateLatestAnalysedCommit(currentRepository, currentGit.getHeadOfRepo());
                } catch (Exception e)
                {
                    System.err.println("ERROR: Failed analyzing repo "+currentRepository);
                    e.printStackTrace();
                }
            }
            System.out.println("****** All "+numberOfRepositoriesToAnalyse+" repositories analyzed ****** [DONE]");
        } catch (Exception e){
            System.err.println("ERROR: Failed analyzing ALL REPO (??)");
            e.printStackTrace();
        }
    }

}
