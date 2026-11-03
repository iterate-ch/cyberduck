package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class S3LoggingFeatureTest extends AbstractS3Test {

    @Test
    public void testGetConfiguration() throws Exception {
        this.assumeNot(S3TestServer.LOCALSTACK, "Bucket logging not supported");
        this.assumeNot(S3TestServer.MINIO, "Bucket logging not supported");
        this.assumeNot(S3TestServer.CEPH, "Bucket logging not supported");
        this.assumeNot(S3TestServer.RUSTFS, "Bucket logging not supported");
        final S3LoggingFeature feature = new S3LoggingFeature(session);
        final Path bucket = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        feature.setConfiguration(bucket, new LoggingConfiguration(true, "test-logging-us-east-1-cyberduck"));
        final LoggingConfiguration configuration = feature.getConfiguration(bucket);
        assertNotNull(configuration);
        assertEquals("test-logging-us-east-1-cyberduck", configuration.getLoggingTarget());
        assertTrue(configuration.isEnabled());
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        new S3LoggingFeature(session).getConfiguration(
            new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory))
        );
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        this.assumeNot(S3TestServer.MINIO, "Reply for missing bucket differs");
        this.assumeNot(S3TestServer.CEPH, "Reply for missing bucket differs");
        this.assumeNot(S3TestServer.RUSTFS, "Reply for missing bucket differs");
        new S3LoggingFeature(session).setConfiguration(
            new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new LoggingConfiguration(false)
        );
    }
}
