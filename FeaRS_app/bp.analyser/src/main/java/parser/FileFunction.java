package parser;

public class FileFunction {
    private int startLine;
    private  int endLine;

    FileFunction(int start, int end){
        this.startLine = start;
        this.endLine = end;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }
}
