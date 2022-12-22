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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HostPlistReaderTest {

    @BeforeClass
    public static void register() {
        ProtocolFactory.get().register(new TestProtocol());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testDeserializeNoSuchFile() throws Exception {
        final HostPlistReader reader = new HostPlistReader();
        reader.read(new Local("test"));
    }

    @Test
    public void testRead() throws Exception {
        HostPlistReader reader = new HostPlistReader();
        final Host read = reader.read(new Local(
                "src/test/resources/s3.amazonaws.com – S3.duck"));
        assertNotNull(read);
        assertEquals("Amazon Simple Storage Service & CloudFront CDN", read.getComment());
        assertEquals(new TestProtocol(), read.getProtocol());
    }

    @Test
    public void testReadPrivateKey() throws Exception {
        HostPlistReader reader = new HostPlistReader();
        final Host read = reader.read(new Local(
                "src/test/resources/Private Key Legacy.duck"));
        assertNotNull(read);
        assertEquals(new TestProtocol(), read.getProtocol());
        assertNotNull(read.getCredentials().getIdentity());
        assertEquals(String.format("~%s.ssh%skey.pem", PreferencesFactory.get().getProperty("local.delimiter"), PreferencesFactory.get().getProperty("local.delimiter")), read.getCredentials().getIdentity().getAbbreviatedPath());
    }

    @Test
    public void testReadPrivateKeyBookmark() throws Exception {
        HostPlistReader reader = new HostPlistReader();
        final Host read = reader.read(new Local(
                "src/test/resources/Private Key.duck"));
        assertNotNull(read);
        assertEquals(new TestProtocol(), read.getProtocol());
        assertNotNull(read.getCredentials().getIdentity());
        assertEquals(String.format("~%s.ssh%skey.pem", PreferencesFactory.get().getProperty("local.delimiter"), PreferencesFactory.get().getProperty("local.delimiter")), read.getCredentials().getIdentity().getAbbreviatedPath());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testReadNotFound() throws Exception {
        PlistReader reader = new HostPlistReader();
        reader.read(new Local("notfound.duck"));
    }

    @Test(expected = AccessDeniedException.class)
    public void testBrokenBookmark() throws Exception {
        HostPlistReader reader = new HostPlistReader();
        final Host read = reader.read(new Local(
                "src/test/resources/broken.duck"));
    }
}
