package VMtranslator;

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
    private Future<List<String>>[] futures;
    private CodeWriter codeWriter;

    /**
     * Main method, creates VMtranslator instance calls run method
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        VMtranslator vMtranslator = new VMtranslator();
        vMtranslator.run(args);
    }

    private void run(String[] args) throws IOException {
        String vMarg = "";
        try {
            vMarg = args[0];
        } catch (IndexOutOfBoundsException e) {
            logger.error("VM file/directory not found");
        }

        //Check to see if input provided ends in .vm, indicating a single vm file input
        Pattern vmPattern = Pattern.compile("(.vm)$");
        Matcher vmMatcher = vmPattern.matcher(vMarg);
        Path inputPath = Paths.get(vMarg);

        if (vmMatcher.matches()){
            Path fullPath;
            try{
                fullPath = inputPath.toRealPath();
            }
            catch(IOException e){
                logger.error("Input vm file does not exist");
                throw(e);
            }
            this.executorService = Executors.newFixedThreadPool(1);
            this.futures = new Future[1];
            this.futures[0] = this.executorService.submit(new Parser(fullPath));
            this.codeWriter = new CodeWriter(this.futures);
        }

        
    }
}
