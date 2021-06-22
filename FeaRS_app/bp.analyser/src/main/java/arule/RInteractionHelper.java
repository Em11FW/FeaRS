package arule;

import org.rosuda.JRI.Rengine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;


public class RInteractionHelper {
    private static RInteractionHelper helperInstance = null;
    Rengine rengine;
    final static String inputFilePath = "transactions.csv";
    final static String rules_output_prefix = "generated-rules";

    private RInteractionHelper(){
        rengine = initREngine();
    }

    public static RInteractionHelper getInstance(){
        if(helperInstance==null)
            helperInstance = new RInteractionHelper();
        return helperInstance;
    }

    private Rengine initREngine(){
        String[] Rargs = {"--vanilla", "--args", inputFilePath, rules_output_prefix}; //--save --no-save
        Rengine rengine = Rengine.getMainEngine();
        if(rengine == null ) {
            rengine = new Rengine(Rargs, false, null);
            if (!rengine.waitForR()) {
                System.err.println("Cannot load R");
                System.exit(1);
            }
        }
        return rengine;
    }

    public String executeARule(int sensitivity_level) throws Exception{
        String output_file_name = String.format("%s-%d.csv", rules_output_prefix, sensitivity_level);

        final String R_LOG_PATH = "Rlogfile.txt";
        File rlogFile = new File(R_LOG_PATH);
        rlogFile.createNewFile();
        rengine.eval("log<-file('"+rlogFile.getPath()+"')");
        rengine.eval("sink(log, append=TRUE)");
        rengine.eval("sink(log, append=TRUE, type='message')");
        try{
            System.out.println("\t\t\tRunning R script ....");
            readScript(rengine, sensitivity_level);
            System.out.println("\t\t\tRunning R script [DONE]");
        }catch(IOException e){
            System.err.println("Failed running R script and generating Association Rule");
            e.printStackTrace();
        }
        boolean isError = printError();
        if(isError){
            System.err.println("Error found in the output log of running R Script (See above)");
        }
        rengine.end();
        rlogFile.delete();
        return output_file_name;
    }

    private void readScript(Rengine rengine, int sensitivity_level) throws IOException {
        InputStream in = getClass().getResourceAsStream("/arule_valentina_"+sensitivity_level+".R");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while((line = reader.readLine()) != null) {
            rengine.eval(line);
        }
        reader.close();
    }

    private boolean printError() throws Exception{
        List<String> lines = Files.readAllLines(Paths.get("Rlogfile.txt"));
        boolean isError = false;
        StringBuffer errrorMsg = new StringBuffer();
        for(String line : lines){
            if(line.equals("In addition: Warning message:"))
                isError = true;
            if(isError)
                errrorMsg.append(line).append("\n");
        }
        if(isError) {
            System.err.println(errrorMsg);
            return true;
        }
        return false;
    }

}
