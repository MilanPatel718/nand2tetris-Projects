package Assembler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import Assembler.Assembler;
import ch.qos.logback.core.util.FileUtil;
import junitparams.*;

import static org.junit.Assert.*;

import org.junit.Before;

import static Assembler.Assembler.logger;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(JUnitParamsRunner.class)
public class AssemblerTest {

    Assembler assembler;

    @Before
    public void setup() {
        assembler = new Assembler();
        logger = LoggerFactory.getLogger(Assembler.class);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @Parameters({ "10,0000000000001010", "32767,0111111111111111" })
    public void getCorrectbinaryTest(String decimalVal, String binaryReturn) throws Exception {
        // Assembler assembler = new Assembler();
        assertThat("Returns valid 16 bit binary", assembler.getBinary(decimalVal), is(binaryReturn));
    }

    @Test
    @Parameters({ "-25", "32768" })
    public void getBinaryThrowsExceptionTest(String decimalSymbol) throws Exception {
        exception.expect(Exception.class);
        exception.expectMessage("Decimal must be >=0 and < 32768");
        assembler.getBinary(decimalSymbol);

    }

    @Test
    @Parameters({ "add.hack" })
    public void testRunThrowsExceptionForInvalidFile(String assemblyFile) throws IndexOutOfBoundsException, Exception {
        exception.expect(Exception.class);
        exception.expectMessage("Please input a valid .asm file");
        assembler.run(assemblyFile);
    }

    @Test
    @Parameters({ "add.asm" })
    public void testRunThrowsIOExceptionForMissingFile(String assemblyFile)
            throws IndexOutOfBoundsException, Exception {
        exception.expect(IOException.class);
        assembler.run(assemblyFile);
    }

    @Test
    @Parameters({ "100,true", "abc,false", "2ac,false", "1000000,true", "test@email.com,false" })
    public void testIsNumeric(String inputString, boolean valid) {
        assertThat(Assembler.isNumeric(inputString), is(valid));
    }

    @Test
    @Parameters({ "/Pong.asm" })
    public void testRun(String assemblyFile) throws IOException, Exception {
        /*
         * URL url = this.getClass().getResource(assemblyFile); String assemblyFilePath
         * = url.toString();
         * assembler.run(assemblyFilePath.substring(assemblyFilePath.indexOf(":") + 1));
         */
        File resourcesDirectory = new File("src/test/resources" + assemblyFile);
        final File expected = new File(assemblyFile.substring(1, assemblyFile.indexOf(".")) + "_Expected" + ".hack");
        final File actual = new File(assemblyFile.substring(1, assemblyFile.indexOf(".")) + ".hack");
        assembler.run(resourcesDirectory.getAbsolutePath());
        assertThat("Files Should Match", readLines(actual, "UTF-8"), is(readLines(expected, "UTF-8")));
    }
    
}
