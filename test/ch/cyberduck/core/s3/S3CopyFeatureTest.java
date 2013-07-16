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
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class S3CopyFeatureTest extends AbstractTestCase {

    @Test
    public void testCopy() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                new Credentials(properties.getProperty("s3.key"), properties.getProperty("s3.secret"))
        );
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        session.touch(test);
        final Path copy = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new S3CopyFeature(session).copy(test, copy);
        assertTrue(session.exists(test));
        session.delete(test, new DisabledLoginController());
        assertTrue(session.exists(copy));
        session.delete(copy, new DisabledLoginController());
        session.close();
    }
}
