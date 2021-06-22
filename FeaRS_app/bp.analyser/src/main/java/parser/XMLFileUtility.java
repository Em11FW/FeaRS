package parser;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class XMLFileUtility {
    private static XMLFileUtility shellCmdExecutorInstance = null;
    private int id = 0;

    public static XMLFileUtility getInstance() {
        if(shellCmdExecutorInstance == null){
            shellCmdExecutorInstance = new XMLFileUtility();
        }
        return shellCmdExecutorInstance;
    }

    private XMLFileUtility() {}

    private void incrementId(){
        this.id += 1;
    }

    public Path createXMLFile(String source, Path filePath, String destination){
        Path xmlFilepath = null;
        try{
            xmlFilepath = Paths.get(destination,"code"+id+".java.xml");

            xmlFilepath.toFile().getParentFile().mkdirs(); // srcml fails if parent directories don't exist
            incrementId();
            String[] srcmlCommand = {"srcml","--position", Paths.get(source).resolve(filePath).toString(), "-o", xmlFilepath.toString()};
            ProcessBuilder processBuilder = new ProcessBuilder(srcmlCommand);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            Process process = processBuilder.start();
            String output = readInputStream(process.getInputStream());
            process.waitFor();
            int exitCode = process.exitValue();
            if(exitCode!=0){
                System.err.println();
                System.err.println("[SrcML] return code: "+exitCode+". Cannot create XML file of "+Paths.get(source).resolve(filePath));
                return null;
            }
        }catch(IOException | InterruptedException e){
            System.err.println("[SrcML] Cannot create XML file of "+Paths.get(source).resolve(filePath));
            e.printStackTrace();
            return null;
        }
        return xmlFilepath;
    }

    private String readInputStream(InputStream is)
    {
        StringBuilder output = new StringBuilder();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            int ch;
            while ((ch = reader.read()) != -1) {
                if((char)ch=='\r') {
                    System.out.println("84");
                    continue;
                }
                System.out.println("83");
                output.append((char) ch);
                System.out.println("83");
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public void deleteXMLFile(Path filepath){
        try{
            Files.delete(filepath);
        }catch(IOException e ){
            System.err.println("Cannot delete file "+filepath);
            e.printStackTrace();
        }
    }

    public void deleteXMLFilesDirectory(String pathname){
        try {
            FileUtils.deleteDirectory(new File(pathname));
        }catch(IOException e){
            System.err.println("Cannot delete "+pathname);
            e.printStackTrace();
        }
    }

    public ArrayList<FileFunction> getXMLFileFunctions(Path filepath){
        ArrayList<FileFunction> fileFunctions = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        try {
            Document readDoc = builder.build(filepath.toFile());

            Element root = readDoc.getRootElement();
            Namespace rootNamespace = root.getNamespace();

            for(Element javaClass : root.getChildren("class", rootNamespace)){
                Namespace javaClassNameSpace = javaClass.getNamespace();

                for(Element classBlock : javaClass.getChildren("block", javaClassNameSpace)){
                    Namespace classBlockNameSpace = classBlock.getNamespace();
                    storeFileFunctions(fileFunctions, classBlock.getChildren("function", classBlockNameSpace));
                    storeFileFunctions(fileFunctions, classBlock.getChildren("constructor", classBlockNameSpace));
                }
            }
        }catch (IOException | JDOMException e){
            System.err.println("Cannot extract functions from file "+filepath);
            e.printStackTrace();
        }
        return fileFunctions;
    }

    private void storeFileFunctions(ArrayList<FileFunction> fileFunctions, List<Element> functions) {
        for (Element function : functions) {
            Namespace attributeNameSpace = function.getAttributes().get(0).getNamespace();

            String start = function.getAttributeValue("start", attributeNameSpace);
            String end = function.getAttributeValue("end", attributeNameSpace);

            int startLine = Integer.parseInt(start.substring(0, start.indexOf(':')));
            int endLine = Integer.parseInt(end.substring(0, end.indexOf(':')));

            fileFunctions.add(new FileFunction(startLine, endLine));
        }
    }
}
