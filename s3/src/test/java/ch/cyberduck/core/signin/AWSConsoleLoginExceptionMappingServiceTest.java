package ch.cyberduck.core.signin;

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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.signin.AWSConsoleLoginExceptionMappingService.ErrorResponseException;

import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AWSConsoleLoginExceptionMappingServiceTest {

    private final AWSConsoleLoginExceptionMappingService service = new AWSConsoleLoginExceptionMappingService();

    @Test
    public void testRefreshTokenExpired() {
        // Observed response with status 401 although documented as 400
        final BackgroundException e = service.map(new ErrorResponseException(401, "Unauthorized",
                "TOKEN_EXPIRED", "The refresh token has expired."));
        assertEquals(ExpiredTokenException.class, e.getClass());
        assertTrue(e instanceof LoginFailureException);
        assertTrue(e.getDetail(false).contains("The refresh token has expired"));
    }

    @Test
    public void testExpiredIndependentOfStatus() {
        assertEquals(ExpiredTokenException.class, service.map(new ErrorResponseException(400, "Bad Request",
                "TOKEN_EXPIRED", "The refresh token has expired.")).getClass());
    }

    @Test
    public void testAuthorizationCodeExpired() {
        assertEquals(ExpiredTokenException.class, service.map(new ErrorResponseException(401, "Unauthorized",
                "AUTHCODE_EXPIRED", "The authorization code has expired.")).getClass());
    }

    @Test
    public void testUserCredentialsChanged() {
        assertEquals(ExpiredTokenException.class, service.map(new ErrorResponseException(401, "Unauthorized",
                "USER_CREDENTIALS_CHANGED", "The user credentials have changed.")).getClass());
    }

    @Test
    public void testInsufficientPermissions() {
        final BackgroundException e = service.map(new ErrorResponseException(401, "Unauthorized",
                "INSUFFICIENT_PERMISSIONS", "Missing permission signin:CreateOAuth2Token."));
        assertEquals(AccessDeniedException.class, e.getClass());
        assertTrue(e.getDetail(false).contains("Missing permission"));
    }

    @Test
    public void testValidationError() {
        final BackgroundException e = service.map(new ErrorResponseException(400, "Bad Request",
                "INVALID_REQUEST", "Missing required parameter."));
        assertEquals(InteroperabilityException.class, e.getClass());
        assertTrue(e.getDetail(false).contains("Missing required parameter"));
    }

    @Test
    public void testRateLimited() {
        assertEquals(RetriableAccessDeniedException.class, service.map(new ErrorResponseException(429, "Too Many Requests",
                "INVALID_REQUEST", "Rate exceeded.")).getClass());
    }

    @Test
    public void testServerError() {
        assertEquals(RetriableAccessDeniedException.class, service.map(new ErrorResponseException(500, "Internal Server Error",
                "SERVER_ERROR", "Internal failure.")).getClass());
    }

    @Test
    public void testUnknownCodeFallsBackToStatus() {
        assertEquals(LoginFailureException.class, service.map(new ErrorResponseException(401, "Unauthorized",
                "SOMETHING_NEW", "Unexpected.")).getClass());
    }

    @Test
    public void testMissingBodyUsesReasonPhrase() {
        final BackgroundException e = service.map(new ErrorResponseException(401, "Unauthorized", null, null));
        assertEquals(LoginFailureException.class, e.getClass());
        assertTrue(e.getDetail(false).contains("Unauthorized"));
    }

    @Test
    public void testPlainHttpResponseException() {
        assertEquals(LoginFailureException.class,
                service.map(new HttpResponseException(401, "Unauthorized")).getClass());
    }
}
