package org.example.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'_'HH:mm:ss.SSSZ");

    public LogFormatter() {
        df.setTimeZone(tz);
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(df.format(new Date(record.getMillis()))).append(" | ");
        builder.append(record.getLevel().getName()).append(" [ ");
        builder.append(record.getSourceClassName()).append(".");
        builder.append(record.getSourceMethodName()).append(" ] ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }
}
