package db;

import java.sql.*;
import java.time.LocalDateTime;

public class PsqlDB {

    private static PsqlDB psqlDBInstance = null;
    private Connection connection;
    private PreparedStatement prepStatement;


    private PsqlDB(){}

    public static PsqlDB getInstance(){
        if(psqlDBInstance == null){
            psqlDBInstance = new PsqlDB();
        }
        return psqlDBInstance;
    }
    
    public void connect(String url, String dbName, String user, String password){
        try{
            this.connection = DriverManager.getConnection(url + dbName, user, password);
        } catch (SQLException e){
            System.err.println("Cannot connect DB");
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            if(!this.connection.isClosed()) {
                this.connection.close();
                System.out.println("DB is disconnected");
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
            System.err.println("Cannot create prepare statement");
            e.printStackTrace();
        }
    }

    public LocalDateTime getLastRun(){
        LocalDateTime latestRunDatetime = LocalDateTime.of(2008,01,01,00,00);
        String sql = "SELECT max(execution_date) FROM history;";
        createPrepStatement(sql);
        try{
            ResultSet results = this.prepStatement.executeQuery();
            while ( results.next() ) {
                latestRunDatetime = results.getTimestamp(1).toLocalDateTime();
            }
            results.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return latestRunDatetime;
    }

    public String getDefaultBranch(String fullName){
        String sql = "SELECT default_branch FROM repositories WHERE full_name='"+fullName+"';";
        createPrepStatement(sql);
        try{
            ResultSet results = this.prepStatement.executeQuery();
            while(results.next()) {
                return results.getString("default_branch");
            }
        }catch(SQLException e){
            System.err.println("Cannot get "+fullName+"'s default branch");
            e.printStackTrace();
        }
        return "";
    }

    public ResultSet getRepositoriesToUpdate(){
        ResultSet results = null;
        String sql = "Select full_name from repositories where is_uptodate=false;";
        createPrepStatement(sql);
        try{
            results = this.prepStatement.executeQuery();
        }catch (SQLException e){
            System.err.println("Cannot get repositories to update");
            e.printStackTrace();
        }
        return results;
    }

    public int getNumberOfRepositoriesToUpdate(){
        String sql = "Select count(full_name) from repositories where is_uptodate=false;";
        createPrepStatement(sql);
        try{
            ResultSet results = this.prepStatement.executeQuery();
            while(results.next()){
                return results.getInt(1);
            }
        }catch(SQLException e){
            System.err.println("Cannot get number of repositories to update");
            e.printStackTrace();
        }
        return 0;
    }

    public void insertRepository(String fullName, String defaultBranch){
        String sql = "INSERT INTO repositories (full_name,is_uptodate,default_branch) VALUES (?,?,?)"
                +"ON CONFLICT (full_name) DO UPDATE SET is_uptodate=excluded.is_uptodate , default_branch=excluded.default_branch;";
        createPrepStatement(sql);
        try {
            this.prepStatement.setString(1, fullName);
            this.prepStatement.setBoolean(2, false);
            this.prepStatement.setString(3, defaultBranch);
            this.prepStatement.executeUpdate();
        }catch (SQLException e){
            System.err.println("Cannot insert repository "+fullName);
            e.printStackTrace();
        }
    }

    public void updateRepositoryLatestCommit(String fullName, String latestCommit){
        String sql = "UPDATE repositories SET latest_commit=?, is_uptodate=? WHERE full_name=?;";
        createPrepStatement(sql);
        try{
            this.prepStatement.setString(1,latestCommit);
            this.prepStatement.setBoolean(2, true);
            this.prepStatement.setString(3, fullName);
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot update repository "+fullName);
            e.printStackTrace();
        }
    }

    public void insertHistoryRecord(LocalDateTime now){
        Timestamp datetime = Timestamp.valueOf(now);
        String sql = "INSERT INTO history (execution_date)"
                + "VALUES (?)"
                +"ON CONFLICT (execution_date) DO NOTHING;";
        createPrepStatement(sql);
        try {
            this.prepStatement.setTimestamp(1, datetime);
            this.prepStatement.executeUpdate();
        }catch(SQLException e){
            System.err.println("Cannot insert record "+now+" in history table");
            e.printStackTrace();
        }
    }

    public void updateHistoryRecord(LocalDateTime executionDate, int newRepositories, int updatedRepositories){
        Timestamp datetime = Timestamp.valueOf(executionDate);
        String sql = "UPDATE history SET number_new_repositories=?, number_updated_repositories=?"
                +" WHERE execution_date=?;";
        createPrepStatement(sql);
        try {
            this.prepStatement.setInt(1, newRepositories);
            this.prepStatement.setInt(2, updatedRepositories);
            this.prepStatement.setTimestamp(3, datetime);
            this.prepStatement.executeUpdate();
        }catch (SQLException e){
            System.err.println("Cannot update record "+datetime+" in history table");
            e.printStackTrace();
        }

    }

    public void deleteRepository(String fullName){
        String sql = "DELETE FROM repositories WHERE full_name='"+fullName+"';";
        createPrepStatement(sql);
        try{
            this.prepStatement.executeUpdate();
        }catch (SQLException e){
            System.err.println("Cannot delete repository "+fullName);
            e.printStackTrace();
        }
    }



}
