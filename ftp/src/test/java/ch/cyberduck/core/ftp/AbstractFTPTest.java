package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.fail;

public class AbstractFTPTest {

    private static final int PORT_NUMBER = ThreadLocalRandom.current().nextInt(2000, 3000);

    protected FTPSession session;

    private FtpServer server;
    private Local directory;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{CryptoVault.VAULT_VERSION_DEPRECATED, CryptoVault.VAULT_VERSION};
    }

    @Parameterized.Parameter
    public int vaultVersion;

    @After
    public void disconnect() {
        try {
            session.close();
        }
        catch(BackgroundException e) {
            // Ignore
        }
    }

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new FTPProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/FTP.cyberduckprofile"));
        final Host host = new Host(profile, "localhost", PORT_NUMBER, new Credentials("test", "test")) {
            @Override
            public String getProperty(final String key) {
                if(key.equals("ftp.datachannel.epsv")) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        session = new FTPSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public <T> T _getFeature(final Class<T> type) {
                final T f = AbstractFTPTest.this.getFeature(type);
                if(null == f) {
                    return super._getFeature(type);
                }
                return f;
            }
        };
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }

            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(), new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            return "n";
        }
    }

    protected <T> T getFeature(final Class<T> type) {
        return null;
    }

    @After
    public void stop() {
        server.stop();
        try {
            directory.delete();
        }
        catch(AccessDeniedException | NotfoundException e) {
            // Ignore
        }
    }

    @Before
    public void start() throws Exception {
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        final UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        user.setName("test");
        user.setPassword("test");
        directory = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        directory.mkdir();
        user.setHomeDirectory(directory.getAbsolute());
        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        //authorities.add(new ConcurrentLoginPermission(2, Integer.MAX_VALUE));
        user.setAuthorities(authorities);
        userManager.save(user);
        serverFactory.setUserManager(userManager);
        final ListenerFactory factory = new ListenerFactory();
        factory.setPort(PORT_NUMBER);
        serverFactory.addListener("default", factory.createListener());
        server = serverFactory.createServer();
        server.start();
    }
}
