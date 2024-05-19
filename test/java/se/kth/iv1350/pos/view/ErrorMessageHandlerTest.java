package test.java.se.kth.iv1350.pos.view;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.kth.iv1350.pos.view.ErrorMessageHandler;

public class ErrorMessageHandlerTest {
    private ByteArrayOutputStream outContent;
    private PrintStream originalSysOut;
    private ErrorMessageHandler instance;

    @Before
    public void setUp() {
        originalSysOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        instance = ErrorMessageHandler.getInstance();
    }

    @After
    public void tearDown() {
        outContent = null;
        System.setOut(originalSysOut);
        instance = null;
    }

    @Test
    public void testDisplayErrorMessage() {
        String errorMessage = "This is a test error message";
        instance.displayErrorMessage(errorMessage);
        String printout = outContent.toString();
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US));

        assertTrue("The error message is missing the '====== ERROR ======' line.", printout.contains("====== ERROR ======"));
        // 检查时间戳的日期部分，因为秒可能会有微小的时间差
        assertTrue("The error message is missing the timestamp.", printout.contains(currentDateTime.substring(0, 10))); 
        assertTrue("The error message is missing the 'ERROR: ' prefix.", printout.contains("ERROR: " + errorMessage));
        assertTrue("The error message is missing the '===================' line.", printout.contains("==================="));
    }
}
