package ch.cyberduck.core.azure.apache;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;

import static org.junit.Assert.assertEquals;

public class ApacheHttpResponseTest {

    @Test
    public void testBodyNotReadUntilSubscribed() {
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        response.setEntity(new InputStreamEntity(new ByteArrayInputStream("<xml/>".getBytes(StandardCharsets.UTF_8))));
        final ApacheHttpResponse r = new ApacheHttpResponse(new HttpRequest(HttpMethod.GET, "https://localhost/"), response);
        // Not consumed when only requested by the pipeline
        r.getBody();
        r.getBodyAsByteArray();
        assertEquals("<xml/>", r.getBodyAsString().block());
    }
}
