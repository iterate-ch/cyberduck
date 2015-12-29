package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProfileReaderFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Category(IntegrationTest.class)
public class IRODSSessionTest {

    @BeforeClass
    public static void protocol() {
        ProtocolFactory.register(new IRODSProtocol());
    }

    @Test
    public void testConnect() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/iRODS (iPlant Collaborative).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("irods.key"), System.getProperties().getProperty("irods.secret")
        ));

        final IRODSSession session = new IRODSSession(host);

        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());

        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testLoginDefault() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/iRODS (iPlant Collaborative).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("irods.key"), System.getProperties().getProperty("irods.secret")
        ));

        final IRODSSession session = new IRODSSession(host);

        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());

        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertNotNull(session.workdir());

        final AttributedList<Path> list = session.list(session.workdir(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());

        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testLoginPam() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/iRODS (iPlant Collaborative).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("tacc.key"), System.getProperties().getProperty("tacc.secret")
        ));
        host.setHostname("irods.wrangler.tacc.utexas.edu");
        host.setPort(1247);
        host.setRegion("taccWranglerZ");
        host.setDefaultPath("/taccWranglerZ/home/iterate");
        final AtomicBoolean verified = new AtomicBoolean();
        final IRODSSession session = new IRODSSession(host, new DisabledX509TrustManager() {
            @Override
            protected void accept(final List<X509Certificate> certs) {
                verified.set(true);
                super.accept(certs);
            }
        }, new DefaultX509KeyManager());

        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());

        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertNotNull(session.workdir());
        assertTrue(verified.get());

        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/iRODS (iPlant Collaborative).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("a", "a"));

        final IRODSSession session = new IRODSSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
    }

}
