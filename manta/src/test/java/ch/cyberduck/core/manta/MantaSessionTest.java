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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;

import org.junit.Test;

import javax.crypto.SecretKey;

import com.joyent.manta.client.crypto.SecretKeyUtils;
import com.joyent.manta.config.DefaultsConfigContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MantaSessionTest {

    @Test
    public void testFeatures() throws Exception {
        final MantaSession session = new MantaSession(new Host(new MantaProtocol(), "username"));
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
            SessionFactory.create(new Credentials(username)).login(
                    new DisabledPasswordStore(),
                    new DisabledLoginCallback(),
                    new DisabledCancelCallback(),
                    PathCache.empty()
            );
        } catch (LoginFailureException e) {
            assertTrue(e.getMessage().contains("Login failed"));
            assertTrue(e.getDetail().contains("Invalid username"));
        } catch(BackgroundException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testSessionRejectsBadUsernames() {
        assertUsernameFailsLogin("!");
        assertUsernameFailsLogin("/subuser");
        assertUsernameFailsLogin("login/");
    }


    @Test
    public void testUserOwnerIdentification() throws BackgroundException {
        final MantaSession ownerSession = SessionFactory.create(new Credentials("theOwner"));
        ownerSession.initializeHomeInfo();
        assertTrue(ownerSession.userIsOwner());
        final MantaSession subuserSession = SessionFactory.create(new Credentials("theOwner/theSubUser"));
        subuserSession.initializeHomeInfo();
        assertFalse(subuserSession.userIsOwner());
    }

}
