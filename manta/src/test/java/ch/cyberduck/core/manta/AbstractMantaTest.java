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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

public abstract class AbstractMantaTest extends VaultTest {
    private static final Logger log = LogManager.getLogger(AbstractMantaTest.class);

    protected MantaSession session;
    protected Path testPathPrefix;

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new MantaProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Joyent Triton Object Storage (us-east).cyberduckprofile"));

        final String hostname;
        final Local file;
        if(ObjectUtils.allNotNull(PROPERTIES.get("manta.key_path"), PROPERTIES.get("manta.url"))) {
            file = new Local(PROPERTIES.get("manta.key_path"));
            hostname = new URL(PROPERTIES.get("manta.url")).getHost();
        }
        else {
            final String key = PROPERTIES.get("manta.key");
            file = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
            LocalTouchFactory.get().touch(file);
            IOUtils.write(key, file.getOutputStream(false), Charset.defaultCharset());
            hostname = profile.getDefaultHostname();
        }

        final String user = PROPERTIES.get("manta.user");
        final Host host = new Host(profile, hostname, new Credentials(user).withIdentity(file));
        session = new MantaSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final String testRoot = "cyberduck-test-" + new AlphanumericRandomStringService().random();
        testPathPrefix = new Path(new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getAccountPrivateRoot(), testRoot, EnumSet.of(Type.directory));
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
