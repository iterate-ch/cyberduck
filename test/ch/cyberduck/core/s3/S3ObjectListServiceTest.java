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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3ObjectListServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("static.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("EU");
        final List<Path> list = new S3ObjectListService(session).list(container);
        assertFalse(list.isEmpty());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            assertNotNull(p.attributes().getRegion());
            if(p.attributes().isFile()) {
                assertNotNull(p.attributes().getModificationDate());
                assertNotNull(p.attributes().getSize());
                assertNotNull(p.attributes().getChecksum());
            }
        }
    }

    @Test
    public void tetsEmptyPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final List<Path> list = new S3ObjectListService(session).list(new Path(container, "test", Path.DIRECTORY_TYPE));
        assertTrue(list.isEmpty());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("notfound.cyberduck.ch", Path.VOLUME_TYPE);
        new S3ObjectListService(session).list(container);
    }
}
