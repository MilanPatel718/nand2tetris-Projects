package Assembler;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Parser.Parser;
import SymbolTable.SymbolTable;

public final class Assembler {
    public static Logger logger;
    private final String assemblyExt = ".asm";
    private final String hackExt = ".hack";

    protected Assembler() {
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception, IndexOutOfBoundsException {
        logger = LoggerFactory.getLogger(Assembler.class);
        String assemblyFile;
        // Parse input program name
        try{
            assemblyFile = args[0];
        }
        catch(IndexOutOfBoundsException e){
            logger.debug("Assembler arguments empty, please input a file name");
            throw e;
        }
        Assembler assembler = new Assembler();
        assembler.run(assemblyFile);
    }

    protected void run(String assemblyFilePath) throws Exception, IOException {
        String hackFile;
        String assemblyFile = Paths.get(new URI(assemblyFilePath).getPath()).getFileName().toString();
        // Validate user input
        if (assemblyFile.matches(".*" + assemblyExt))
            hackFile = assemblyFile.substring(0, assemblyFile.indexOf(".")) + hackExt;
        else {
            logger.debug("Invalid file input");
            throw new Exception("Please input a valid .asm file");
        }
        
        //Open File, trim and parse relevant commands
        List<String> commands;
        try {
            commands = Files.lines(Paths.get(assemblyFilePath)).map(s -> s.replaceAll("//.*", "")).map(s -> s.trim()).filter(s -> !s.isEmpty())
                            .filter(s->!s.matches("//.*")).collect(Collectors.toList());
        } catch (IOException e) {
            logger.debug("Input file could not be read because " + e.getMessage());
            throw e;
        }

        // Create parser and file writer
        Parser parser = new Parser(commands);
        SymbolTable st = new SymbolTable();
        FileWriter fw = new FileWriter(hackFile);

        // First pass assembler run
        int romAddress = 0;
        Hashtable<String, Integer> symbolTable = st.getSymbolTable();
        for (String currCommand : parser.getCommands()) {
            if(currCommand.matches("[(].*[)]")){
               symbolTable.put(currCommand.substring(1, currCommand.length() - 1), romAddress);
            }
            else{
                romAddress++;
            }
        }

        //Second pass assembler run
        Integer ramAddress = 16;
        for (String currCommand : parser.getCommands()){
            StringBuilder binaryCommand = new StringBuilder();

            //Determine command type
            String cmdType = parser.commandType(currCommand);

            //A Command Processing
            if(cmdType.equals("A_COMMAND")){
                String decimalSymbol = parser.symbol(currCommand, cmdType);

                //Checks first to see if symbol is numeric, then checks symbol table
                if(!isNumeric(decimalSymbol)){
                    if(symbolTable.containsKey(decimalSymbol)){
                        decimalSymbol = symbolTable.get(decimalSymbol).toString();
                    }
                    else{
                        symbolTable.put(decimalSymbol, ramAddress);
                        decimalSymbol = ramAddress.toString();
                        ramAddress++;
                    }
                }
                String binarySymbol = getBinary(decimalSymbol);
                binaryCommand.append(binarySymbol);
            }
            //C Command Processing
            if(cmdType.equals("C_COMMAND")){
                binaryCommand.append("111");
                binaryCommand.append(parser.comp(currCommand));
                binaryCommand.append(parser.dest(currCommand));
                binaryCommand.append(parser.jump(currCommand));
            }
            //Write new binaryCommand to file
            if(cmdType.equals("A_COMMAND") || cmdType.equals("C_COMMAND")){
                logger.info("Machine instruction is " + binaryCommand.toString() + " for assembly instruction " + currCommand);
                fw.write(binaryCommand.toString() + "\n");
            }
        }
        //Close new file and flush file writer
        fw.flush();
        fw.close();
    }

    /**
     * Returns 16 digit binary translation of decimal symbol
     * 
     * @param decimalSymbol
     * @return
     * @throws Exception
     */
    protected String getBinary(String decimalSymbol) throws Exception {
        Integer decimal = Integer.parseInt(decimalSymbol);
        if(decimal > 32767 || decimal < 0)
            throw new Exception("Decimal must be >=0 and < 32768");
            
        StringBuilder binaryBuilder = new StringBuilder();
        binaryBuilder.append("0");
        List<Integer> significantBits = new ArrayList<>();

        //Determine all bit positions that will be 1
        while(decimal != 0){
            int power = (int) (Math.log(decimal) / Math.log(2));
            significantBits.add(power);
            decimal = decimal - ((int) Math.pow(2, power));
        }

        //Build 16 bit insturction
        for(int i = 14; i >= 0; i--){
            if(significantBits.contains(i))
                binaryBuilder.append("1");
            else
                binaryBuilder.append("0");
        }
        return binaryBuilder.toString();
    }

    /**
     * Checks to see if symbol is numeric or not for an A command
     * 
     * @param symbol
     * @return
     */
    public static boolean isNumeric(String symbol){
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(symbol,pos);
        return symbol.length() == pos.getIndex();
     }
}
