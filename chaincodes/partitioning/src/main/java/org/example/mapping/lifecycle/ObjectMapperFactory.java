package org.example.mapping.lifecycle;

import java.util.logging.Logger;

import org.example.utils.LoggerFactory;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperFactory {

    private static ObjectMapperFactory omf;
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperFactory.class.getName());

    private ObjectMapperFactory() {
    }

    public static synchronized ObjectMapperFactory getInstance() {
        if (omf == null) {
            omf = new ObjectMapperFactory();
        }
        return omf;
    }

    public ObjectMapper createObjectMapper() {
        LOGGER.finest("get current ObjectMapper called");
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, Visibility.NONE); // turn off everything
        om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return om;
    }

}
