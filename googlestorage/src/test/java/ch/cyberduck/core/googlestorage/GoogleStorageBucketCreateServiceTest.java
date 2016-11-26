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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class GoogleStorageBucketCreateServiceTest {

    @Test
    public void testCreate() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
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
        final Path bucket = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageBucketCreateService(session).create(bucket, "US");
        assertTrue(session.getFeature(Find.class).find(bucket));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(bucket));
        session.close();
    }
}
