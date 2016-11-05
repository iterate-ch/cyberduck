package ch.cyberduck.core.hubic;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class HubicSessionTest {

    @Test(expected = LoginCanceledException.class)
    public void testConnectInvalidRefreshToken() throws Exception {
        final HubicSession session = new HubicSession(new Host(new HubicProtocol(),
                new HubicProtocol().getDefaultHostname(), new Credentials("u@domain")));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        try {
            session.login(new DisabledPasswordStore() {
                @Override
                public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                    return "1464730217WkCCqXpaGwQfxpUwI6wcXe6NvMCTJMg5lHrcBTRIaY4yAbRFBxvaSBparqNRsui9";
                }
            }, new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(LoginFailureException e) {
            assertEquals("Invalid refresh token. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
        session.close();
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectInvalidAccessToken() throws Exception {
        final HubicSession session = new HubicSession(new Host(new HubicProtocol(),
                new HubicProtocol().getDefaultHostname(), new Credentials("u@domain")));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("hubiC (u@domain) OAuth2 Access Token")) {
                    return "invalid";
                }
                if(user.equals("hubiC (u@domain) OAuth2 Refresh Token")) {
                    return System.getProperties().getProperty("hubic.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }
}