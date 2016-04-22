package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class GoogleStorageUrlProviderTest {

    @Test
    public void testGet() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertEquals("https://storage.cloud.google.com/c/f", new GoogleStorageUrlProvider(session).toUrl(
                new Path("/c/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.authenticated).getUrl());
    }

    @Test
    public void testGetEncoded() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertEquals("https://storage.cloud.google.com/container/Screen%20Shot%202013-07-18%20at%2023.55.10.png", new GoogleStorageUrlProvider(session).toUrl(
                new Path("/container/Screen Shot 2013-07-18 at 23.55.10.png", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.authenticated).getUrl());
    }
}
