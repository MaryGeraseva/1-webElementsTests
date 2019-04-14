package common;

import org.apache.log4j.*;
import org.testng.ITestContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class LogInstance {

    private static ThreadLocal<Logger> log = new ThreadLocal<>();
    private static ConcurrentHashMap<String, Integer> testCaseIdList = new ConcurrentHashMap<>();
    private static volatile int loggerId = 0;

    private LogInstance() {
    }

    public static synchronized Logger getLogger() {
        if (log.get() == null) {
            LogManager.getLogger("Logger");
        }
        return log.get();
    }

    public static synchronized Logger setContext(ITestContext context, Method method) {
        Thread.currentThread().setName(method.getName());
        Logger logger = LogManager.getLogger(String.format("- [%d] - %s", loggerId++, context.getCurrentXmlTest().getName()));
        logger.addAppender(appenderConfig(context));
        log.set(logger);
        return log.get();
    }

    public static void resetLog() {
        log.set(null);
    }

    private static FileAppender appenderConfig(ITestContext context) {

        Integer testCaseId;

        if (!testCaseIdList.containsKey(context.getCurrentXmlTest().getName())) {
            testCaseId = 1;
            testCaseIdList.put(context.getCurrentXmlTest().getName(), testCaseId);
        } else {
            testCaseId = testCaseIdList.get(context.getCurrentXmlTest().getName());
            ++testCaseId;
            testCaseIdList.put(context.getCurrentXmlTest().getName(), testCaseId);
        }

        String path = String.format("%s\\target\\logs\\methods\\%s\\%d#%s.log",
                    System.getProperty("user.dir"), context.getCurrentXmlTest().getName(), testCaseId, Thread.currentThread().getName());


        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileAppender appender = new FileAppender();
        appender.setFile(path);
        appender.setLayout(new EnhancedPatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1} - %m%n"));
        appender.setThreshold(Level.TRACE);
        appender.setAppend(false);
        appender.activateOptions();
        return appender;
    }

    public static Integer getTestCaseId(ITestContext context) {
        return testCaseIdList.get(context.getCurrentXmlTest().getName());
    }
}
