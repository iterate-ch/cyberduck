package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.local.FlatTemporaryFileService;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.SecurityManager;
import io.milton.http.fs.FileSystemResourceFactory;
import io.milton.http.fs.SimpleSecurityManager;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.simpleton.SimpletonServer;

import static org.junit.Assert.fail;

public class AbstractDAVTest {

    protected final PathCache cache = new PathCache(100);
    protected DAVSession session;

    private SimpletonServer server;
    private static final int PORT_NUMBER = ThreadLocalRandom.current().nextInt(2000, 3000);

    @After
    public void disconnect() throws Exception {
        session.close();
        cache.clear();
    }

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DAVProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            this.getClass().getResourceAsStream("/DAV.cyberduckprofile"));
        final Host host = new Host(profile, "localhost", PORT_NUMBER, new Credentials("cyberduck"));
        session = new DAVSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
                return null;
            }

            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                //
            }
        }, new DisabledHostKeyCallback(), new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, PathCache.empty(), new DisabledCancelCallback());
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            return "n";
        }
    }

    @After
    public void stop() throws Exception {
        server.stop();
    }

    @Before
    public void start() throws Exception {
        final HttpManagerBuilder b = new HttpManagerBuilder();
        b.setEnableFormAuth(false);
        b.setEnableDigestAuth(false);
        b.setEnableOptionsAuth(false);

        final Local directory = new FlatTemporaryFileService().create(new AlphanumericRandomStringService().random());
        directory.mkdir();

        SecurityManager sm = new SimpleSecurityManager() {
            @Override
            public Object authenticate(final String user, final String password) {
                return user;
            }

            @Override
            public Object authenticate(final DigestResponse digestRequest) {
                return "ok";
            }

            @Override
            public String getRealm(final String host) {
                return "realm";
            }
        };
        final FileSystemResourceFactory resourceFactory = new FileSystemResourceFactory(new File(directory.getAbsolute()), sm, "/");
        resourceFactory.setAllowDirectoryBrowsing(true);
        b.setResourceFactory(resourceFactory);
        final HttpManager httpManager = b.buildHttpManager();

        server = new SimpletonServer(httpManager, b.getOuterWebdavResponseHandler(), 100, 10);
        server.setHttpPort(PORT_NUMBER);
        server.start();
    }
}
