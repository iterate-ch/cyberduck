package ch.cyberduck.core.freenet;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FreenetUrlProviderTest {

    @Test
    public void testToUrlRoot() {
        final FreenetUrlProvider provider = new FreenetUrlProvider(new Host(new FreenetProtocol(), "dav.freenet.de", 443, "/webdav"));
        final DescriptiveUrlBag urls = provider.toUrl(new Path("/webdav", EnumSet.of(Path.Type.directory)));
        assertEquals(1, urls.size());
        final DescriptiveUrl url = urls.find(DescriptiveUrl.Type.http);
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.http, url.getType());
        assertEquals("https://webmail.freenet.de/web/?goTo=share&path=/#cloud", url.getUrl());
    }

    @Test
    public void testToUrlDirectory() {
        final FreenetUrlProvider provider = new FreenetUrlProvider(new Host(new FreenetProtocol(), "dav.freenet.de", 443, "/webdav"));
        final DescriptiveUrlBag urls = provider.toUrl(new Path("/webdav/d", EnumSet.of(Path.Type.directory)));
        assertEquals(1, urls.size());
        final DescriptiveUrl url = urls.find(DescriptiveUrl.Type.http);
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.http, url.getType());
        assertEquals("https://webmail.freenet.de/web/?goTo=share&path=/d#cloud", url.getUrl());
    }

    @Test
    public void testToUrlFile() {
        final FreenetUrlProvider provider = new FreenetUrlProvider(new Host(new FreenetProtocol(), "dav.freenet.de", 443, "/webdav"));
        final DescriptiveUrlBag urls = provider.toUrl(new Path("/webdav/d/f", EnumSet.of(Path.Type.file)));
        assertEquals(1, urls.size());
        final DescriptiveUrl url = urls.find(DescriptiveUrl.Type.http);
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.http, url.getType());
        assertEquals("https://webmail.freenet.de/web/?goTo=share&path=/d#cloud", url.getUrl());
    }
}
