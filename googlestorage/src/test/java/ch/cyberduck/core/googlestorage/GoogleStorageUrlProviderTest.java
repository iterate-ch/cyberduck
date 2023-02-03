package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class GoogleStorageUrlProviderTest extends AbstractGoogleStorageTest {

    @Test
    public void testGet() {
        assertEquals("https://storage.cloud.google.com/c/f", new GoogleStorageUrlProvider(session).toUrl(
            new Path("/c/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.authenticated).getUrl());
    }

    @Test
    public void testGetEncoded() {
        assertEquals("https://storage.cloud.google.com/container/Screen%20Shot%202013-07-18%20at%2023.55.10.png", new GoogleStorageUrlProvider(session).toUrl(
            new Path("/container/Screen Shot 2013-07-18 at 23.55.10.png", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.authenticated).getUrl());
    }

    @Test
    public void testWebsiteConfiguration() {
        final GoogleStorageUrlProvider provider = new GoogleStorageUrlProvider(session);
        assertEquals("http://test.cyberduck.ch.storage.googleapis.com/f", provider.toUrl(new Path("test.cyberduck.ch/f", EnumSet.of(Path.Type.file))).find(
            DescriptiveUrl.Type.origin).getUrl());
    }
}
