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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.junit.Ignore;
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
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("static.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("EU");
        final List<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
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
        session.close();
    }

    @Test
    public void tetsEmptyPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final List<Path> list = new S3ObjectListService(session).list(new Path(container, "test", Path.DIRECTORY_TYPE),
                new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("notfound.cyberduck.ch", Path.VOLUME_TYPE);
        new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        session.close();
    }

    @Test
    @Ignore
    public void testListCnameAnonymous() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new Path("/dist.springframework.org", Path.DIRECTORY_TYPE),
                new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release", Path.DIRECTORY_TYPE).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/milestone", Path.DIRECTORY_TYPE).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/snapshot", Path.DIRECTORY_TYPE).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/robots.txt", Path.FILE_TYPE).getReference()));
        session.close();
    }

    @Test
    public void testListBuckenameAnonymous() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new Path("/dist.springframework.org", Path.DIRECTORY_TYPE),
                new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release", Path.DIRECTORY_TYPE).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/milestone", Path.DIRECTORY_TYPE).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/snapshot", Path.DIRECTORY_TYPE).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/robots.txt", Path.FILE_TYPE).getReference()));
        session.close();
    }

    @Test
    public void testListDefaultPath() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dist.springframework.org/release");
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        assertEquals(new Path("/dist.springframework.org/release", Path.DIRECTORY_TYPE), new DefaultHomeFinderService(session).find());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release/SWF", Path.DIRECTORY_TYPE).getReference()));
        session.close();
    }

    @Test
    public void testListVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final AttributedList<Path> list = new S3ObjectListService(session).list(new Path("versioning.test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE),
                new DisabledListProgressListener());
        final PathAttributes att = new PathAttributes(Path.FILE_TYPE);
        assertTrue(list.contains(new Path("/versioning.test.cyberduck.ch/test", att).getReference()));
        att.setVersionId("xtgd1iPdpb1L0c87oe.3KVul2rcxRyqh");
        assertTrue(list.contains(new Path("/versioning.test.cyberduck.ch/test", att).getReference()));
        session.close();
    }
}
