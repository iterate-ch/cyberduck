package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.apache.http.client.methods.HttpUriRequest;
import org.jets3t.service.impl.rest.httpclient.RegionEndpointCache;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(IntegrationTest.class)
public class RequestEntityRestStorageServiceTest extends AbstractS3Test {

    @Test
    public void testGetBucket() {
        assertEquals("bucketname", RequestEntityRestStorageService.findBucketInHostname(new Host(new S3Protocol(), "bucketname.s3.amazonaws.com")));
        assertNull(RequestEntityRestStorageService.findBucketInHostname(new Host(new TestProtocol(), "bucketname.s3.amazonaws.com")));
    }

    @Test
    public void testSetupConnection() throws Exception {
        final RequestEntityRestStorageService service = new RequestEntityRestStorageService(session, new HttpConnectionPoolBuilder(session.getHost(),
                new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), session.getHost().getHostname()),
                new DefaultX509KeyManager(), new DisabledProxyFinder()).build(Proxy.DIRECT, new DisabledTranscriptListener(), new DisabledLoginCallback()));
        final RegionEndpointCache cache = service.getRegionEndpointCache();
        cache.clear();
        final String key = new AlphanumericRandomStringService().random();
        {
            final HttpUriRequest request = service.setupConnection("GET", "test-eu-central-1-cyberduck", key, Collections.emptyMap());
            assertEquals(String.format("https://test-eu-central-1-cyberduck.s3.dualstack.eu-central-1.amazonaws.com:443/%s", key), request.getURI().toString());
        }
        cache.clear();
        {
            final HttpUriRequest request = service.setupConnection("HEAD", "test-eu-central-1-cyberduck", key, Collections.singletonMap("location", ""));
            assertEquals(String.format("https://test-eu-central-1-cyberduck.s3.amazonaws.com:443/%s?location=", key), request.getURI().toString());
        }
    }

    @Test
    public void testSetupConnectionVirtualHost() throws Exception {
        final RequestEntityRestStorageService service = new RequestEntityRestStorageService(virtualhost, new HttpConnectionPoolBuilder(virtualhost.getHost(),
                new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), session.getHost().getHostname()),
                new DefaultX509KeyManager(), new DisabledProxyFinder()).build(Proxy.DIRECT, new DisabledTranscriptListener(), new DisabledLoginCallback()));
        final RegionEndpointCache cache = service.getRegionEndpointCache();
        cache.clear();
        final String key = new AlphanumericRandomStringService().random();
        {
            final HttpUriRequest request = service.setupConnection("GET", "", key, Collections.emptyMap());
            assertEquals(String.format("https://test-eu-west-3-cyberduck.s3.amazonaws.com:443/%s", key), request.getURI().toString());
        }
    }
}