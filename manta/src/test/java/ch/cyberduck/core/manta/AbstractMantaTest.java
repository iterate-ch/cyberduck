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
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProfileReaderFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.UUID;

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

        final String key = System.getProperty("manta.key");
        final Local file = TemporaryFileServiceFactory.get().create(new AlphanumericRandomStringService().random());
        LocalTouchFactory.get().touch(file);
        IOUtils.write(key, file.getOutputStream(false), Charset.defaultCharset());
        final String user = System.getProperty("manta.user");
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
            user).withIdentity(file)
        );
        session = new MantaSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        testPathPrefix = new Path(new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getAccountPrivateRoot(), new AlphanumericRandomStringService().random(), EnumSet.of(Type.directory));
        session.getClient().putDirectory(testPathPrefix.getAbsolute());
    }

    @After
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
