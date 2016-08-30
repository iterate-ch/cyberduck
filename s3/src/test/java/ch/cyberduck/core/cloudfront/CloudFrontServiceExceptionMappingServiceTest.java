package ch.cyberduck.core.cloudfront;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.LoginFailureException;

import org.jets3t.service.CloudFrontServiceException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CloudFrontServiceExceptionMappingServiceTest {

    @Test
    public void testLoginFailure() throws Exception {
        final CloudFrontServiceException f = new CloudFrontServiceException(
                "message", 403, "type", "InvalidClientTokenId", "The security token included in the request is invalid.", "detail", "id45"
        );
        assertTrue(new CloudFrontServiceExceptionMappingService().map(f) instanceof LoginFailureException);
        assertEquals("Login failed", new CloudFrontServiceExceptionMappingService().map(f).getMessage());
        assertEquals("The security token included in the request is invalid. Detail. Please contact your web hosting service provider for assistance.", new CloudFrontServiceExceptionMappingService().map(f).getDetail());
    }

    @Test
    public void testCloudfrontException() {
        CloudFrontServiceException e = new CloudFrontServiceException("message",
                1, "type", "errorCode", "errorMessage", "errorDetail", "Id");
        assertEquals("ErrorMessage. ErrorDetail.", new CloudFrontServiceExceptionMappingService().map(e).getDetail());
    }

    @Test
    public void testNullValues() {
        CloudFrontServiceException e = new CloudFrontServiceException("message",
                1, "type", "", "errorMessage", "", "Id");
        assertEquals("ErrorMessage.", new CloudFrontServiceExceptionMappingService().map(e).getDetail());
    }
}
