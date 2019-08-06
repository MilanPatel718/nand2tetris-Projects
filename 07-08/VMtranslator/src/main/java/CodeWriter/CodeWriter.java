package CodeWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.apache.commons.io.FilenameUtils.removeExtension;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeWriter {
    private static final Logger logger = LoggerFactory.getLogger(CodeWriter.class);
    private Future<Map<String,Pair<String,String>>>[] parsedOutput;
    private String asmFileName;
    private String fileName;
    private static String currentClass;

    //Globals for CodeWriter
    private static String stackInc = "@R0\nM=M+1\n";
    private static String stackDec = "@R0\nM=M-1\n";
    private static String progTerminate = "(INFINITE_LOOP)\n@INFINITE_LOOP\n0;JMP";
    //private static String sysInit = "@256\nD=A\n@SP\nM=D\n@Sys.init\n0;JMP\n";
    private static int eqId = 1;
    private static int gtId = 1;
    private static int ltId = 1;
    private static int functionCallId = 1;

    //Two globals to track if label is encountered inside a function or not
    //If encountered inside function, label name is functionName$label
    //private static boolean inFunction = false;
    private static String funcName;

    /**
     * 
     * @param futures
     */
    public CodeWriter(Future<Map<String, Pair<String, String>>>[] futures) {
        this.parsedOutput = futures;
    }

    /**
     * 
     * @param asmFileName
     */
    public void setAsmFileName(String asmFileName) {
        this.fileName = removeExtension(asmFileName);
        this.asmFileName = this.fileName + ".asm";
    }

    /**
     * 
     * @return
     */
    public String getAsmFileName() {
        return asmFileName;
    }

    public void writeLines() throws Exception, IOException, InterruptedException, ExecutionException {
        FileWriter fw = new FileWriter(this.asmFileName,true);
        
        for(Future<Map<String, Pair<String, String>>> future : this.parsedOutput) {
            Map<String, Pair<String, String>> map = future.get();
            for(Map.Entry<String,Pair<String,String>>entry : map.entrySet()){
                currentClass = entry.getKey().replaceFirst("_.*", "");
                Pair<String,String> inputPair = entry.getValue();
                String cmdType = (String) inputPair.getValue0();
                String inputCommand = (String) inputPair.getValue1();
                String commandsToWrite = "";

                if (cmdType == "C_ARITHMETIC")
                    commandsToWrite = writeArithmetic(inputCommand);
                else if(cmdType == "C_PUSH" || cmdType == "C_POP")
                    commandsToWrite = writePushPop(cmdType, inputCommand);
                else if(cmdType == "C_LABEL")
                    commandsToWrite = writeLabel(inputCommand);
                else if(cmdType == "C_GOTO")
                    commandsToWrite = writeGoto(inputCommand);
                else if(cmdType == "C_IF")
                    commandsToWrite = writeIf(inputCommand);
                else if(cmdType == "C_FUNCTION")
                    commandsToWrite = writeFunction(inputCommand);
                else if(cmdType == "C_RETURN")
                    commandsToWrite = writeReturn(inputCommand);
                //commandType.equals("call")
                else    
                    commandsToWrite = writeCall(inputCommand);
                    
                fw.write(commandsToWrite);

            }
        }
        fw.flush();
        fw.close();

    }

    /**
     * 
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writeCall(String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();
        StringBuilder returnCommand = new StringBuilder();

        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }

        if(commandTokens.size() != 3)
            throw new Exception("Invalid call command in file"); 

        String functionCall = commandTokens.get(1);
        String returnLabel = functionCall + "_Return_" + functionCallId;
        String numArgs = commandTokens.get(2);
        functionCallId++;

        returnCommand.append("@" + returnLabel + "\nD=A\n@R0\nA=M\nM=D\n" + stackInc);
        returnCommand.append("@LCL\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        returnCommand.append("@ARG\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        returnCommand.append("@THIS\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        returnCommand.append("@THAT\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        returnCommand.append("@SP\nD=M\n@5\nD=D-A\n@" + numArgs + "\nD=D-A\n@ARG\nM=D\n");
        returnCommand.append("@SP\nD=M\n@LCL\nM=D\n");
        returnCommand.append("@" + functionCall + "\n0;JMP\n");
        returnCommand.append("(" + returnLabel + ")\n");

        return returnCommand.toString();
    }
    /**
     * 
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writeReturn(String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();
        StringBuilder returnCommand = new StringBuilder();

        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }

        if(commandTokens.size() != 1)
            throw new Exception("Invalid return command in file"); 

        returnCommand.append("@LCL\nD=M\n@R13\nM=D\n");
        returnCommand.append("@R13\nD=M\n@5\nD=D-A\nA=D\nD=M\n@R14\nM=D\n");
        returnCommand.append
        (stackDec + "@R0\nA=M\nD=M\n@ARG\nA=M\nM=D\n");
        returnCommand.append("@ARG\nD=M+1\n@SP\nM=D\n");
        returnCommand.append("@R13\nD=M\n@1\nD=D-A\nA=D\nD=M\n@THAT\nM=D\n");
        returnCommand.append("@R13\nD=M\n@2\nD=D-A\nA=D\nD=M\n@THIS\nM=D\n");
        returnCommand.append("@R13\nD=M\n@3\nD=D-A\nA=D\nD=M\n@ARG\nM=D\n");
        returnCommand.append("@R13\nD=M\n@4\nD=D-A\nA=D\nD=M\n@LCL\nM=D\n");
        returnCommand.append("@R14\nA=M\n0;JMP\n");

        //Set inFunction flag to false 
        //inFunction = false;
        return returnCommand.toString();
    }
    /**
     * 
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writeFunction(String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();
        StringBuilder returnCommand = new StringBuilder();

        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }

        if(commandTokens.size() != 3)
            throw new Exception("Invalid function command in file"); 

        String functionName = commandTokens.get(1);
        Integer localVarCount = Integer.parseInt(commandTokens.get(2));
        returnCommand.append("(" + functionName + ")\n");
        for(int i = 0; i < localVarCount; i++){
            //returnCommand.append("@R1\nD=M\n@" + i + "\nA=A+D\nD=A\n@R14\nM=D\n@0\nD=A\n@R14\nA=M\nM=D\n");
            returnCommand.append("@0\nD=A\n@R0\nA=M\nM=D\n" + stackInc);
        }

        //Set inFunction flag to true and store function name globally
        //inFunction = true;
        funcName = functionName;
        return returnCommand.toString();
    }
    /**
     * 
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writeIf(String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();
        StringBuilder returnCommand = new StringBuilder();

        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }

        if(commandTokens.size() != 2)
            throw new Exception("Invalid if-goto command in file"); 

        String label = commandTokens.get(1);

        //if(inFunc)
            returnCommand.append(stackDec + "@R0\nA=M\nD=M\n@" + funcName + "$" + label  + "\nD;JNE\n");
        /*else
            returnCommand.append(stackDec + "@R0\nA=M\nD=M\n@" + label + "\nD;JNE\n");*/
        
            return returnCommand.toString();
    }
    /**
     * 
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writeGoto(String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();
        StringBuilder returnCommand = new StringBuilder();

        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }

        if(commandTokens.size() != 2)
            throw new Exception("Invalid Goto command in file"); 

        String label = commandTokens.get(1);

        //if(inFunc)
            returnCommand.append("@" + funcName + "$" + label + "\n0;JMP\n");
        /*else
            returnCommand.append("@" + label + "\n0;JMP\n");*/
        
            return returnCommand.toString();
    }
    /**
     * 
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writeLabel(String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();

        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }

        if(commandTokens.size() != 2)
            throw new Exception("Invalid label command in file"); 
        //if(inFunc)
            return "(" + funcName + "$" + commandTokens.get(1) + ")\n";
        /*else
            return "(" + commandTokens.get(1) + ")\n";*/
    }

    /**
     * 
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writeArithmetic(String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();
        StringBuilder returnCommand = new StringBuilder();

        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }

        if(commandTokens.size() != 1)
            throw new Exception("Invalid arithmetic command in file"); 

        String function = commandTokens.get(0);

        //Binary operators
        if (function.equals("add"))
            returnCommand.append(stackDec+"A=M\nD=M\n"+stackDec+"A=M\nM=M+D\n"+stackInc);
            
        else if(function.equals("sub"))
            returnCommand.append(stackDec+"A=M\nD=M\n"+stackDec+"A=M\nM=M-D\n"+stackInc);
            
        else if(function.equals("eq")){
            String eqLabel = "@EQUALS_" + eqId + "\n";
            String notEqLabel = "@NOT_EQUALS_" + eqId + "\n";
            String eqBlock = "(EQUALS_" + eqId + ")\nD=-1\n";
            String notEqBlock = "(NOT_EQUALS_" + eqId + ")\n@R0\nA=M\nM=D\n";
            returnCommand.append(stackDec+"A=M\nD=M\n"+stackDec+
                                "A=M\nD=M-D\n" + eqLabel + "D;JEQ\nD=0\n" + notEqLabel + "0;JMP\n"
                                + eqBlock + notEqBlock + stackInc);
            eqId++;
        }

        else if(function.equals("gt")){
            String gtLabel = "@GREATER_" + gtId + "\n";
            String notGtLabel = "@NOT_GREATER_" + gtId + "\n";
            String gtBlock = "(GREATER_" + gtId + ")\nD=-1\n";
            String notGtBlock = "(NOT_GREATER_" + gtId + ")\n@R0\nA=M\nM=D\n";
            returnCommand.append(stackDec+"A=M\nD=M\n"+stackDec+
                                "A=M\nD=M-D\n" + gtLabel + "D;JGT\nD=0\n" + notGtLabel + "0;JMP\n"
                                + gtBlock + notGtBlock + stackInc);
            gtId++;
        }
        else if(function.equals("lt")){
            String ltLabel = "@LESSER_" + ltId + "\n";
            String notLtLabel = "@NOT_LESSER_" + ltId + "\n";
            String ltBlock = "(LESSER_" + ltId + ")\nD=-1\n";
            String notLtBlock = "(NOT_LESSER_" + ltId + ")\n@R0\nA=M\nM=D\n";
            returnCommand.append(stackDec+"A=M\nD=M\n"+stackDec+
                                "A=M\nD=M-D\n" + ltLabel + "D;JLT\nD=0\n" + notLtLabel + "0;JMP\n"
                                + ltBlock + notLtBlock + stackInc);
            ltId++;
        }
        else if(function.equals("and")){
            returnCommand.append(stackDec+"A=M\nD=M\n"+stackDec+ "A=M\nD=D&M\nM=D\n" + stackInc);
        }
        
        else if(function.equals("or")){
            returnCommand.append(stackDec+"A=M\nD=M\n"+stackDec+ "A=M\nD=D|M\nM=D\n" + stackInc);
        }
        
        //Unary operators
        else if(function.equals("neg"))
            returnCommand.append(stackDec+"A=M\nD=M\n@0\nD=A-D\n@R0\nA=M\nM=D\n"+stackInc);
        //not operator
        else
            returnCommand.append(stackDec+"A=M\nD=!M\nM=D\n"+stackInc);

        return returnCommand.toString();
        
    }

    /**
     * 
     * @param cmdType
     * @param inputCommand
     * @return
     * @throws Exception
     */
    private String writePushPop(String cmdType, String inputCommand) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(inputCommand);
        List<String> commandTokens = new ArrayList<>();
        StringBuilder returnCommand = new StringBuilder();
        while(tokenizer.hasMoreTokens()){
            commandTokens.add(tokenizer.nextToken());
        }
        if(commandTokens.size() != 3)
            throw new Exception("Invalid Push/Pop command in file"); 

        String segment = commandTokens.get(1);
        String index = commandTokens.get(2); 
        
        //Push case
        if(cmdType.equals("C_PUSH")){
            if(segment.equals("argument"))
                returnCommand.append("@R2\nD=M\n@" + index + "\nA=A+D\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
            else if(segment.equals("local"))
                returnCommand.append("@R1\nD=M\n@" + index + "\nA=A+D\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
            else if(segment.equals("static"))
                returnCommand.append("@" + currentClass + "." + index + "\nD=M\n@R0\nA=M\nM=D\n" + stackInc);

            else if(segment.equals("constant"))
                returnCommand.append("@"+index+"\nD=A\n@R0\nA=M\nM=D\n" + stackInc);
            
            else if(segment.equals("this"))
                returnCommand.append("@R3\nD=M\n@" + index + "\nA=A+D\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
            
            else if(segment.equals("that"))
                returnCommand.append("@R4\nD=M\n@" + index + "\nA=A+D\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
            
            else if(segment.equals("pointer"))
                returnCommand.append("@3\nD=A\n@" + index + "\nA=A+D\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
            //tmp segment
            else
                returnCommand.append("@5\nD=A\n@" + index + "\nA=A+D\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
         }
        //Pop case
        else{
            if(segment.equals("argument")){
                returnCommand.append
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@R2\nD=M\n@" + index + "\nA=A+D\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            }

            else if(segment.equals("local")){
                returnCommand.append
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@R1\nD=M\n@" + index + "\nA=A+D\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            }

            else if(segment.equals("static"))
                returnCommand.append
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@" + currentClass + "." + index + "\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            
            else if(segment.equals("this")){
                returnCommand.append
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@R3\nD=M\n@" + index + "\nA=A+D\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            }
            
            else if(segment.equals("that")){
                returnCommand.append
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@R4\nD=M\n@" + index + "\nA=A+D\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            }

            else if(segment.equals("pointer")){
                returnCommand.append
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@3\nD=A\n@" + index + "\nA=A+D\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            }
            //tmp segment
            else{
                returnCommand.append
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@5\nD=A\n@" + index + "\nA=A+D\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            }
        }
        return returnCommand.toString();
    }
    /**
     * Writes program initialization block
     * @throws IOException
     */
	public void writeInit() throws IOException {
        StringBuilder sysInit = new StringBuilder();
        sysInit.append("@256\nD=A\n@SP\nM=D\n");
        sysInit.append("@sysReturn\nD=A\n@R0\nA=M\nM=D\n" + stackInc);
        sysInit.append("@LCL\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        sysInit.append("@ARG\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        sysInit.append("@THIS\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        sysInit.append("@THAT\nD=M\n@R0\nA=M\nM=D\n" + stackInc);
        sysInit.append("@SP\nD=M\n@5\nD=D-A\n@ARG\nM=D\n");
        sysInit.append("@SP\nD=M\n@LCL\nM=D\n");
        sysInit.append("@Sys.init\n0;JMP\n");
        sysInit.append("(sysReturn)\n");

        FileWriter fw = new FileWriter(this.asmFileName);
        fw.write(sysInit.toString());
        fw.flush();
        fw.close();
	}

    /**
     * Writes program termination block after threads finish writing all lines
     * @throws IOException
     */
	public void terminateProgram() throws IOException {
        FileWriter fw = new FileWriter(this.asmFileName, true);
        fw.write(progTerminate);
        logger.info("Finished writing file");
        fw.flush();
        fw.close();
	}

}