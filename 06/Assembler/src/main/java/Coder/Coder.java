package Coder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Coder {
    //Don't let anyone instantiate this class
    private Coder() {}

    //Initialize static maps
    private static final Map<String, String> compMap;
    static{
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("0", "0101010");
        tempMap.put("1", "0111111");
        tempMap.put("-1", "0111010");
        tempMap.put("D", "0001100");
        tempMap.put("A", "0110000");
        tempMap.put("!D", "0001101");
        tempMap.put("!A", "0110011");
        tempMap.put("D+1", "0011111");
        tempMap.put("A+1", "0110111");
        tempMap.put("D-1", "0001110");
        tempMap.put("A-1", "0110010");
        tempMap.put("D+A", "0000010");
        tempMap.put("D-A", "0010011");
        tempMap.put("A-D", "0000111");
        tempMap.put("D&A", "0000000");
        tempMap.put("D|A", "0010101");
        tempMap.put("M", "1110000");
        tempMap.put("!M", "1110001");
        tempMap.put("-M", "1110011");
        tempMap.put("M+1", "1110111");
        tempMap.put("M-1", "1110010");
        tempMap.put("D+M", "1000010");
        tempMap.put("D-M", "1010011");
        tempMap.put("M-D", "1000111");
        tempMap.put("D&M", "1000000");
        tempMap.put("D|M", "1010101");
        compMap = Collections.unmodifiableMap(tempMap);
    }
    private static final Map<String, String> destMap;
    static{
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("", "000");
        tempMap.put("M", "001");
        tempMap.put("D", "010");
        tempMap.put("MD", "011");
        tempMap.put("A", "100");
        tempMap.put("AM", "101");
        tempMap.put("AD", "110");
        tempMap.put("AMD", "111");
        destMap = Collections.unmodifiableMap(tempMap);
    }
    private static final Map<String, String> jumpMap;
    static{
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("", "000");
        tempMap.put("JGT", "001");
        tempMap.put("JEQ", "010");
        tempMap.put("JGE", "011");
        tempMap.put("JLT", "100");
        tempMap.put("JNE", "101");
        tempMap.put("JLE", "110");
        tempMap.put("JMP", "111");
        jumpMap = Collections.unmodifiableMap(tempMap);
    }
    
    /**
     * Determines dest mneumonic of C command
     * 
     * @return String
     * @param cIns
     */
	public static String dest(String cIns) {
        if(cIns.matches(".*=.*"))
            return destMap.get((cIns.substring(0, cIns.indexOf("="))).trim());
        else{
            return destMap.get("");
        }
	}

    /**
     * Determines comp mneumonic of C command
     * 
     * @return String
     * @param cIns
     */
	public static String comp(String cIns) {
        if(cIns.matches(".*=.*"))
            return compMap.get((cIns.substring(cIns.indexOf("=") + 1)).trim());
        else    
            return compMap.get((cIns.substring(0, cIns.indexOf(";"))).trim());
	}

    /**
     * Determines jump mneumonic of C command
     * 
     * @return String
     * @param cIns
     */
	public static String jump(String cIns) {
        if(cIns.matches(".*;.*"))
            return jumpMap.get((cIns.substring(cIns.indexOf(";") + 1)).trim());
        else
            return jumpMap.get("");
	}

}