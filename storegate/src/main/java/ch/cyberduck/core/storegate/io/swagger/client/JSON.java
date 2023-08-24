package ch.cyberduck.core.storegate.io.swagger.client;

import ch.cyberduck.core.jersey.CustomJacksonObjectMapper;

import javax.ws.rs.ext.ContextResolver;
import java.text.DateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class JSON implements ContextResolver<ObjectMapper> {
  private ObjectMapper mapper;

    public JSON() {
        mapper = new CustomJacksonObjectMapper();
        mapper.setDateFormat(new RFC3339DateFormat());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new JodaModule());
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
}
