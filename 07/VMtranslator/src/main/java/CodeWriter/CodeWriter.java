package CodeWriter;

import java.util.List;
import java.util.concurrent.Future;

public class CodeWriter{
    private Future<List<String>>[] parsedOutput;
    private String asmFileName;

    /**
     * 
     * @param parsedOutput
     */
    public CodeWriter(Future<List<String>>[] parsedOutput){
        this.parsedOutput = parsedOutput;

    }

    /**
     * 
     * @param asmFileName
     */
    public void setAsmFileName(String asmFileName) {
        this.asmFileName = asmFileName;
    }
}