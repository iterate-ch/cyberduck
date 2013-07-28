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
import ch.cyberduck.core.features.Touch;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3MetadataFeatureTest extends AbstractTestCase {

    @Test
    public void testGetMetadataBucket() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(container);
        assertTrue(metadata.isEmpty());
        session.close();
    }

    @Test
    public void testGetMetadataFile() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(new Path(container, "test.txt", Path.FILE_TYPE));
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Content-Type"));
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertTrue(metadata.containsKey("test"));
        assertEquals("Cyberduck", metadata.get("test"));
        session.close();
    }

    @Test
    public void testSetMetadataFile() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        session.getFeature(Touch.class, new DisabledLoginController()).touch(test);
        final String v = UUID.randomUUID().toString();
        new S3MetadataFeature(session).setMetadata(test, Collections.<String, String>singletonMap("Test", v));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("test"));
        assertEquals(v, metadata.get("test"));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test));
        session.close();
    }
}
