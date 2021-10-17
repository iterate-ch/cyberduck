package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GmxcloudExceptionMappingServiceTest {

    @Test
    public void testRetry() {
        final BackgroundException failure = new GmxcloudExceptionMappingService().map(new ApiException(429, "",
                Collections.singletonMap("Retry-After", Collections.singletonList("5")), ""));
        assertTrue(failure instanceof RetriableAccessDeniedException);
        assertEquals(5, ((RetriableAccessDeniedException) failure).getDelay().getSeconds());
    }
}