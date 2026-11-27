package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;

import static org.junit.Assert.assertTrue;

public class DriveExceptionMappingServiceTest {

    private static GoogleJsonResponseException failure(final String reason, final String message) {
        final GoogleJsonError.ErrorInfo info = new GoogleJsonError.ErrorInfo();
        info.setDomain("global");
        info.setReason(reason);
        info.setMessage(message);
        final GoogleJsonError details = new GoogleJsonError();
        details.setCode(403);
        details.setMessage(message);
        details.setErrors(Collections.singletonList(info));
        return new GoogleJsonResponseException(new HttpResponseException.Builder(403, "Forbidden", new HttpHeaders()), details);
    }

    @Test
    public void testPropertyLengthLimitExceeded() {
        final BackgroundException e = new DriveExceptionMappingService(new DriveFileIdProvider(new DriveSession(new Host(new DriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()))).map(failure("propertyLengthLimitExceeded",
                "Properties and app properties are limited to 124 bytes in UTF-8 encoding, counting both the key and the value."));
        assertTrue(e instanceof InteroperabilityException);
        assertTrue(e.getDetail(false), e.getDetail(false).contains("limited to 124 bytes"));
    }

    @Test
    public void testPropertyLengthLimitExceededWrapped() {
        // Error response wrapped by output stream
        final BackgroundException e = new DriveExceptionMappingService(new DriveFileIdProvider(new DriveSession(new Host(new DriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager())))
                .map(new IOException(failure("propertyLengthLimitExceeded", "Properties and app properties are limited to 124 bytes in UTF-8 encoding, counting both the key and the value.")));
        assertTrue(e instanceof InteroperabilityException);
    }

    @Test
    public void testPropertyLengthLimitExceededInContent() {
        // Error response with JSON error details in body
        final BackgroundException e = new DriveExceptionMappingService(new DriveFileIdProvider(new DriveSession(new Host(new DriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager())))
                .map(new HttpResponseException.Builder(403, "Forbidden", new HttpHeaders()).setContent("{\"error\": {\"code\": 403, " +
                        "\"errors\": [{\"domain\": \"global\", \"reason\": \"propertyLengthLimitExceeded\", " +
                        "\"message\": \"Properties and app properties are limited to 124 bytes in UTF-8 encoding, counting both the key and the value.\"}], " +
                        "\"message\": \"Properties and app properties are limited to 124 bytes in UTF-8 encoding, counting both the key and the value.\"}}").build());
        assertTrue(e instanceof InteroperabilityException);
        assertTrue(e.getDetail(false), e.getDetail(false).contains("limited to 124 bytes"));
    }

    @Test
    public void testForbidden() {
        assertTrue(new DriveExceptionMappingService(new DriveFileIdProvider(new DriveSession(new Host(new DriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()))).map(failure("insufficientFilePermissions",
                "The user does not have sufficient permissions for this file.")) instanceof AccessDeniedException);
    }
}
