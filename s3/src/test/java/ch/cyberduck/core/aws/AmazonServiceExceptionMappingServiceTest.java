package ch.cyberduck.core.aws;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginFailureException;

import org.junit.Test;

import com.amazonaws.AmazonServiceException;

import static org.junit.Assert.assertTrue;

public class AmazonServiceExceptionMappingServiceTest {

    @Test
    public void testLoginFailure() {
        final AmazonServiceException f = new AmazonServiceException("message", null);
        f.setStatusCode(401);
        assertTrue(new AmazonServiceExceptionMappingService().map(f) instanceof LoginFailureException);
    }

    @Test
    public void testAccessFailure() {
        final AmazonServiceException f = new AmazonServiceException("message", null);
        f.setStatusCode(403);
        f.setErrorCode("AccessDenied");
        assertTrue(new AmazonServiceExceptionMappingService().map(f) instanceof AccessDeniedException);
        f.setErrorCode("SignatureDoesNotMatch");
        assertTrue(new AmazonServiceExceptionMappingService().map(f) instanceof LoginFailureException);
    }
}
