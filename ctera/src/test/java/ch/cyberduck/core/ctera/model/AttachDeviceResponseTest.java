package ch.cyberduck.core.ctera.model;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;

public class AttachDeviceResponseTest {

    @Test
    public void testParse() throws Exception {
        final String response = "{\n" +
            "    \"$class\":\"AttachDeviceRespond\",\n" +
            "    \"deviceName\": \"Test-device\",\n" +
            "    \"deviceUID\": 5986,\n" +
            "    \"lastLogin\": \"2021-02-03T12:03:00\",\n" +
            "    \"sharedSecret\": \"asdf\"\n" +
            " }\n";
        ObjectMapper mapper = new ObjectMapper();
        final AttachDeviceResponse object = mapper.readValue(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)), AttachDeviceResponse.class);
        assertEquals("Test-device", object.deviceName);
        assertEquals("5986", object.deviceUID);
        assertEquals("asdf", object.sharedSecret);
    }
}
