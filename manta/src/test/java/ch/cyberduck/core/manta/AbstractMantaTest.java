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

import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProfileReaderFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.fail;

public abstract class AbstractMantaTest {

    private static final Logger log = Logger.getLogger(AbstractMantaTest.class);

    protected MantaSession session;
    protected Path testPathPrefix;

    @BeforeClass
    public static void protocol() {
        ProtocolFactory.get().register(new MantaProtocol());
    }

    @Before
    public void setup() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/Triton Manta.cyberduckprofile"));

        final String keyPath = ObjectUtils.firstNonNull(System.getProperty("manta.key_path"), "");
        final String username = System.getProperty("manta.user");
        final Host host = new Host(
                profile,
                profile.getDefaultHostname(),
                new Credentials(username)
                        .withIdentity(new Local(keyPath)));

        session = new MantaSession(host);
        new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                        fail(reason);
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()
        ).connect(session, PathCache.empty(), new DisabledCancelCallback());

        testPathPrefix = new Path(session.getAccountPrivateRoot(), UUID.randomUUID().toString(), EnumSet.of(Type.directory));
        session.getClient().putDirectory(testPathPrefix.getAbsolute());
    }

    @AfterClass
    public void disconnect() throws Exception {
        log.debug("cleaning up test directory: " + testPathPrefix);
        session.getClient().deleteRecursive(testPathPrefix.getAbsolute());
        session.close();
    }

    protected Path randomFile() {
        return new Path(
                testPathPrefix,
                UUID.randomUUID().toString(),
                EnumSet.of(Type.file));
    }

    protected Path randomDirectory() {
        return new Path(
                testPathPrefix,
                UUID.randomUUID().toString(),
                EnumSet.of(Type.directory));
    }
}
