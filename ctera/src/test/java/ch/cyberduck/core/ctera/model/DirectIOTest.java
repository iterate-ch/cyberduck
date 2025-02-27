package ch.cyberduck.core.ctera.model;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

public class DirectIOTest {

    @Test
    public void testParse() throws Exception {
        final String response = "{\n" +
                "    \"wrapped_key\": \"CeuGXbS1O0pK8S8jReooFaPhxWC02NemFwxhC36hyP/0yIAXV6D2khDSSoUl7+1I\",\n" +
                "    \"chunks\": [\n" +
                "        {\n" +
                "            \"url\": \"https://alexb-dcdirectio.s3.eu-west-1.amazonaws.com/blocks/0000000f/81eba990102c37a1f68da388f6cd19028fac2f68-1c?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250227T081312Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZYQPDYEO5UVSMPFE%2F20250227%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=9614791651e6b52c16639f728c62b85024776f51052cdcbbbc6cb06b61a7beab\",\n" +
                "            \"len\": 4194304\n" +
                "        },\n" +
                "        {\n" +
                "            \"url\": \"https://alexb-dcdirectio.s3.eu-west-1.amazonaws.com/blocks/0000000f/d0422bac1f3b9c07607788addbe96e1e2f9658cb-1d?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250227T081312Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZYQPDYEO5UVSMPFE%2F20250227%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=fa4ef3ff8b32e63b095d9ff1d5f5ed819ee9b70e67de2eb77a99647a6b7f8f32\",\n" +
                "            \"len\": 462848\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        final DirectIO directio = mapper.readValue(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)), DirectIO.class);
        assertEquals("CeuGXbS1O0pK8S8jReooFaPhxWC02NemFwxhC36hyP/0yIAXV6D2khDSSoUl7+1I", directio.wrapped_key);
        assertEquals(2, directio.chunks.size());
        assertEquals(4194304, directio.chunks.get(0).len);
        assertEquals(462848, directio.chunks.get(1).len);
        assertEquals("https://alexb-dcdirectio.s3.eu-west-1.amazonaws.com/blocks/0000000f/81eba990102c37a1f68da388f6cd19028fac2f68-1c?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250227T081312Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZYQPDYEO5UVSMPFE%2F20250227%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=9614791651e6b52c16639f728c62b85024776f51052cdcbbbc6cb06b61a7beab", directio.chunks.get(0).url);
        assertEquals("https://alexb-dcdirectio.s3.eu-west-1.amazonaws.com/blocks/0000000f/d0422bac1f3b9c07607788addbe96e1e2f9658cb-1d?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250227T081312Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZYQPDYEO5UVSMPFE%2F20250227%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=fa4ef3ff8b32e63b095d9ff1d5f5ed819ee9b70e67de2eb77a99647a6b7f8f32", directio.chunks.get(1).url);
    }
}