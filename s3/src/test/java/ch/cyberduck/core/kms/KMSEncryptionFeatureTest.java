package ch.cyberduck.core.kms;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class KMSEncryptionFeatureTest {
    @Test
    public void testGetKeys() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final KMSEncryptionFeature kms = new KMSEncryptionFeature(new S3Session(host));
        assertFalse(kms.getKeys(new DisabledLoginCallback()).isEmpty());
    }

    @Test(expected = LoginCanceledException.class)
    public void testCreateUserAuthenticationFailure() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                "key", "secret"
        ));
        new KMSEncryptionFeature(new S3Session(host)).getKeys(new DisabledLoginCallback());
    }

    @Test(expected = ConnectionTimeoutException.class)
    public void testTimeout() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final KMSEncryptionFeature kms = new KMSEncryptionFeature(new S3Session(host), 1);
        try {
            kms.getKeys(new DisabledLoginCallback());
            fail();
        }
        catch(BackgroundException e) {
            assertTrue(new DefaultFailureDiagnostics().determine(e) == FailureDiagnostics.Type.network);
            throw e;
        }
    }
}