package ch.cyberduck.core.eue;

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

import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EueExceptionMappingServiceTest {

    @Test
    public void testRetry() {
        final BackgroundException failure = new EueExceptionMappingService().map(new ApiException(429, "",
                Collections.singletonMap("Retry-After", Collections.singletonList("5")), ""));
        assertTrue(failure instanceof RetriableAccessDeniedException);
        assertEquals(5, ((RetriableAccessDeniedException) failure).getDelay().getSeconds());
    }

    @Test
    public void testEnhancedStatus() {
        final BackgroundException failure = new EueExceptionMappingService().map(new ApiException(404, "",
                Collections.singletonMap("X-UI-ENHANCED-STATUS", Collections.singletonList("NOT_FOUND")), ""));
        assertTrue(failure instanceof NotfoundException);
        assertEquals("NOT_FOUND. Please contact your web hosting service provider for assistance.", failure.getDetail());
    }

    @Test
    public void testParseError() {
        assertEquals("LIMIT_MAX_FOLDER_COUNT. LIMIT_MAX_RESOURCE_COUNT. Please contact your web hosting service provider for assistance.",
                new EueExceptionMappingService().map(new ApiException("LIMIT_MAX_FOLDER_COUNT,LIMIT_MAX_RESOURCE_COUNT", null, 500, null)).getDetail());
    }
}