package ch.cyberduck.core.sts;

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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static ch.cyberduck.core.sts.AbstractAssumeRoleWithWebIdentityTest.MILLIS;
import static ch.cyberduck.core.sts.AbstractAssumeRoleWithWebIdentityTest.OAUTH_TTL_SECS;
import static ch.cyberduck.core.sts.STSTestSetup.readProfile;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


@Category(TestcontainerTest.class)
public class STSBucketRequestBeforeTokenExpiryFailsBecauseOfLatencyTest {


    /**
     * If an HTTP HEAD request to MinIO fails, the error code and error description are written to MinIO-specific error headers, since HTTP HEAD requests do not use a body.
     * For example, a HEAD request is used to check whether an S3 bucket or folder is discoverable.
     * When a HEAD request uses expired STS credentials. Because of preemtively refresh tokens this case is only possible it the credentials are still valid but a few milliseconds before expire. Because of the latency in the network the request will be invalid when reaching the MinIO Service.
     * But the sleep time needs to be ajusted according to the network latency.
     * Adjust the sleep time according to the network latency.
     * Overall the test may be removed and the general question is how to handle the MinIO-specific HTTP-Headers when a HEAD-Request is failing.
     * This test fails if the x-minio Headers are not read because of InvalidAccessKeyId error code which has no response body.
     */
    @Test
    @Ignore("Time of network latency may vary and so the time needs to be adjusted manually") // TODO should we remove this test or keep for documentation?
    public void testBucketRequestBeforeTokenExpiryFailsBecauseOfLatency() throws BackgroundException, InterruptedException {
        final Profile profile = readProfile();

        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback());

        String firstAccessToken = host.getCredentials().getOauth().getIdToken();
        String firstAccessKey = session.getClient().getProviderCredentials().getAccessKey();

        // Time of latency may vary and so the time needs to be adjusted accordingly
        final int NETWORK_LATENCY = 1180;
        final int wait = OAUTH_TTL_SECS * MILLIS - NETWORK_LATENCY;
        Thread.sleep(wait);

        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));

        assertNotEquals(firstAccessToken, host.getCredentials().getOauth().getIdToken());
        assertNotEquals(firstAccessKey, session.getClient().getProviderCredentials().getAccessKey());
        session.close();
    }
}