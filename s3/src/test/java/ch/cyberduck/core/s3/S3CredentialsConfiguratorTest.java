package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class S3CredentialsConfiguratorTest {

    @Test
    public void testConfigure() throws Exception {
        new S3CredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback())
                .reload().configure(new Host(new TestProtocol()));
    }

    @Test
    public void readFailureForInvalidAWSCredentialsProfileEntry() throws Exception {
        final Credentials credentials = new Credentials("test_s3_profile");
        final Credentials verify = new S3CredentialsConfigurator(LocalFactory.get(new File("src/test/resources/invalid/.aws").getAbsolutePath()),
                new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback())
                .reload().configure(new Host(new TestProtocol(), StringUtils.EMPTY, credentials));
        assertEquals(credentials, verify);
    }

    @Test
    public void readSuccessForValidAWSCredentialsProfileEntry() throws Exception {
        final Credentials verify = new S3CredentialsConfigurator(LocalFactory.get(new File("src/test/resources/valid/.aws").getAbsolutePath())
                , new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback())
                .reload().configure(new Host(new TestProtocol(), StringUtils.EMPTY, new Credentials("test_s3_profile")));
        assertEquals("EXAMPLEKEYID", verify.getTokens().getAccessKeyId());
        assertEquals("EXAMPLESECRETKEY", verify.getTokens().getSecretAccessKey());
        assertEquals("EXAMPLETOKEN", verify.getTokens().getSessionToken());
    }

    @Test
    public void readSSOCachedTemporaryTokens() throws Exception {
        final Credentials verify = new S3CredentialsConfigurator(LocalFactory.get(new File("src/test/resources/valid/.aws").getAbsolutePath())
                , new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback())
                .reload().configure(new Host(new TestProtocol(), StringUtils.EMPTY, new Credentials("ReadOnlyAccess-189584543480")));
        assertEquals("TESTACCESSKEY", verify.getTokens().getAccessKeyId());
        assertEquals("TESTSECRETKEY", verify.getTokens().getSecretAccessKey());
        assertEquals("TESTSESSIONTOKEN", verify.getTokens().getSessionToken());
        assertEquals(3497005724000L, verify.getTokens().getExpiryInMilliseconds(), 0L);
    }
}
