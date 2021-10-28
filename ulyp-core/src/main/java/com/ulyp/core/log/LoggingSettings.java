package com.ulyp.core.log;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides instant access to enabled logging levels
 */
@SuppressWarnings("all")
public class LoggingSettings {

    public static final String LOG_LEVEL_PROPERTY = "ulyp.org.slf4j.simpleLogger.defaultLogLevel";
    public static final boolean TRACE_ENABLED;
    public static final boolean DEBUG_ENABLED;
    public static final boolean INFO_ENABLED;
    public static final boolean ERROR_ENABLED;

    static {
        if (System.getProperty(LOG_LEVEL_PROPERTY) == null) {
            System.setProperty(LOG_LEVEL_PROPERTY, "OFF");
        }

        Logger logger = LoggerFactory.getLogger(LoggingSettings.class);

        TRACE_ENABLED = logger.isTraceEnabled();
        DEBUG_ENABLED = logger.isDebugEnabled();
        INFO_ENABLED = logger.isInfoEnabled();
        ERROR_ENABLED = logger.isErrorEnabled();
    }

    public static String getLoggingLevel() {
        return System.getProperty("ulyp.org.slf4j.simpleLogger.defaultLogLevel");
    }
}
