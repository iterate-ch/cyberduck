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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.VaultVersion;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@Category(TestcontainerTest.class)
public abstract class AbstractFTPTest {

    private static final String USER = "test";
    private static final String PASSWORD = "test";
    private static final int FTP_PORT = 21;
    /**
     * Home directory of user in container
     */
    protected static final String HOME = "/ftp/test";
    /**
     * Passive data connections are made to the port announced by the server and must be mapped one to one
     */
    private static final int PASV_MIN_PORT = 21000;
    private static final int PASV_MAX_PORT = 21010;

    private static final GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("delfer/alpine-ftp-server:latest"))
            .withEnv("USERS", String.format("%s|%s", USER, PASSWORD))
            .withEnv("ADDRESS", "127.0.0.1")
            .withEnv("MIN_PORT", String.valueOf(PASV_MIN_PORT))
            .withEnv("MAX_PORT", String.valueOf(PASV_MAX_PORT))
            .withExposedPorts(FTP_PORT)
            .withLogConsumer(f -> System.out.print("FTPD " + f.getUtf8String()))
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

    @BeforeClass
    public static void start() throws Exception {
        container.setPortBindings(IntStream.rangeClosed(PASV_MIN_PORT, PASV_MAX_PORT)
                .mapToObj(port -> String.format("%d:%d", port, port)).collect(Collectors.toList()));
        container.start();
        awaitGreeting();
    }

    /**
     * Port forwarded to host accepts connections before server is ready to reply
     */
    private static void awaitGreeting() throws Exception {
        final long timeout = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        while(true) {
            try(Socket socket = new Socket(container.getHost(), container.getMappedPort(FTP_PORT))) {
                socket.setSoTimeout(2000);
                final String greeting = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
                if(null != greeting && greeting.startsWith("220")) {
                    return;
                }
            }
            catch(IOException e) {
                // Retry
            }
            if(System.nanoTime() > timeout) {
                throw new IOException("Timeout waiting for FTP server");
            }
            Thread.sleep(500L);
        }
    }

    @AfterClass
    public static void stop() {
        container.stop();
    }

    protected FTPSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultVersion.Type.V8, VaultVersion.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultVersion.Type vaultVersion;

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
        final Host host = new Host(new FTPProtocol(), container.getHost(), container.getMappedPort(FTP_PORT), new Credentials(USER, PASSWORD)) {
            @Override
            public String getProperty(final String key) {
                if(key.equals("ftp.datachannel.epsv")) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        session = new FTPSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
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
        }, HostKeyCallback.noop, new TestPasswordStore(), ProgressListener.noop);
        login.check(session, CancelCallback.noop);
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            return "n";
        }
    }

    /**
     * Skip test if feature is not advertised by server in reply to FEAT
     */
    protected void assumeFeature(final String feature) {
        assumeTrue(String.format("No support for %s", feature), session.getClient().hasFeature(feature));
    }
}
