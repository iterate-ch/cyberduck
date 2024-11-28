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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.fail;

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

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{CryptoVault.VAULT_VERSION_DEPRECATED, CryptoVault.VAULT_VERSION};
    }

    @Parameterized.Parameter
    public int vaultVersion;

    @After
    public void disconnect() throws Exception {
        session.close();
        virtualhost.close();
        cloudfront.close();
    }

    @Before
    public void setupDefault() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        ));
        session = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
        session.getHost().getCredentials().setPassword(PROPERTIES.get("s3.secret"));
    }

    @Before
    public void setupVirtualHost() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, "test-eu-central-1-cyberduck.s3.amazonaws.com", new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        ));
        virtualhost = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(virtualhost, new DisabledCancelCallback());
        virtualhost.getHost().getCredentials().setPassword(PROPERTIES.get("s3.secret"));
    }

    @Before
    public void setupCloudFront() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, "d4fobtprygi46.cloudfront.net", new Credentials("anonymous")).withRegion("eu-central-1");
        cloudfront = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(cloudfront, new DisabledCancelCallback());
        cloudfront.getHost().getCredentials().setPassword(PROPERTIES.get("s3.secret"));
    }
}
