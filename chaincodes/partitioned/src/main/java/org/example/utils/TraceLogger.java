package org.example.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import org.example.exceptions.ExceptionLogger;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TraceLogger {

    private HashMap<String, String> traces = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private String module;
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'_'HH:mm:ss.SSSZ");

    public TraceLogger(String txId, String module) {
        this.module = module;
        traces.put("tx_id",txId);
        df.setTimeZone(tz);
    }

    public void putTrace(String traceId, String traceValue) {
        traces.put(traceId, traceValue);
    }

    public String getTraceValue(String traceId) {
        return traces.get(traceId);
    }

    public void updateTraceValue(String traceId, String newValue) {
        if (traceId.equals("tx_id")) {
            throw new IllegalArgumentException("Can't modify transaction id for the trace");
        }
        traces.replace(traceId, newValue);
    }

    public void finalizeTrace() {
        try {
            String msg = mapper.writeValueAsString(traces);
            StringBuilder builder = new StringBuilder(5000);
            builder.append(df.format(new Date(System.currentTimeMillis()))).append(" | ");
            builder.append("info").append(" | ");
            builder.append(module).append(" | ");
            builder.append(msg);
            builder.append("\n");
            System.err.println(builder.toString());
        } catch (JsonProcessingException e) {
            ExceptionLogger.log(LoggerFactory.getLogger(TraceLogger.class.getName()), e);
            throw new JSONException(e);
        }
    }

}
