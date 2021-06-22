package git;

import analyser_db.PsqlDB;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class GitUtility {
    private Git git;
    private String currentRepository;

    public GitUtility(String repositoriesPath, String currentRepository) throws IOException {
        git = Git.open(Paths.get(repositoriesPath,currentRepository).toFile());
        this.currentRepository = currentRepository;
    }

    public Iterable<RevCommit> gitLog(){
        try{
            return git.log().call();
        }
        catch(GitAPIException e){
            System.err.println("Cannot 'git log' "+currentRepository);
            e.printStackTrace();
            return null;
        }
    }

    public List<DiffEntry> gitDiff(RevCommit commit){
        try{
            ObjectReader reader = git.getRepository().newObjectReader();

            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
            newTreeParser.reset(reader, commit.getTree());

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            oldTreeParser.reset(reader, commit.getParent(0).getTree());

            return git.diff().setNewTree(newTreeParser).setOldTree(oldTreeParser).call();

        }catch (IOException| GitAPIException e){
            System.err.println("Cannot `git diff` of "+commit.getId().getName()+" of repository "+currentRepository);
            e.printStackTrace();
            return null;
        }
    }

    public void gitCheckoutCommit(String commitSHA) throws GitAPIException{
        if(this.getHeadOfRepo().equals(commitSHA))
            return;
        this.git.checkout().setName(commitSHA).call();
    }

    public void gitCheckoutDefaultBranch() throws GitAPIException{
        String defaultBranch = PsqlDB.getInstance().getRepoDefaultBranch(currentRepository);
        if(defaultBranch==null){
            System.err.println("Cannot checkout to default_branch of "+currentRepository);
            return;
        }
        git.checkout().setName(defaultBranch).call();
    }

    public String getHeadOfRepo(){
        try{
            return git.getRepository().resolve(Constants.HEAD).name();
        }catch(IOException e){
            System.err.println("Cannot get latest commit SHA of repository "+currentRepository);
            return null;
        }
    }
}
