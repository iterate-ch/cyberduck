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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.s3.S3LoginProtocol;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class AWSConsoleLoginCredentialsStrategyTest {

    @Test
    public void testExportAndMemoryCache() throws Exception {
        final Host host = new Host(new S3LoginProtocol()).setUuid("bookmark");
        host.getCredentials().setSaved(true);
        final TestStrategy strategy = new TestStrategy(host,
                new AWSConsoleLoginCredentialsStrategy.Result(0, credentials(), ""),
                new AWSConsoleLoginCredentialsStrategy.Result(0,
                        identity("arn:aws:iam::123456789012:user/alice"), ""));

        final Credentials first = strategy.get();
        final Credentials second = strategy.get();

        assertEquals("ASIAEXAMPLE", first.getTokens().getAccessKeyId());
        assertEquals("secret", first.getTokens().getSecretAccessKey());
        assertEquals("session", first.getTokens().getSessionToken());
        assertFalse(first.isSaved());
        assertFalse(second.isSaved());
        assertFalse(host.getCredentials().isSaved());
        assertEquals(2, strategy.commands.size());
        assertEquals(Arrays.asList("configure", "export-credentials", "--profile", "cyberduck-bookmark",
                "--region", "us-east-1", "--format", "process", "--no-cli-auto-prompt", "--no-cli-pager"),
                strategy.commands.get(0));
        assertEquals(Arrays.asList("sts", "get-caller-identity", "--profile", "cyberduck-bookmark",
                "--region", "us-east-1", "--output", "json", "--no-cli-auto-prompt", "--no-cli-pager"),
                strategy.commands.get(1));
    }

    @Test
    public void testLoginWhenExportFails() throws Exception {
        final TestStrategy strategy = new TestStrategy(
                new Host(new S3LoginProtocol()).setRegion("eu-west-1").setUuid("bookmark"),
                new AWSConsoleLoginCredentialsStrategy.Result(1, "No credentials", ""),
                new AWSConsoleLoginCredentialsStrategy.Result(0, "Login succeeded", ""),
                new AWSConsoleLoginCredentialsStrategy.Result(0, credentials(), ""),
                new AWSConsoleLoginCredentialsStrategy.Result(0,
                        identity("arn:aws:iam::123456789012:user/alice"), ""));

        strategy.get();

        assertEquals(4, strategy.commands.size());
        assertEquals(Arrays.asList("configure", "export-credentials", "--profile", "cyberduck-bookmark",
                "--region", "eu-west-1", "--format", "process", "--no-cli-auto-prompt", "--no-cli-pager"),
                strategy.commands.get(0));
        assertEquals(Arrays.asList("login", "--profile", "cyberduck-bookmark", "--region", "eu-west-1",
                "--no-cli-auto-prompt", "--no-cli-pager"), strategy.commands.get(1));
    }

    @Test
    public void testShowLoginErrorWithoutCredentialOutput() {
        final TestStrategy strategy = new TestStrategy(new Host(new S3LoginProtocol()),
                new AWSConsoleLoginCredentialsStrategy.Result(1, "credentials", ""),
                new AWSConsoleLoginCredentialsStrategy.Result(1, "sign-in URL", "permission denied"));

        final LoginFailureException failure = assertThrows(LoginFailureException.class, strategy::get);
        assertTrue(failure.getDetail(false).contains("permission denied"));
        assertFalse(failure.getDetail(false).contains("credentials"));
    }

    @Test
    public void testRejectPermanentCredentials() {
        final TestStrategy strategy = new TestStrategy(new Host(new S3LoginProtocol()),
                new AWSConsoleLoginCredentialsStrategy.Result(0,
                        "{\"Version\":1,\"AccessKeyId\":\"key\",\"SecretAccessKey\":\"secret\"}", ""));

        assertThrows(LoginFailureException.class, strategy::get);
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

    private static String credentials() {
        return "{\"Version\":1,\"AccessKeyId\":\"ASIAEXAMPLE\",\"SecretAccessKey\":\"secret\","
                + "\"SessionToken\":\"session\",\"Expiration\":\"2099-01-01T00:00:00+00:00\"}";
    }

    private static String identity(final String arn) {
        return String.format("{\"Account\":\"123456789012\",\"Arn\":\"%s\"}", arn);
    }

    private static final class TestStrategy extends AWSConsoleLoginCredentialsStrategy {
        private final Queue<Result> results = new ArrayDeque<>();
        private final List<List<String>> commands = new ArrayList<>();

        private TestStrategy(final Host host, final Result... results) {
            super(host, CancelCallback.noop);
            this.results.addAll(Arrays.asList(results));
        }

        @Override
        protected Result execute(final String... arguments) {
            commands.add(Arrays.asList(arguments));
            return results.remove();
        }
    }
}
