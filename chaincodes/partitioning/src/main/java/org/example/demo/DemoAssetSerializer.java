package org.example.demo;

import java.io.IOException;
import java.util.logging.Logger;

import org.example.utils.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class DemoAssetSerializer extends StdSerializer<DemoAsset> {
    private static Logger LOGGER = LoggerFactory.getLogger(DemoAssetSerializer.class.getName());

    public DemoAssetSerializer() {
        super(DemoAsset.class);
    }

    public DemoAssetSerializer(Class<DemoAsset> t) {
        super(t);
    }

    @Override
    public void serialize(DemoAsset value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        LOGGER.finest("Custom serializer serializing " + value.getUuid() + " asset");
        gen.writeStartObject();
        gen.writeStringField("uuid", value.getUuid());
        LOGGER.finest("Custom serializer wrote field uuid");
        // gen.writeFieldName("nestedAsset");
        if (value.pocket1 !=null) {
            gen.writeNumberField("pocket1", value.pocket1);
            LOGGER.finest("Custom serializer wrote field pocket1");
        }
        if (value.pocket23 !=null) {
            gen.writePOJOField("pocket23", value.pocket23);
            LOGGER.finest("Custom serializer wrote field pocket2");
        }
        gen.writeEndObject();
    }

}
