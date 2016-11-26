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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3SingleUploadService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3SingleUploadServiceTest {

    @Test
    public void testUpload() throws Exception {
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
        final S3SingleUploadService m = new S3SingleUploadService(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final String random = RandomStringUtils.random(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out, Charset.defaultCharset());
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(random.getBytes().length);
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = session.list(container,
                new DisabledListProgressListener()).get(test).attributes();
        assertEquals(random.getBytes().length, attributes.getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }
}
