package Assembler;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import Assembler.Assembler;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class AssemblerTest {

    //Test Parameters
    @Parameter
    public String decimalVal;
    @Parameter(1)
    public String binaryReturn;

    //Creates test data
    @Parameters
    public static Collection<Object[]> data(){
        Object[][] data = new Object[][] 
        {{"10", "0000000000001010"}, {"32767", "0111111111111111"}, {"35000", "Too large, will throw exception"},
         {"-25", "Negative addresses not allowed"}};

        return Arrays.asList(data);
    }
    
    @Test
    public void getBinaryTest() {
        Assembler assembler = new Assembler();
        try{
            assertThat("Returns valid 16 bit binary", assembler.getBinary(decimalVal), is(binaryReturn));
        }
        catch(Exception e){
            assertEquals("Decimal values > 32767 throw exception", "Decimal must be >=0 and < 32768" , e.getMessage());
        }
        
    }

    


}
