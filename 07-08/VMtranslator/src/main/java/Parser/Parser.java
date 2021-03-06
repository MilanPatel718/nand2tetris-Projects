package Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import static org.apache.commons.io.FilenameUtils.removeExtension;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser implements Callable<Map<String,Pair<String,String>>>{
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private Path inputFilePath;
    private String fileName;
    private List<String> vmCommands;
    private Map<String, Pair<String, String>> parsedCommands;
    
    /**
     * 
     * @param inputFilePath
     */
    public Parser(Path inputFilePath){
        this.inputFilePath = inputFilePath;
        this.fileName = removeExtension(this.inputFilePath.getFileName().toString());
    }

    @Override
    public Map<String,Pair<String,String>> call() throws Exception {
        try{
                this.vmCommands = Files.lines(inputFilePath).map(s -> s.trim().replaceAll("//.*", "")).filter(s -> !s.isEmpty())
                .filter(s -> !s.matches("//.*")).collect(Collectors.toList());
        }
        catch(IOException e){
            logger.error("Input vm file could not be parsed because {}", e.getMessage());
            throw e;
        }
        this.parsedCommands = new LinkedHashMap<>();
        int i = 1;
        for (String command : this.vmCommands){
            String cmdType = commandType(command);
            Pair<String, String> commandMeta = Pair.with(cmdType, command);
            String commandKey = this.fileName + "_" + i;
            this.parsedCommands.put(commandKey, commandMeta);
            i++;
        }

        return this.parsedCommands;
    }

    /**
     * 
     * @param command
     * @return
     */
    private String commandType(String command) {
        StringTokenizer tokenizer = new StringTokenizer(command);
        String commandType = tokenizer.nextToken();
        
        if(commandType.equals("add") || commandType.equals("sub") || commandType.equals("neg") ||
           commandType.equals("eq") || commandType.equals("lt") || commandType.equals("gt") ||
           commandType.equals("and") || commandType.equals("or") || commandType.equals("not"))
           return "C_ARITHMETIC";
        
        else if(commandType.equals("pop"))
           return "C_POP";
        else if(commandType.equals("push"))
            return "C_PUSH";
        else if(commandType.equals("label"))
            return "C_LABEL";
        else if(commandType.equals("goto"))
            return "C_GOTO";
        else if(commandType.equals("if-goto"))
            return "C_IF";
        else if(commandType.equals("function"))
            return "C_FUNCTION";
        else if(commandType.equals("return"))
            return "C_RETURN";
        //commandType.equals("call")
        else
            return "C_CALL";
    }





}