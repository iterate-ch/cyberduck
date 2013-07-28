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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3BucketCreateServiceTest extends AbstractTestCase {

    @Test
    public void testCreate() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        assertNotNull(session.open(new DefaultHostKeyController()));
        final Path bucket = new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        new S3BucketCreateService(session).create(bucket, "US");
        assertTrue(session.exists(bucket));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(bucket));
        assertFalse(session.exists(bucket));
        session.close();
    }
}
