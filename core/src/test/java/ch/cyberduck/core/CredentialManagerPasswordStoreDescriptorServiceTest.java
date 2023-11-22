package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CredentialManagerPasswordStoreDescriptorServiceTest {

    @Test
    public void testDefaultPrefix() {
        final CredentialManagerPasswordStoreDescriptorService s = new CredentialManagerPasswordStoreDescriptorService();
        assertEquals("test", s.getDescriptor(new Host(new TestProtocol(), "h", new Credentials().withUsername("u"))));
        assertEquals("h", s.getHostname(new Host(new TestProtocol(), "h", new Credentials().withUsername("u"))));
        assertEquals(Scheme.s3, s.getScheme(new Host(new TestProtocol(Scheme.s3), "h", new Credentials().withUsername("u"))));
        assertNull(s.getPort(new Host(new TestProtocol(Scheme.s3), "h", new Credentials().withUsername("u"))));
        assertEquals(999, s.getPort(new Host(new TestProtocol(Scheme.s3), "h", 999, new Credentials().withUsername("u"))), 0);
        assertEquals("test", s.getDescriptor(new Host(new TestProtocol(Scheme.s3), "h", new Credentials())));
    }

    @Test
    public void testOAuth() {
        final CredentialManagerPasswordStoreDescriptorService s = new CredentialManagerPasswordStoreDescriptorService();
        assertEquals("test", s.getDescriptor(new Host(new TestProtocol(Scheme.s3), "h", new Credentials().withOauth(OAuthTokens.EMPTY))));
        assertEquals("test", s.getDescriptor(new Host(new TestProtocol(Scheme.s3) {
            @Override
            public String getOAuthTokenUrl() {
                return "https://login/token";
            }
        }, "h", new Credentials().withOauth(OAuthTokens.EMPTY))));
        assertEquals("test", s.getDescriptor(new Host(new TestProtocol(Scheme.s3) {
            @Override
            public String getOAuthTokenUrl() {
                return "https://login/token";
            }
        }, "h", new Credentials().withOauth(OAuthTokens.EMPTY))));
        assertEquals("test", s.getDescriptor(new Host(new TestProtocol(Scheme.s3) {
            @Override
            public String getOAuthTokenUrl() {
                return "https://login/token";
            }
        }, "h", new Credentials().withOauth(new OAuthTokens("a", "r", -1L)))));
        assertNull(s.getHostname(new Host(new TestProtocol(Scheme.s3) {
            @Override
            public String getOAuthTokenUrl() {
                return "https://login/token";
            }
        }, "h", new Credentials().withOauth(new OAuthTokens("a", "r", -1L)))));
        assertNull(s.getHostname(new Host(new TestProtocol(Scheme.s3) {
            @Override
            public String getOAuthTokenUrl() {
                return "https://login:9000/token";
            }
        }, "h", new Credentials().withOauth(new OAuthTokens("a", "r", -1L)))));
        assertEquals(9000, s.getPort(new Host(new TestProtocol(Scheme.s3) {
            @Override
            public String getOAuthTokenUrl() {
                return "https://login:9000/token";
            }
        }, "h", new Credentials().withOauth(new OAuthTokens("a", "r", -1L)))), 0);
    }
}