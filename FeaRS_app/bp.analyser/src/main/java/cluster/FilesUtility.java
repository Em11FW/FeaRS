package cluster;

import analyser_db.PsqlDB;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FilesUtility {
    private static FilesUtility creatorInstance = null;

    public static FilesUtility getInstance() {
        if(creatorInstance == null){
            creatorInstance = new FilesUtility();
        }
        return creatorInstance;
    }

    private FilesUtility() {}

    public int createMethodsFiles(String methodsPath){
        if(!Files.exists(Paths.get(methodsPath))){
            Paths.get(methodsPath).toFile().mkdirs();
        }

        ResultSet results = PsqlDB.getInstance().getAllMethods();
        try{
            int nMethods = 0;
            while(results.next()){
                File newFile = Paths.get(methodsPath,results.getLong("id")+".txt").toFile();
                boolean b = newFile.createNewFile();
                writeMethodIntoNewFile(methodsPath, results, newFile);
                nMethods++;
            }
            return nMethods;
        }
        catch(SQLException| IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    private void writeMethodIntoNewFile(String methodsPath, ResultSet results, File newFile) throws IOException, SQLException {
        FileWriter methodWriter = new FileWriter(Paths.get(methodsPath, newFile.getName()).toFile());
        methodWriter.write(results.getString("body"));
        methodWriter.close();
    }

    public void deleteFolder(String folderPath){
        File folder = new File(folderPath);
        try {
            FileUtils.cleanDirectory(folder);
            FileUtils.forceDelete(folder);
        }catch(IOException e){
            System.err.println("Cannot delete "+folderPath);
            e.printStackTrace();
        }
    }
}
