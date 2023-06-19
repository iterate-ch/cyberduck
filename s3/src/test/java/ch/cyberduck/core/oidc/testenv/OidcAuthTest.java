package ch.cyberduck.core.oidc.testenv;/*
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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OidcAuthTest extends AbstractOidcTest {
    //    with Fiddler as proxy
//    new Proxy(Proxy.Type.HTTP, "localhost", 8888)

    @Test
    public void testSuccessfulLoginViaOidc() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "rouser"));
        session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        Credentials creds = host.getCredentials();
        System.out.println(creds.toString());
        assertNotEquals(StringUtils.EMPTY, creds.getUsername());
        assertNotEquals(StringUtils.EMPTY, creds.getPassword());
        // credentials from STS are written to the client object in the S3Session and not into the Credential object from the Host.
        assertTrue(creds.getToken().isEmpty());
        assertNotNull(creds.getOauth().getAccessToken());
        assertNotNull(creds.getOauth().getRefreshToken());
        assertNotEquals(Optional.of(Long.MAX_VALUE).get(), creds.getOauth().getExpiryInMilliseconds());
    }

    @Test(expected = LoginFailureException.class) //Todo check expected Exception
    public void testInvalidUserName() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("WrongUsername", "rouser"));
        session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test(expected = LoginFailureException.class) //Todo check expected Exception
    public void testInvalidPassword() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "invalid"));
        session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    // testTokenRefresh

    // Authorization
    @Test
    public void testUserReadAccess() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "rouser"));
        session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        //TODO read a file
        final TransferStatus status = new TransferStatus();
        final Path container = new Path( "", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3ReadFeature(session).read(new Path(container, "cyberduckbucket/testfile.txt", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
    }

//    @Test(expected = IllegalArgumentException.class)
//    public void testNoWritePermissionOnBucket() throws BackgroundException {
//        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "rouser"));
//        session = new S3Session(host);
//        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
//        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
//        //TODO write a file
//    }

    // testUserWriteAccess

    // testNoWritePermissionOnBucket
    // testNoReadPermissionOnBucket




    // testNoWritePermissionOnFolder
    // testNoReadPermissionOnFolder



    // testUserWithRightPolicy
    // testUserWithWrongPolicy


    //separate STS Service test - maybe not possible
//    @Test
//    public void testSTSAssumeRoleWithWebIdentity() throws BackgroundException {
//        NonAwsSTSCredentialsConfigurator sts = new NonAwsSTSCredentialsConfigurator(new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledLoginCallback());
//    }
}
