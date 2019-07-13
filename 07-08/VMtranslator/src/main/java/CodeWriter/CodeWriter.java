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

    //Globals for CodeWriter
    private static String stackInc = "@R0\nM=M+1\n";
    private static String stackDec = "@R0\nM=M-1\n";
    private static String progTerminate = "(INFINITE_LOOP)\n@INFINITE_LOOP\n0;JMP";
    private static int eqId = 1;
    private static int gtId = 1;
    private static int ltId = 1;

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
        FileWriter fw = new FileWriter(this.asmFileName);
        
        for(Future<Map<String, Pair<String, String>>> future : this.parsedOutput) {
            Map<String, Pair<String, String>> map = future.get();
            for(Map.Entry<String,Pair<String,String>>entry : map.entrySet()){
                Pair<String,String> inputPair = entry.getValue();
                String cmdType = (String) inputPair.getValue0();
                String inputCommand = (String) inputPair.getValue1();
                String commandsToWrite = "";

                if (cmdType == "C_ARITHMETIC")
                    commandsToWrite = writeArithmetic(inputCommand);
                else if(cmdType == "C_PUSH" || cmdType == "C_POP")
                    commandsToWrite = writePushPop(cmdType, inputCommand);
                //More types to be added in Chapter 8
                
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
                returnCommand.append("@" + this.fileName + "." + index + "\nD=M\n@R0\nA=M\nM=D\n" + stackInc);

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
                (stackDec + "@R0\nA=M\nD=M\n@R13\nM=D\n@" + this.fileName + "." + index + "\nD=A\n@R14\nM=D\n@R13\nD=M\n@R14\nA=M\nM=D\n");
            
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