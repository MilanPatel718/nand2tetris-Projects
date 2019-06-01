package Assembler;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        logger = LoggerFactory.getLogger(Assembler.class);

        // Parse input program name
        String assemblyFile = args[0];
        Assembler assembler = new Assembler();
        assembler.run(assemblyFile);
    }

    protected void run(String assemblyFile) throws IOException {
        String hackFile;

        // Validate user input
        if (assemblyFile.matches(".*" + assemblyExt))
            hackFile = assemblyFile.substring(0, assemblyFile.indexOf(".")) + hackExt;
        else {
            logger.debug("Invalid file input");
            throw new IOException("Please input a valid .asm file");
        }

        // Create parser and file writer
        Parser parser = new Parser(assemblyFile);
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
        //Close new file
        fw.close();
    }

    /**
     * Returns 16 digit binary translation of decimal symbol
     * 
     * @param decimalSymbol
     * @return
     */
    private String getBinary(String decimalSymbol) {
        Integer decimal = Integer.parseInt(decimalSymbol);
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
