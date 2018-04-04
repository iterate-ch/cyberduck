package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class WebUrlProviderTest {

    @Test
    public void testToUrl() {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        assertEquals("http://test.cyberduck.ch/", new WebUrlProvider(host).toUrl().getUrl());
        assertEquals("http://test.cyberduck.ch/my/documentroot/f",
            new WebUrlProvider(host).toUrl(new Path("/my/documentroot/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testCustom() {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        host.setWebURL("customhost");
        assertEquals("http://customhost/", new WebUrlProvider(host).toUrl().getUrl());
        assertEquals("http://customhost/my/documentroot/f",
            new WebUrlProvider(host).toUrl(new Path("/my/documentroot/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.http).getUrl());
        host.setWebURL("https://customhost/");
        assertEquals("https://customhost/", new WebUrlProvider(host).toUrl().getUrl());
        assertEquals("https://customhost/my/documentroot/f",
            new WebUrlProvider(host).toUrl(new Path("/my/documentroot/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.http).getUrl());
        host.setWebURL("https://customhost");
        assertEquals("https://customhost", new WebUrlProvider(host).toUrl().getUrl());
        assertEquals("https://customhost/my/documentroot/f",
            new WebUrlProvider(host).toUrl(new Path("/my/documentroot/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.http).getUrl());
    }
}
