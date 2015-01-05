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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.jets3t.service.Jets3tProperties;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final List<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            assertEquals("us-east-1", p.attributes().getRegion());
            if(p.isFile()) {
                assertNotNull(p.attributes().getModificationDate());
                assertNotNull(p.attributes().getSize());
                assertNotNull(p.attributes().getChecksum());
                assertNotNull(p.attributes().getStorageClass());
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final List<Path> list = new S3ObjectListService(session).list(new Path(container, "empty", EnumSet.of(Path.Type.directory, Path.Type.placeholder)),
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.volume));
        new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        session.close();
    }

    @Test
    @Ignore
    public void testListCnameAnonymous() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory)).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/milestone", EnumSet.of(Path.Type.directory)).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/snapshot", EnumSet.of(Path.Type.directory)).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/robots.txt", EnumSet.of(Path.Type.file)).getReference()));
        session.close();
    }

    @Test
    public void testListBuckenameAnonymous() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory, Path.Type.placeholder)).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/milestone", EnumSet.of(Path.Type.directory, Path.Type.placeholder)).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/snapshot", EnumSet.of(Path.Type.directory, Path.Type.placeholder)).getReference()));
        assertTrue(list.contains(new Path("/dist.springframework.org/robots.txt", EnumSet.of(Path.Type.file)).getReference()));
        session.close();
    }

    @Test
    public void testListDefaultPath() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dist.springframework.org/release");
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertEquals(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory)), new DefaultHomeFinderService(session).find());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release/SWF", EnumSet.of(Path.Type.directory, Path.Type.placeholder)).getReference()));
        session.close();
    }

    @Test
    public void testListVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final AttributedList<Path> list = new S3ObjectListService(session).list(new Path("versioning.test.cyberduck.ch",
                        EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new DisabledListProgressListener());
        final PathAttributes att = new PathAttributes();
        assertTrue(list.contains(new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(Path.Type.file), att).getReference()));
        att.setVersionId("xtgd1iPdpb1L0c87oe.3KVul2rcxRyqh");
        assertTrue(list.contains(new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(Path.Type.file), att).getReference()));
        session.close();
    }

    @Test
    public void testListPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(placeholder);
        placeholder.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Arrays.asList(placeholder), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testListAWS4SignatureFrankfurt() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("cyberduck-frankfurt", EnumSet.of(Path.Type.volume));
        final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        session.close();
    }

    @Test
    public void testListAWS2AutoSwitchAWS4SignatureFrankfurt() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS2;
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("cyberduck-frankfurt", EnumSet.of(Path.Type.volume));
        final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        session.close();
    }

    @Test(expected = BackgroundException.class)
    public void testAccessPathStyleBucketEuCentral() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

            @Override
            protected Jets3tProperties configure() {
                final Jets3tProperties properties = super.configure();
                properties.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
                return properties;
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("cyberduck-frankfurt", EnumSet.of(Path.Type.volume));
        try {
            final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        }
        catch(BackgroundException e) {
            assertEquals("Listing directory cyberduck-frankfurt failed.", e.getMessage());
            assertEquals("Received redirect response HTTP/1.1 301 Moved Permanently but no location header.", e.getDetail());
            throw e;
        }
        finally {
            session.close();
        }
    }
}
