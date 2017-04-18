package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import ch.iterate.openstack.swift.exception.GenericException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SwiftExceptionMappingServiceTest {

    @Test
    public void testLoginFailure() throws Exception {
        final GenericException f = new GenericException(
                "message", new Header[]{}, new BasicStatusLine(new ProtocolVersion("http", 1, 1), 403, "Forbidden"));
        assertTrue(new SwiftExceptionMappingService().map(f) instanceof AccessDeniedException);
        assertEquals("Access denied", new SwiftExceptionMappingService().map(f).getMessage());
        assertEquals("Message. 403 Forbidden. Please contact your web hosting service provider for assistance.", new SwiftExceptionMappingService().map(f).getDetail());
    }

    @Test
    public void testMap() throws Exception {
        assertEquals("Message. 500 reason. Please contact your web hosting service provider for assistance.", new SwiftExceptionMappingService().map(
                new GenericException("message", null, new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public int getStatusCode() {
                        return 500;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return "reason";
                    }
                })).getDetail());
    }
}
