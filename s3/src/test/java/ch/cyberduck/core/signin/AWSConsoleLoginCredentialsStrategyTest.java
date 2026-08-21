package ch.cyberduck.core.signin;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.s3.S3LoginProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class AWSConsoleLoginCredentialsStrategyTest {

    @Test
    public void testMemoryCache() throws Exception {
        final Host host = new Host(new S3LoginProtocol());
        host.getCredentials().setSaved(false);
        final TestStrategy strategy = new TestStrategy(host);

        final Credentials first = strategy.get();
        final Credentials second = strategy.get();

        assertEquals("ASIAEXAMPLE", first.getTokens().getAccessKeyId());
        assertEquals("secret", first.getTokens().getSecretAccessKey());
        assertEquals("session", first.getTokens().getSessionToken());
        assertFalse(first.isSaved());
        assertFalse(second.isSaved());
        assertEquals(1, strategy.authorizations);
    }

    @Test
    public void testPinIdentity() throws Exception {
        final Host host = new Host(new S3LoginProtocol());
        AWSConsoleLoginCredentialsStrategy.validateIdentity(host, "123456789012",
                "arn:aws:iam::123456789012:user/alice");
        assertEquals("123456789012/user/alice",
                host.getProperty(AWSConsoleLoginCredentialsStrategy.IDENTITY_PROPERTY));
        assertEquals("123456789012/user/alice", host.getNickname());

        AWSConsoleLoginCredentialsStrategy.validateIdentity(host, "123456789012",
                "arn:aws:iam::123456789012:user/alice");
        assertThrows(LoginFailureException.class, () -> AWSConsoleLoginCredentialsStrategy.validateIdentity(host,
                "123456789012", "arn:aws:sts::123456789012:assumed-role/Admin/session"));
    }

    @Test
    public void testRoleIdentityDropsSessionName() throws Exception {
        final Host host = new Host(new S3LoginProtocol());
        AWSConsoleLoginCredentialsStrategy.validateIdentity(host, "123456789012",
                "arn:aws:sts::123456789012:assumed-role/Admin/session");
        assertEquals("123456789012/role/Admin",
                host.getProperty(AWSConsoleLoginCredentialsStrategy.IDENTITY_PROPERTY));
    }

    private static final class TestStrategy extends AWSConsoleLoginCredentialsStrategy {
        private int authorizations;

        private TestStrategy(final Host host) {
            super(null, host, null, new DisabledPasswordStore());
        }

        @Override
        protected TemporaryAccessTokens authorize() {
            authorizations++;
            return new TemporaryAccessTokens("ASIAEXAMPLE", "secret", "session", Long.MAX_VALUE);
        }
    }
}
