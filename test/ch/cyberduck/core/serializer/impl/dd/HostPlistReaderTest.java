package ch.cyberduck.core.serializer.impl.dd;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.FinderLocal;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class HostPlistReaderTest extends AbstractTestCase {

    @Test(expected = LocalAccessDeniedException.class)
    public void testDeserializeNoSuchFile() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        reader.read(new Local("test"));
    }

    @Test
    public void testDeserializeDeprecatedProtocol() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        assertNull(reader.read(
                new FinderLocal("test/ch/cyberduck/core/serializer/impl/1c158c34-db8a-4c32-a732-abd9447bb27c.duck")));
    }

    @Test
    public void testRead() throws Exception {
        HostPlistReader reader = new HostPlistReader();
        final Host read = reader.read(new Local(
                "test/ch/cyberduck/core/serializer/impl/s3.amazonaws.com – S3.duck"));
        assertNotNull(read);
        assertEquals("Amazon Simple Storage Service & CloudFront CDN", read.getComment());
        assertEquals(ProtocolFactory.S3_SSL, read.getProtocol());
    }

    @Test
    public void testReadPrivateKey() throws Exception {
        HostPlistReader reader = new HostPlistReader();
        final Host read = reader.read(new Local(
                "test/ch/cyberduck/core/serializer/impl/Private Key Legacy.duck"));
        assertNotNull(read);
        assertEquals(ProtocolFactory.SFTP, read.getProtocol());
        assertNotNull(read.getCredentials().getIdentity());
        assertEquals("~/.ssh/key.pem", read.getCredentials().getIdentity().getAbbreviatedPath());
    }

    @Test
    public void testReadPrivateKeyBookmark() throws Exception {
        HostPlistReader reader = new HostPlistReader();
        final Host read = reader.read(new Local(
                "test/ch/cyberduck/core/serializer/impl/Private Key.duck"));
        assertNotNull(read);
        assertEquals(ProtocolFactory.SFTP, read.getProtocol());
        assertNotNull(read.getCredentials().getIdentity());
        assertEquals("~/.ssh/key.pem", read.getCredentials().getIdentity().getAbbreviatedPath());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testReadNotFound() throws Exception {
        PlistReader reader = new HostPlistReader();
        reader.read(new Local("notfound.duck"));
    }
}
