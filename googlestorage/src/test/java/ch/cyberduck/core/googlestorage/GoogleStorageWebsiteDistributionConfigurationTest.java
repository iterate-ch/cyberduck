package ch.cyberduck.core.googlestorage;

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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageWebsiteDistributionConfigurationTest {

    @Test
    public void testGetMethods() throws Exception {
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(new GoogleStorageSession(
                new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname())));
        assertEquals(Collections.singletonList(Distribution.WEBSITE), configuration.getMethods(new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetProtocol() throws Exception {
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(new GoogleStorageSession(
                new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname())));
        assertEquals(new GoogleStorageProtocol().getDefaultHostname(), configuration.getHostname());
    }

    @Test
    public void testUrl() throws Exception {
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(new GoogleStorageSession(
                new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname())));
        assertEquals("http://test.cyberduck.ch.storage.googleapis.com/f", configuration.toUrl(new Path("test.cyberduck.ch/f", EnumSet.of(Path.Type.file))).find(
                DescriptiveUrl.Type.origin).getUrl());
        assertEquals("http://test.cyberduck.ch.storage.googleapis.com/f", configuration.toUrl(new Path("test.cyberduck.ch/f", EnumSet.of(Path.Type.file))).find(
                DescriptiveUrl.Type.cdn).getUrl());
    }

    @Test
    public void testRead() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Access Token")) {
                    return System.getProperties().getProperty("google.accesstoken");
                }
                if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Refresh Token")) {
                    return System.getProperties().getProperty("google.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(session);
        final Distribution website = configuration.read(new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory)), Distribution.WEBSITE,
                new DisabledLoginCallback());
        assertTrue(website.isEnabled());
        assertEquals(URI.create("http://test.cyberduck.ch.storage.googleapis.com"), website.getUrl());
        assertTrue(website.getContainers().contains(new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory))));
    }

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Access Token")) {
                    return System.getProperties().getProperty("google.accesstoken");
                }
                if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Refresh Token")) {
                    return System.getProperties().getProperty("google.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(session);
        final Path bucket = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageBucketCreateService(session).create(bucket, "US");
        configuration.write(bucket, new Distribution(null, Distribution.WEBSITE, true), new DisabledLoginCallback());
        assertTrue(configuration.read(bucket, Distribution.WEBSITE, new DisabledLoginCallback()).isEnabled());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testFeatures() {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        final DistributionConfiguration d = new GoogleStorageWebsiteDistributionConfiguration(
                session
        );
        assertNotNull(d.getFeature(Index.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(AnalyticsProvider.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(DistributionLogging.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(IdentityConfiguration.class, Distribution.WEBSITE));
        assertNull(d.getFeature(Cname.class, Distribution.WEBSITE));
    }
}
