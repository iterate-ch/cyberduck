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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.LoginFailureException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HubicSessionTest {

    @Test(expected = LoginFailureException.class)
    public void testConnectInvalidRefreshToken() throws Exception {
        final HubicSession session = new HubicSession(new Host(new HubicProtocol()));
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
}