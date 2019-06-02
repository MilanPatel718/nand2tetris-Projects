package Parser;

import java.io.IOException;
import java.util.List;
import Coder.Coder;


public class Parser {
    private List<String> commands;

    /**
     * Constructor for Parser Class
     * 
     * @param instructions
     * @throws IOException
     */
    public Parser(List<String> commands){
        this.commands = commands;
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