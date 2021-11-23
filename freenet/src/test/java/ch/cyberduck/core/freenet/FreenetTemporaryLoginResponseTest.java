package ch.cyberduck.core.freenet;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FreenetTemporaryLoginResponseTest {

    @Test
    public void testParse() throws Exception {
        final String response = "{\n" +
            " \"cid\": 720497664,\n" +
            " \"hash\": \"eyJhbGciOiJIUzUxMi\",\n" +
            " \"urls\": {\n" +
            "     \"check\": \"https://webmail.freenet.de/api/v2.0/hash/check/eyJhbGciOiJIUzUxMi\",\n" +
            "     \"login\": \"https://webmail.freenet.de/api/v2.0/hash/login/eyJhbGciOiJIUzUxMi\"\n" +
            "  }\n" +
            "}\n";
        final ObjectMapper mapper = new ObjectMapper();
        final FreenetTemporaryLoginResponse parsed = mapper.readValue(response, FreenetTemporaryLoginResponse.class);
        assertEquals("720497664", parsed.cid);
        assertEquals("eyJhbGciOiJIUzUxMi", parsed.hash);
        assertNotNull(parsed.urls);
        assertEquals("https://webmail.freenet.de/api/v2.0/hash/check/eyJhbGciOiJIUzUxMi", parsed.urls.check);
        assertEquals("https://webmail.freenet.de/api/v2.0/hash/login/eyJhbGciOiJIUzUxMi", parsed.urls.login);
    }
}
