package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class MantaSessionTest {

    @Test
    public void testFeatures() throws Exception {
        final MantaSession session = new MantaSession(new Host(new MantaProtocol(), "username"),
            new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertTrue(session.getFeature(Read.class) instanceof MantaReadFeature);
        assertTrue(session.getFeature(Write.class) instanceof MantaWriteFeature);
        assertTrue(session.getFeature(Directory.class) instanceof MantaDirectoryFeature);
        assertTrue(session.getFeature(Touch.class) instanceof MantaTouchFeature);
        assertTrue(session.getFeature(Delete.class) instanceof MantaDeleteFeature);
        assertTrue(session.getFeature(UrlProvider.class) instanceof MantaUrlProviderFeature);
        assertTrue(session.getFeature(AttributesFinder.class) instanceof MantaAttributesFinderFeature);
    }

    private void assertUsernameFailsLogin(final String username) {
        try {
            final MantaSession session = new MantaSession(
                new Host(
                    new MantaProtocol(),
                    null,
                    443,
                    new Credentials(username)), new DisabledX509TrustManager(), new DefaultX509KeyManager());
            session.open(new DisabledHostKeyCallback());
            session.login(
                new DisabledPasswordStore(),
                new DisabledLoginCallback(),
                new DisabledCancelCallback()
            );
        }
        catch(LoginFailureException e) {
            assertTrue(e.getMessage().contains("Login failed"));
        }
        catch(BackgroundException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }
    }

    @Test
    @Ignore
    public void testSessionRejectsBadUsernames() {
        assertUsernameFailsLogin("!");
        assertUsernameFailsLogin("/subuser");
        assertUsernameFailsLogin("login/");
    }


    @Test
    public void testUserOwnerIdentification() throws BackgroundException {
        final MantaSession ownerSession = new MantaSession(
            new Host(
                new MantaProtocol(),
                null,
                443,
                new Credentials("theOwner")), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertTrue(ownerSession.userIsOwner());
        final MantaSession subuserSession = new MantaSession(
            new Host(
                new MantaProtocol(),
                null,
                443,
                new Credentials("theOwner/theSubUser")), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertFalse(subuserSession.userIsOwner());
    }
}
