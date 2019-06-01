package Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Coder.Coder;


public class Parser {
    private List<String> commands;
    private Logger logger;

    /**
     * Constructor for Parser Class
     * 
     * @param instructions
     */
    public Parser(String assemblyFile) {
        logger = LoggerFactory.getLogger(Parser.class);
        try {
            commands = Files.lines(Paths.get(assemblyFile)).map(s -> s.replaceAll("//.*", "")).map(s -> s.trim()).filter(s -> !s.isEmpty())
                            .filter(s->!s.matches("//.*")).collect(Collectors.toList());
        } catch (IOException e) {
            logger.info("Input file could not be read because " + e.getMessage());
        }
    }

    /**
     * Getter for Parser commands
     * 
     * @return commands
     */
    public List<String> getCommands() {
        return commands;
    }
    /**
     * Setter for Parser commands
     * 
     * @param commands
     */
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    /**
     * Takes in current command and returns the correct type of command
     * 
     * @param currCommand
     * @return String
     */
	public String commandType(String currCommand) {
        if(currCommand.matches("[@].*"))
            return "A_COMMAND";
        else if(currCommand.matches("[(].*[)]"))
            return "L_COMMAND";
        else
            return "C_COMMAND";
	}
    /**
     * Takes in current command and command type
     * Uses both to determine the appropriate symbol to return back to the assembler
     * 
     * @return
     * @param currCommand
     * @param cmdType
     */
	public String symbol(String currCommand, String cmdType) {
        String symbolReturn;
        if(cmdType.equals("A_COMMAND")){
            symbolReturn = currCommand.substring(1);
        }
        else{
            symbolReturn = currCommand.substring(1, currCommand.length() - 1);
        }
        return symbolReturn;
	}
    /**
     * Returns dest mneumonic of C command
     * 
     * @return String
     * @param cIns
     */
	public String dest(String cIns) {
		return Coder.dest(cIns);
	}
    /**
     * Returns comp mneumonic of C command
     * 
     * @return String
     * @param cIns
     */
	public String comp(String cIns) {
		return Coder.comp(cIns);
	}
    /**
     * Returns jump mneumonic of C command
     * 
     * @return String
     * @param cIns
     */
	public String jump(String cIns) {
		return Coder.jump(cIns);
	}
    
}