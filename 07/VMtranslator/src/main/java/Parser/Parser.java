package Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser implements Callable<List<String>> {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private Path inputFilePath;
    private List<String> parsedCommands;
    
    /**
     * 
     * @param inputFilePath
     */
    public Parser(Path inputFilePath){
        this.inputFilePath = inputFilePath;
    }

    @Override
    public List<String> call() throws Exception {
        try{
        this.parsedCommands = Files.lines(this.inputFilePath).map(s -> s.trim().replaceAll("//.*", "")).filter(s -> !s.isEmpty())
                                    .filter(s -> !s.matches("//.*")).collect(Collectors.toList());
        }
        catch(IOException e){
            logger.error("Input vm file could not be parsed because {}", e.getMessage());
            throw e;
        }
        return this.parsedCommands;
    }





}