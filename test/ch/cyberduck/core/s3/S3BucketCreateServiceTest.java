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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3BucketCreateServiceTest extends AbstractTestCase {

    @Test
    public void testCreateLocationUS() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        assertNotNull(session.open(new DefaultHostKeyController()));
        final Path bucket = new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE | Path.VOLUME_TYPE);
        new S3BucketCreateService(session).create(bucket, "US");
        assertTrue(new S3FindFeature(session).find(bucket));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(bucket), new DisabledLoginController());
        assertFalse(new S3FindFeature(session).find(bucket));
        session.close();
    }

    @Test
    public void testCreateLocationAsia() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        assertNotNull(session.open(new DefaultHostKeyController()));
        final Path bucket = new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE | Path.VOLUME_TYPE);
        new S3BucketCreateService(session).create(bucket, "ap-northeast-1");
        bucket.attributes().setRegion("ap-northeast-1");
        assertTrue(new S3FindFeature(session).find(bucket));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(bucket), new DisabledLoginController());
        assertFalse(new S3FindFeature(session).find(bucket));
        session.close();
    }
}
