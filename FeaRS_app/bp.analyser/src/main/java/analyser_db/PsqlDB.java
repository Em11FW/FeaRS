package analyser_db;

import arule.AssociationsFileParser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import parser.RepositoryNewMethodsExtractor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class PsqlDB {
    private static PsqlDB psqlInstance = null;
    private Connection connection;
    private PreparedStatement prepStatement;
    private PsqlDB(){}

    public static PsqlDB getInstance(){
        if(psqlInstance==null){
            psqlInstance = new PsqlDB();
        }
        return psqlInstance;
    }

    public void connect(String url, String dbName, String user, String psw){
        try{
            this.connection = DriverManager.getConnection(url + dbName, user, psw);
            System.out.println("DB is connected.");
        } catch (SQLException e){
            System.err.println("Cannot connect DB");
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            if(!this.connection.isClosed()) {
                this.connection.close();
                System.out.println("DB is disconnected.");
            }
        }catch(SQLException e){
            System.err.println("Cannot disconnect DB");
            e.printStackTrace();
        }
    }

    private void createPrepStatement(String sql){
        try {
            this.prepStatement = this.connection.prepareStatement(sql);
        }catch(SQLException e){
            this.prepStatement = null;
            System.err.println("Cannot create prepare statement");
            e.printStackTrace();
        }
    }
    private ResultSet getQueryResultSet(String sql, String errorMsg){
        createPrepStatement(sql);
        ResultSet results = null;
        try{
            results = this.prepStatement.executeQuery();
        }catch(SQLException e){
            System.err.println(errorMsg);
            e.printStackTrace();
        }
        return results;
    }

    public ResultSet getRepositoriesToAnalyse(){
        String sql = "SELECT full_name FROM repositories WHERE (latest_commit IS NOT NULL) AND (latest_commit<>latest_analysed_commit OR latest_analysed_commit IS NULL);";
        String errorMsg = "Cannot get repositories to analyse.";
        return getQueryResultSet(sql, errorMsg);
    }

    public void insertCommit(String repoFullName, String commitSHA, LocalDateTime commitDate){
        Timestamp datetime = Timestamp.valueOf(commitDate);
        String sql = "INSERT INTO commits (repository_full_name, commit_sha, date_of_commit) VALUES (?,?,?);";
        createPrepStatement(sql);
        try {
            this.prepStatement.setString(1, repoFullName);
            this.prepStatement.setString(2,commitSHA);
            this.prepStatement.setTimestamp(3, datetime);
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot insert commit "+commitSHA+" of repository "+repoFullName);
            e.printStackTrace();
        }
    }

    public String getLatestAnalysedCommit(String repoFullName){
        String sql = "SELECT latest_analysed_commit FROM repositories WHERE full_name='"+repoFullName+"';";
        ResultSet results = getQueryResultSet(sql, "");
        String latestAnalysedCommit = null;
        try {
            while (results.next()) {
                latestAnalysedCommit = results.getString("latest_analysed_commit");
            }
        }catch(SQLException e){
            System.err.println("Cannot get latest_analysed_commit of repository "+repoFullName);
            e.printStackTrace();
        }
        return latestAnalysedCommit;
    }

    public void updateLatestAnalysedCommit(String repoFullName, String latestCommit){
        String sql = "UPDATE repositories SET latest_analysed_commit=? WHERE full_name=?;";
        createPrepStatement(sql);
        try{
            this.prepStatement.setString(1,latestCommit);
            this.prepStatement.setString(2, repoFullName);
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot update latest_analysed_commit column of repository "+repoFullName);
            e.printStackTrace();
        }
    }

    public String getRepoDefaultBranch(String repoFullName){
        String sql = "SELECT default_branch FROM repositories WHERE full_name='"+repoFullName+"';";
        ResultSet results = getQueryResultSet(sql, "");
        try {
            while (results.next()) {
                return results.getString("default_branch");
            }
        }catch(SQLException e){
            System.err.println("Cannot get default_branch of repository "+repoFullName);
            e.printStackTrace();
        }
        return null;
    }

    public Long getCommitId(String repoFullName, String commitSHA){
        String sql = "SELECT id FROM commits WHERE repository_full_name='"+repoFullName+"' AND commit_sha='"+commitSHA+"';";
        createPrepStatement(sql);
        ResultSet results = getQueryResultSet(sql, "");
        try{
            while(results.next()){
                return results.getLong("id");
            }
        }catch(SQLException e){
            System.err.println("Cannot get id of commit "+commitSHA+" of "+repoFullName);
            e.printStackTrace();
        }
        return null;
    }

    public void insertBatchOfMethods(Long commitId, List<RepositoryNewMethodsExtractor.ExtractedMethodInfo> allMethods) {
        String sql = "INSERT INTO methods (commit_id,filepath,body) VALUES (?,?,?);";
        try {
            this.connection.setAutoCommit(false);
            createPrepStatement(sql);
            for(RepositoryNewMethodsExtractor.ExtractedMethodInfo method: allMethods)
            {
                this.prepStatement.setLong(1, commitId);
                this.prepStatement.setString(2, method.filePath);
                this.prepStatement.setString(3, method.methodBody);
                this.prepStatement.addBatch();
            }
            prepStatement.executeBatch();
            this.connection.commit();
            this.connection.setAutoCommit(true);
        }catch(SQLException e){
            System.err.println("Cannot insert methods of commit "+commitId);
            e.printStackTrace();
        }
    }


    public ResultSet getAllMethods(){
        String sql = "SELECT * FROM methods;";
        return getQueryResultSet(sql,"Cannot get methods");
    }

    public ResultSet getMethodsWithClusterInfo(){
        String sql = "SELECT * FROM methods WHERE cluster_id IS NOT NULL;";
        return getQueryResultSet(sql,"Cannot get methods");
    }

    public ImmutablePair<String, String> getMethodBody(long id){
//        String sql = "SELECT body FROM methods WHERE id=?;";
        String sql = "SELECT methods.body AS body, commits.repository_full_name AS repo, commits.commit_sha AS sha\n" +
                "FROM methods JOIN commits ON methods.commit_id = commits.id WHERE methods.id= ?;";
        createPrepStatement(sql);
        try{
            this.prepStatement.setLong(1, id);
            ResultSet results = this.prepStatement.executeQuery();
            while(results.next()){
                String body = results.getString("body");
                String source_repo = results.getString("repo");
                String source_sha = results.getString("sha");
                return ImmutablePair.of(body, "https://github.com/"+source_repo+"/commit/"+source_sha);
            }
        }catch(SQLException e){
            System.err.println("Cannot get method body of method "+id);
            e.printStackTrace();
        }
        return null;
    }

    public void updateMethodCluster(long methodId, long clusterId){
        String sql = "UPDATE methods SET cluster_id=? WHERE id=?";
        createPrepStatement(sql);
        try{
            this.prepStatement.setLong(2, methodId);
            this.prepStatement.setLong(1, clusterId);
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot update cluster id of method "+methodId);
            e.printStackTrace();
        }
    }

    public void insertCluster(long clusterId, String centroidBody, String source){
        String sql = "INSERT INTO clusters (id, centroid_body, source) VALUES (?,?,?);";
        createPrepStatement(sql);
        try{
            this.prepStatement.setLong(1, clusterId);
            this.prepStatement.setString(2, centroidBody);
            this.prepStatement.setString(3, source);
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot insert cluster "+clusterId);
            e.printStackTrace();
        }
    }

    public int getNumberNonClusteredMethods(){
        // It shouldn't be "COUNT(cluster_id)". Because "COUNT" function ignores NULL and the result is 0 always.
        String sql = "SELECT COUNT(id) FROM methods WHERE cluster_id IS NULL;";
        ResultSet results = getQueryResultSet(sql, "Problems counting non-clustered methods.");
        try {
            while (results.next()) {
                return results.getInt(1);
            }
        }catch(SQLException e){
            System.err.println("Cannot get number of non-clustered methods");
            e.printStackTrace();
        }
        return 0;
    }

    public void cleanClusterTable(){
        String sql = "DELETE FROM clusters;";
        createPrepStatement(sql);
        try{
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot clean table clusters");
            e.printStackTrace();}
    }

    public void insertARules(List<AssociationsFileParser.ARuleItem> aRuleItemList, int sensitivity_level, long startID){
        String sql = "INSERT INTO arules VALUES(?,?,?,?,?,?,?,?)";
        try {
            this.connection.setAutoCommit(false);
            final int N = aRuleItemList.size();
            long ARule_ID = 0;
            createPrepStatement(sql);
            for(AssociationsFileParser.ARuleItem rule: aRuleItemList) {
                this.prepStatement.setLong(1, startID+ARule_ID);
                ARule_ID++;
                this.prepStatement.setArray(2, connection.createArrayOf("BIGINT", rule.LES.toArray()));
                this.prepStatement.setLong(3, rule.RES);
                this.prepStatement.setDouble(4, rule.support);
                this.prepStatement.setDouble(5, rule.confidence);
                this.prepStatement.setDouble(6, rule.lift);
                this.prepStatement.setLong(7, rule.count);
                this.prepStatement.setInt(8, sensitivity_level);
                this.prepStatement.addBatch();

                if(ARule_ID%10000 == 0 || ARule_ID==N)
                    prepStatement.executeBatch();
            }
            this.connection.commit();
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Failed inserting rules !");
            e.printStackTrace();
        }
    }

    public void cleanARulesTable(){
        String sql = "DELETE FROM arules;";
        createPrepStatement(sql);
        try{
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot clean table arules");
            e.printStackTrace();
        }
    }

    public void deleteUnusedCommits(){
        String sql = "DELETE FROM commits WHERE id NOT IN (SELECT commit_id FROM methods);";
        createPrepStatement(sql);
        try{
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot delete commits without methods referencing to them");
            e.printStackTrace();
        }

    }

    public int getNumberOfRepositoriesToAnalyse(){
        String sql = "select count(full_name) from repositories where (latest_analysed_commit<>latest_commit OR latest_analysed_commit is null);";
        createPrepStatement(sql);
        try{
            ResultSet results = this.prepStatement.executeQuery();
            while(results.next()){
                return results.getInt(1);
            }
        }catch(SQLException e){
            System.err.println("Cannot get number of repositories to analyse");
            e.printStackTrace();
        }
        return 0;
    }
}
