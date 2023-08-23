package net.creeperhost.minetogether.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.*;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.file.Path;

/**
 * Created by covers1624 on 9/6/22.
 */
public class Log4jUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Attaches some custom configured Log4j File loggers. These loggers are set up to behave identically
     * to the FTBApp.
     * <p>
     * Any logging event originating from a Logger starting with {@code net.creeperhost.minetogether} will
     * be written to these files.
     * <p>
     * Files:<br>
     * {@code $logsFolder/minetogether/latest.log} -> INFO <br>
     * {@code $logsFolder/minetogether/debug.log} -> ALL
     *
     * @param logsFolder The root log folder.
     */
    public static void attachMTLogs(Path logsFolder) {
        String logsPath = logsFolder.toString();
        if (!logsPath.endsWith("/")) {
            logsPath += "/";
        }
        logsPath += "minetogether/";

        try {
            LoggerContext ctx = LoggerContext.getContext(false);
            Configuration configuration = ctx.getConfiguration();

            Appender latest = RollingRandomAccessFileAppender.newBuilder()
                    .withName("MT-Latest")
                    .withFilePattern(logsPath + "%d{yyyy-MM-dd}-%i.log.gz")
                    .withFileName(logsPath + "latest.log")
                    .withLayout(PatternLayout.newBuilder()
                            .withPattern("[%d{HH:mm:ss}] [%t/%level] [%logger]: %msg%n")
                            .build()
                    )
                    .withPolicy(CompositeTriggeringPolicy.createPolicy(
                            TimeBasedTriggeringPolicy.createPolicy(null, null),
                            OnStartupTriggeringPolicy.createPolicy(1)
                    ))
                    .build();

            Appender debug = RollingRandomAccessFileAppender.newBuilder()
                    .withName("MT-Debug")
                    .withFilePattern(logsPath + "debug-%i.log.gz")
                    .withFileName(logsPath + "debug.log")
                    .withLayout(PatternLayout.newBuilder()
                            .withPattern("[%d{HH:mm:ss.SSS}] [%t/%level] [%logger]: %msg%n")
                            .build()
                    )
                    .withPolicy(CompositeTriggeringPolicy.createPolicy(
                            OnStartupTriggeringPolicy.createPolicy(1),
                            SizeBasedTriggeringPolicy.createPolicy("200MB")
                    ))
                    // TODO
//                    .withStrategy(DefaultRolloverStrategy.newBuilder()
//                            .withMax("5")
//                            .withFileIndex("min")
//                            .build()
//                    )
                    .build();

            latest.start();
            debug.start();
            configuration.addAppender(latest);
            configuration.addAppender(debug);
            configuration.getRootLogger().addAppender(latest, Level.INFO, new LoggerNameStartsWithFilter("net.creeperhost.minetogether", Result.ACCEPT, Result.DENY));
            configuration.getRootLogger().addAppender(debug, Level.ALL, new LoggerNameStartsWithFilter("net.creeperhost.minetogether", Result.ACCEPT, Result.DENY));
            ctx.updateLoggers();
        } catch (Throwable ex) {
            LOGGER.error("Unable to configure MineTogether Logging.", ex);
        }
    }
}
