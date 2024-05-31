package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.test.IntegrationTest;

import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.apache.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CteraAuthenticationHandlerTest extends AbstractCteraTest {

    @Test
    public void retryRequest() {
        final ServiceUnavailableRetryStrategy handler =
                new CustomServiceUnavailableRetryStrategy(session.getHost(), 2,
                        new ExecutionCountServiceUnavailableRetryStrategy(1, new CteraAuthenticationHandler(session) {
                            @Override
                            public void authenticate() throws BackgroundException {
                                //
                            }
                        }));
        assertTrue(handler.retryRequest(
                new BasicHttpResponse(new BasicStatusLine(HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE, "Service Unavailable")),
                1, new BasicHttpContext()));
        assertTrue(handler.retryRequest(
                new BasicHttpResponse(new BasicStatusLine(HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE, "Service Unavailable")),
                2, new BasicHttpContext()));
        assertTrue(handler.retryRequest(
                new BasicHttpResponse(new BasicStatusLine(HTTP_1_1, HttpStatus.SC_MOVED_TEMPORARILY, "Service Unavailable")),
                1, new BasicHttpContext()));
        assertFalse(handler.retryRequest(
                new BasicHttpResponse(new BasicStatusLine(HTTP_1_1, HttpStatus.SC_MOVED_TEMPORARILY, "Service Unavailable")),
                2, new BasicHttpContext()));
    }
}