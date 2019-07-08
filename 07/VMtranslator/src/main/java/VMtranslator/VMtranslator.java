package VMtranslator;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CodeWriter.CodeWriter;
import Parser.Parser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Virtual Machine class and application entry point Creates parsers and
 * code writer Translates input .vm file/files and outputs .asm assembly code
 */
public final class VMtranslator {
    private static final Logger logger = LoggerFactory.getLogger(VMtranslator.class);
    private ExecutorService executorService;
    private Future<Map<String,Pair<String,String>>>[] futures;
    private CodeWriter codeWriter;

    /**
     * Main method, creates VMtranslator instance calls run method
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws IndexOutOfBoundsException, Exception {
        VMtranslator vMtranslator = new VMtranslator();
        vMtranslator.run(args);
    }

    private void run(String[] args) throws IndexOutOfBoundsException, Exception {
        String vMarg = "";
        try {
            vMarg = args[0];
        } catch (IndexOutOfBoundsException e) {
            logger.error("VM file/directory not found");
            throw e;
        }

        //Check to see if input provided ends in .vm, indicating a single vm file input
        Pattern vmPattern = Pattern.compile(".*.vm");
        Matcher vmMatcher = vmPattern.matcher(vMarg);
        Path inputPath = Paths.get(vMarg);
        
        //Single .vm file input
        if (vmMatcher.matches()){
            Path fullPath;
            try{
                fullPath = inputPath.toRealPath();
            }
            catch(IOException e){
                logger.error("Input vm file does not exist");
                throw e;
            }
            logger.info("Starting Threads");
            this.executorService = Executors.newFixedThreadPool(1);
            this.futures = new Future[1];
            this.futures[0] = this.executorService.submit(new Parser(fullPath));
            this.codeWriter = new CodeWriter(this.futures);

            //Set asm file name
            this.codeWriter.setAsmFileName(fullPath.getFileName().toString());
            try{
                this.codeWriter.writeLines();
                this.codeWriter.terminateProgram();
            }
            catch(Exception e){
                logger.error("Error with {} due to " + e.getMessage(), this.codeWriter.getAsmFileName());
                throw e;
            }
            this.executorService.shutdown();
        }

        
    }
}
