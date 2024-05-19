package se.kth.iv1350.pos.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogHandler {
    private static final String LOG_FILE_NAME = "pos-application-log.txt";
    private static FileLogHandler instance;
    private PrintWriter logFile;

    private FileLogHandler() throws IOException {
        logFile = new PrintWriter(new FileWriter(LOG_FILE_NAME, true));
        System.out.println("FileLogHandler: Log file created/opened successfully.");
    }

    public static FileLogHandler getInstance() throws IOException {
        if (instance == null) {
            instance = new FileLogHandler();
        }
        return instance;
    }

    public void logException(Exception exception) {
        logFile.println(createLogMessage(exception));
        logFile.flush();
        System.out.println("FileLogHandler: Logging exception.");
    }

    private String createLogMessage(Exception exception) {
        StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append("Exception occurred: ");
        logMsgBuilder.append(exception.getMessage());
        logMsgBuilder.append(System.lineSeparator());
        logMsgBuilder.append("Time: ");
        logMsgBuilder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logMsgBuilder.append(System.lineSeparator());

        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            logMsgBuilder.append(stackTraceElement.toString());
            logMsgBuilder.append(System.lineSeparator());
        }

        return logMsgBuilder.toString();
    }

    public void closeLogFile() {
        if (logFile != null) {
            logFile.close();
        }
    }
}
