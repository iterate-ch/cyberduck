package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomSchemeUrlProviderTest {

    @Test
    public void testCustomSchemes() {
        Host host = new Host(new TestProtocol() {
            public String[] getSchemes() {
                return new String[]{"c1", "c2"};
            }
        }, "localhost");
        Path path = new Path("/file", EnumSet.of(Path.Type.file));
        final DescriptiveUrlBag list = new CustomSchemeUrlProvider(host).toUrl(path).filter(DescriptiveUrl.Type.provider);
        assertEquals(2, list.size());
        assertTrue(list.contains(new DescriptiveUrl("c1://localhost/file")));
        assertTrue(list.contains(new DescriptiveUrl("c2://localhost/file")));
    }

    @Test
    public void testHelp() {
        Host host = new Host(new TestProtocol() {
            public String[] getSchemes() {
                return new String[]{"s"};
            }
        }, "localhost");
        Path path = new Path("/file", EnumSet.of(Path.Type.file));
        final DescriptiveUrlBag list = new CustomSchemeUrlProvider(host).toUrl(path).filter(DescriptiveUrl.Type.provider);
        assertEquals(1, list.size());
        assertEquals("S URL", list.find(DescriptiveUrl.Type.provider).getHelp());
    }
}
