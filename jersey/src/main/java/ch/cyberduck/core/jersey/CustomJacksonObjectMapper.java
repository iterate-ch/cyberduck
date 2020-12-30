package ch.cyberduck.core.jersey;/*

 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CustomJacksonObjectMapper extends ObjectMapper {

    public CustomJacksonObjectMapper() {
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        this.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        this.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

    @Override
    public ObjectMapper findAndRegisterModules() {
        return this;
    }
}
