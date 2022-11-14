package net.creeperhost.minetogether.lib.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * Created by covers1624 on 9/6/22.
 */
public class LoggerNameStartsWithFilter extends AbstractFilter {

    private final String pattern;

    public LoggerNameStartsWithFilter(String pattern, Result onMatch, Result onMissmatch) {
        super(onMatch, onMissmatch);
        this.pattern = pattern;
    }

    private Result filter(Logger logger) {
        return filter(logger.getName());
    }

    private Result filter(String logger) {
        boolean match = logger != null && logger.startsWith(pattern);
        return match ? onMatch : onMismatch;
    }

    //@formatter:off
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) { return filter(logger); }
    @Override public Result filter(LogEvent event) { return filter(event.getLoggerName()); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) { return filter(logger); }
    //@formatter:on

    @Override
    public String toString() {
        return "LoggerNameStartsWithFilter{" +
                "pattern='" + pattern + '\'' +
                '}';
    }
}
