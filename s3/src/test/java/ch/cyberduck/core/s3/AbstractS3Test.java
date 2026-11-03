package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.VaultVersion;
import ch.cyberduck.test.VaultTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public abstract class AbstractS3Test extends VaultTest {

    protected S3Session session;

    /**
     * Connection with virtual host domain name
     */
    protected S3Session virtualhost;

    /**
     * Cloudfront distribution with S3 bucket origin
     */
    protected S3Session cloudfront;

    /**
     * @return Server implementations to run against. Select with environment variables named after the server
     * with any value, including empty. All servers when none is set.
     */
    private static List<S3TestServer> servers() {
        final List<S3TestServer> selected = Arrays.stream(S3TestServer.values())
                .filter(server -> null != System.getenv(server.name())).collect(Collectors.toList());
        return selected.isEmpty() ? Arrays.asList(S3TestServer.values()) : selected;
    }

    /**
     * Run against every server implementation
     */
    @Parameterized.Parameters(name = "server = {0}")
    public static Object[][] configurations() {
        return servers().stream().map(server -> new Object[]{server, null}).toArray(Object[][]::new);
    }

    /**
     * Run against every server implementation with all vault versions
     */
    protected static Object[][] parameterMatrix() {
        final List<Object[]> data = new ArrayList<>();
        for(S3TestServer server : servers()) {
            for(VaultVersion.Type type : new VaultVersion.Type[]{VaultVersion.Type.V8, VaultVersion.Type.UVF}) {
                data.add(new Object[]{server, type});
            }
        }
        return data.toArray(new Object[0][]);
    }

    @Parameterized.Parameter(0)
    public S3TestServer server;

    @Parameterized.Parameter(1)
    public VaultVersion.Type vaultVersion;

    /**
     * Skip test if server implementation is not expected to pass
     */
    protected void assumeNot(final S3TestServer excluded, final String reason) {
        assumeTrue(reason, server != excluded);
    }

    @After
    public void disconnect() throws Exception {
        if(session != null) {
            session.close();
        }
        if(virtualhost != null) {
            virtualhost.close();
        }
        if(cloudfront != null) {
            cloudfront.close();
        }
    }

    /**
     * Skip before any {@link Before} method connects to the server
     */
    @Rule
    public final TestRule availability = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            assumeTrue(String.format("Server %s not available", server), server.isAvailable());
            base.evaluate();
        }
    };

    @Before
    public void setupDefault() throws Exception {
        if(server != S3TestServer.AWS) {
            session = this.connect();
            return;
        }
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Amazon S3.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key")
        )) {
            @Override
            public String getProperty(final String key) {
                if("s3.listing.versioning.enable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        session = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, HostKeyCallback.noop,
                new TestPasswordStore(), ProgressListener.noop);
        login.check(session, CancelCallback.noop);
    }

    @Before
    public void setupVirtualHost() throws Exception {
        if(server != S3TestServer.AWS) {
            virtualhost = this.connect();
            return;
        }
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Amazon S3.cyberduckprofile"));
        final Host host = new Host(profile, "test-eu-central-1-cyberduck.s3.amazonaws.com", new Credentials(
                PROPERTIES.get("s3.key")
        ));
        virtualhost = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, HostKeyCallback.noop,
                new TestPasswordStore(), ProgressListener.noop);
        login.check(virtualhost, CancelCallback.noop);
    }

    @Before
    public void setupCloudFront() throws Exception {
        if(server != S3TestServer.AWS) {
            // Not available
            return;
        }
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Amazon S3.cyberduckprofile"));
        final Host host = new Host(profile, "d4fobtprygi46.cloudfront.net", new Credentials("anonymous")).setRegion("eu-central-1");
        cloudfront = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, HostKeyCallback.noop,
                new DisabledPasswordStore(), ProgressListener.noop);
        login.check(cloudfront, CancelCallback.noop);
    }

    private S3Session connect() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTP).cyberduckprofile"));
        final Host host = new Host(profile, server.host(), server.port(), new Credentials(S3TestServer.ACCESS_KEY, S3TestServer.SECRET_KEY)) {
            @Override
            public String getProperty(final String key) {
                if("s3.listing.versioning.enable".equals(key)) {
                    return String.valueOf(true);
                }
                if("s3.bucket.virtualhost.disable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        final S3Session connection = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, HostKeyCallback.noop, new DisabledPasswordStore(), ProgressListener.noop).check(connection, CancelCallback.noop);
        return connection;
    }
}
