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
import static org.junit.Assert.assertTrue;

public class DirectIOTest {

    @Test
    public void testParse() throws Exception {
        final String response = "{\n" +
                "    \"encrypt_info\": {\n" +
                "            \"wrapped_key\": \"mWPomzFfgsRg0xYHV4qwN6vQhBotAKK8d7nHUvhvFi7RWLM8MY/JNk7VH4z/793B\",\n" +
                "            \"data_encrypted\": true\n" +
                "    },\n" +
                "    \"actual_blocks_range\": {\n" +
                "        \"file_size\": 4071685,\n" +
                "        \"range\": \"1048576-2097152\"\n" +
                "    },\n" +
                "    \"chunks\" : [\n" +
                "        {\n" +
                "        \"url\": \"https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/1460a01b6d372268897cf7247bb74af26027e070-1q?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86399&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=f85f12b0ebac60c51b5bb3ac7406a8937d286c29e7d064a7749ad369da3e3230\",\n" +
                "        \"len\": 262144\n" +
                "        },\n" +
                "        {\n" +
                "        \"url\": \"https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/f6e13a35a9cc3fd9286b26faa2702cc6c042403b-1t?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=5157fe8fce895d5159cd7be6410976ab5f8aaa536bd88a99527c08209fd396dc\",\n" +
                "        \"len\": 262144\n" +
                "        },\n" +
                "        {\n" +
                "        \"url\": \"https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/392193f6f81aca7d6599603eb48085767d7887a2-1r?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=5b01760bf578f2867c381fa7908bc6dca76ad84e254e8bb2f79e9459cf734bbc\",\n" +
                "        \"len\": 262144\n" +
                "        },\n" +
                "        {\n" +
                "        \"url\": \"https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/65b55099ec0c13b13d5844b2ab7b3d3a19fb54df-1s?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=10f67947fcedbc5f562a7a368f785fd52830de9592ab9988d67ce5fa8efd4153\",\n" +
                "        \"len\": 262144\n" +
                "        }\n" +
                "    ] \n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        final DirectIO directio = mapper.readValue(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)), DirectIO.class);
        assertEquals("mWPomzFfgsRg0xYHV4qwN6vQhBotAKK8d7nHUvhvFi7RWLM8MY/JNk7VH4z/793B", directio.encrypt_info.wrapped_key);
        assertTrue(directio.encrypt_info.data_encrypted);
        assertEquals(4, directio.chunks.size());
        assertEquals(262144, directio.chunks.get(0).len);
        assertEquals(262144, directio.chunks.get(1).len);
        assertEquals(262144, directio.chunks.get(2).len);
        assertEquals(262144, directio.chunks.get(3).len);
        assertEquals("https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/1460a01b6d372268897cf7247bb74af26027e070-1q?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86399&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=f85f12b0ebac60c51b5bb3ac7406a8937d286c29e7d064a7749ad369da3e3230", directio.chunks.get(0).url);
        assertEquals("https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/f6e13a35a9cc3fd9286b26faa2702cc6c042403b-1t?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=5157fe8fce895d5159cd7be6410976ab5f8aaa536bd88a99527c08209fd396dc", directio.chunks.get(1).url);
        assertEquals("https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/392193f6f81aca7d6599603eb48085767d7887a2-1r?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=5b01760bf578f2867c381fa7908bc6dca76ad84e254e8bb2f79e9459cf734bbc", directio.chunks.get(2).url);
        assertEquals("https://noa-media.s3.eu-west-1.amazonaws.com/blocks/00000012/65b55099ec0c13b13d5844b2ab7b3d3a19fb54df-1s?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240815T115508Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAZ3NHRJ7YVOIYLQXM%2F20240815%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=10f67947fcedbc5f562a7a368f785fd52830de9592ab9988d67ce5fa8efd4153", directio.chunks.get(3).url);
    }
}