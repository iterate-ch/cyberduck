package ch.cyberduck.core.brick;

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

import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import java.net.URI;

import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import static org.junit.Assert.*;

public class BrickApiKeyRevocationServiceTest {
    @Test
    public void testDeletedAndAlreadyInvalidKeysSucceed() throws Exception {
        for(final int status : new int[]{200, 204, 401, 404}) {
            final MockHttpTransport transport = new MockHttpTransport.Builder().setLowLevelHttpResponse(
                    new MockLowLevelHttpResponse().setStatusCode(status)).build();
            new BrickApiKeyRevocationService(transport).revoke(URI.create("https://customer.files.com/auth/oauth/token"), "api-key");
            assertEquals("https://customer.files.com/api/rest/v1/api_key", transport.getLowLevelHttpRequest().getUrl());
            assertEquals("api-key", transport.getLowLevelHttpRequest().getFirstHeaderValue("X-FilesAPI-Key"));
        }
    }

    @Test
    public void testUnexpectedResponsesAndRedirectsFail() {
        for(final int status : new int[]{302, 403, 500}) {
            final MockHttpTransport transport = new MockHttpTransport.Builder().setLowLevelHttpResponse(
                    new MockLowLevelHttpResponse().setStatusCode(status).addHeader("Location", "https://other.files.com/api_key")).build();
            final HttpResponseException failure = assertThrows(HttpResponseException.class, () ->
                    new BrickApiKeyRevocationService(transport).revoke(URI.create("https://customer.files.com"), "api-key"));
            assertEquals(status, failure.getStatusCode());
        }
    }
}
