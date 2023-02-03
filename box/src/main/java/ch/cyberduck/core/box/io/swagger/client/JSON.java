package ch.cyberduck.core.box.io.swagger.client;

import ch.cyberduck.core.jersey.CustomJacksonObjectMapper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.ws.rs.ext.ContextResolver;
import java.io.IOException;
import java.text.DateFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JSON implements ContextResolver<ObjectMapper> {
  private ObjectMapper mapper;

  public JSON() {
    mapper = new CustomJacksonObjectMapper();
    mapper.setDateFormat(new RFC3339DateFormat());
    mapper.registerModule(new JodaModule());
    SimpleModule module = new SimpleModule();
    module.addSerializer(DateTime.class, new DateTimeSerializer());
    mapper.registerModule(module);
  }

  /**
   * Set the date format for JSON (de)serialization with Date properties.
   * @param dateFormat Date format
   */
  public void setDateFormat(DateFormat dateFormat) {
    mapper.setDateFormat(dateFormat);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return mapper;
  }

  private static final class DateTimeSerializer extends StdSerializer<DateTime> {
    private static final String YYYY_MM_DD_T_HH_MM_SS_ZZ = "yyyy-MM-dd'T'HH:mm:ssZZ";

    public DateTimeSerializer() {
      this(null);
    }

    public DateTimeSerializer(Class<DateTime> t) {
      super(t);
    }

    @Override
    public void serialize(DateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
      jsonGenerator.writeString(DateTimeFormat.forPattern(YYYY_MM_DD_T_HH_MM_SS_ZZ).print(dateTime));
    }
  }
}
