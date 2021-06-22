package parser;

import analyser.AsiaUtility;
import analyser_db.PsqlDB;
import git.GitUtility;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RepositoryNewMethodsExtractor {
    private String repositoriesPath;
    private String xmlFilesDestinationPath;

    final static private int MIN_METHOD_TOKENS = 6;

    public RepositoryNewMethodsExtractor(String repositoriesPath, String xmlPath){
        this.repositoriesPath = repositoriesPath;
        this.xmlFilesDestinationPath = xmlPath;
    }

    public List<ExtractedMethodInfo> extractMethodsFromFile(String repoFullName, RevCommit commit, DiffEntry file){
        GitUtility currentGit;
        try{ currentGit = new GitUtility(repositoriesPath,repoFullName);
        }catch(IOException e){
            System.err.println("Cannot open repository "+repoFullName);
            e.printStackTrace();
            return null;
        }
        try{ currentGit.gitCheckoutCommit(commit.getId().getName());
        }catch(GitAPIException | JGitInternalException e){
            System.err.println("Cannot checkout to commit "+commit.getId().getName()+" of repository "+repoFullName);
            e.printStackTrace();
            return null;
        }
        Path xmlfilePathname = XMLFileUtility.getInstance().createXMLFile(repositoriesPath, Paths.get(repoFullName, file.getNewPath()), xmlFilesDestinationPath);
        if(xmlfilePathname==null) {
            System.out.println("xmlfilePathname is NULL (1)");
            return null;
        }
        String type = file.getChangeType().toString();
        //file is new
        if(type.equals("ADD")) {
            List<ExtractedMethodInfo> res = extractNewMethodsFromAddedFile(repoFullName, commit, file, xmlfilePathname);
            return res;
        }
        else
        {
            //file is modified
            ArrayList<String> methods = getNewMethodsBodyFromFile(XMLFileUtility.getInstance().getXMLFileFunctions(xmlfilePathname), readFile(repoFullName, file));
            if(methods.isEmpty())
                return null;
            try{ currentGit.gitCheckoutCommit(commit.getParent(0).getId().getName());
            }catch(JGitInternalException | GitAPIException e){
                System.err.println("Cannot checkout to commit "+commit.getId().getName()+" of repository "+repoFullName);
                e.printStackTrace();
                return null;
            }
            Path xmlPrevFilePathname = XMLFileUtility.getInstance().createXMLFile(repositoriesPath,Paths.get(repoFullName, file.getNewPath()), xmlFilesDestinationPath);
            if(xmlPrevFilePathname == null) {
                System.out.println("xmlfilePathname is NULL (2)");
                return null;
            }
            List<ExtractedMethodInfo> res = extractNewMethodsFromModifiedFile(repoFullName, commit, file, xmlfilePathname, xmlPrevFilePathname, methods);
            return res;
        }
    }

    private List<ExtractedMethodInfo> extractNewMethodsFromAddedFile(String repoFullName, RevCommit commit, DiffEntry file, Path xmlfilePathname) {
        List<ExtractedMethodInfo> res = ExtractNewMethodsFromNewFile(repoFullName, commit, file, readFile(repoFullName,file), XMLFileUtility.getInstance().getXMLFileFunctions(xmlfilePathname));
        XMLFileUtility.getInstance().deleteXMLFile(xmlfilePathname);
        return res;
    }

    private List<ExtractedMethodInfo> extractNewMethodsFromModifiedFile(String repoFullName, RevCommit commit, DiffEntry file, Path xmlfilePathname, Path xmlPrevFilePathname, ArrayList<String> methods) {
        List<ExtractedMethodInfo> res = new ArrayList<>();
        ArrayList<String> prevVersionMethods = getNewMethodsBodyFromFile(XMLFileUtility.getInstance().getXMLFileFunctions(xmlPrevFilePathname), readFile(repoFullName, file));
        if(!prevVersionMethods.isEmpty()) {
            res = compareMethods(repoFullName, commit, file, methods, prevVersionMethods);
        }
        XMLFileUtility.getInstance().deleteXMLFile(xmlfilePathname);
        XMLFileUtility.getInstance().deleteXMLFile(xmlPrevFilePathname);
        return res;
    }

    private List<ExtractedMethodInfo> compareMethods(String repoFullName, RevCommit commit, DiffEntry file, ArrayList<String> methods, ArrayList<String> prevVersionMethods) {

        List<ExtractedMethodInfo> res = new ArrayList<>();
        Set<Integer> alreadyMatchedPrevMethodIndex = new HashSet<>();

        for(String method : methods){
            boolean matchFound = false;

            for(int prevMethodIndex=0; prevMethodIndex<prevVersionMethods.size(); prevMethodIndex++) {

                if(alreadyMatchedPrevMethodIndex.contains(prevMethodIndex))
                    continue;

                String prevMethod = prevVersionMethods.get(prevMethodIndex);
                Double similarityRate = -1.0;
                try{
                    if(method.equals(prevMethod))
                        similarityRate = 1.0;
                    else
                        similarityRate = AsiaUtility.getInstance().computeSimilarity(method, prevMethod);
                }catch(Exception e){e.printStackTrace();}

                if(similarityRate>=0.90){
                    matchFound = true;
                    alreadyMatchedPrevMethodIndex.add(prevMethodIndex);
                    break;
                }
            }

            if(!matchFound)
                res.add(new ExtractedMethodInfo(file.getNewPath(), method));
        }

        return res;
    }

    private String createMethodBody(List<String> fileLines, FileFunction function) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = function.getStartLine() - 1; i < function.getEndLine(); i++) {
            stringBuilder.append(fileLines.get(i));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private ArrayList<String> getNewMethodsBodyFromFile(ArrayList<FileFunction> functions, List<String> fileLines){
        ArrayList<String> methods = new ArrayList<>();
        if(fileLines.isEmpty())
            return methods;
        for(FileFunction function : functions){
            String methodBody = createMethodBody(fileLines, function);
            int nTokens = new StringTokenizer(methodBody).countTokens();
            if(nTokens < MIN_METHOD_TOKENS)
                continue;
            methods.add(methodBody);
        }
        return methods;
    }

    private List<ExtractedMethodInfo> ExtractNewMethodsFromNewFile(String repoFullName, RevCommit commit, DiffEntry file, List<String> fileLines, ArrayList<FileFunction> functions) {
        List<ExtractedMethodInfo> res = new ArrayList<>();

        if(fileLines.isEmpty())
            return res;

        for(FileFunction function : functions){
            String methodBody = createMethodBody(fileLines, function);
            int nTokens = new StringTokenizer(methodBody).countTokens();
            if(nTokens < MIN_METHOD_TOKENS)
                continue;
            res.add( new ExtractedMethodInfo(file.getNewPath(), methodBody));
        }
        return res;
    }

    private List<String> readFile(String repoFullName, DiffEntry file) {
        List<String> fileLines = Collections.emptyList();
        try{
            fileLines = Files.readAllLines(Paths.get(repositoriesPath,repoFullName,file.getNewPath()));
        } catch (java.nio.charset.MalformedInputException e)
        {
            System.err.println("Cannot read Malformed file: "+Paths.get(repoFullName, file.getNewPath()));
        }
        catch(IOException e){
            System.err.println("Cannot read file: "+Paths.get(repoFullName, file.getNewPath()));
            e.printStackTrace();
        }
        return fileLines;
    }

    public static class ExtractedMethodInfo {
        public String filePath;
        public String methodBody;

        public ExtractedMethodInfo(String filePath, String methodBody) {
            this.filePath = filePath;
            this.methodBody = methodBody;
        }
    }
}
