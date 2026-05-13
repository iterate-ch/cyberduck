package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultComparisonServiceTest {

    @Test
    public void testCompareFile() {
        final DefaultComparisonService c = new DefaultComparisonService(new TestProtocol());
        assertEquals(Comparison.equal, c.compare(Path.Type.file, new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("1")));
        assertEquals(1519, c.hashCode(Path.Type.file, new DefaultPathAttributes().setETag("1")));
        assertEquals(Comparison.unknown, c.compare(Path.Type.file, new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("2")));
        assertEquals(Comparison.equal, c.compare(Path.Type.file, new DefaultPathAttributes().setETag("1").setSize(1000L), new DefaultPathAttributes().setETag("2").setSize(1000L)));
        assertEquals(Comparison.equal, c.compare(Path.Type.file, new DefaultPathAttributes().setETag("1").setSize(1000L).setModificationDate(1680879106939L), new DefaultPathAttributes().setETag("2").setSize(1000L).setModificationDate(1680879106939L)));
        assertEquals(709040760, c.hashCode(Path.Type.file, new DefaultPathAttributes().setETag("1").setModificationDate(1680879106939L)));
        assertEquals(709040760, c.hashCode(Path.Type.file, new DefaultPathAttributes().setETag("1").setModificationDate(1680879106000L)));
        assertEquals(Comparison.local, c.compare(Path.Type.file, new DefaultPathAttributes().setETag("1").setSize(1000L).setModificationDate(1680879107939L), new DefaultPathAttributes().setETag("2").setSize(1000L).setModificationDate(1680879106939L)));
    }

    @Test
    public void testCompareDirectories() {
        {
            final DefaultComparisonService c = new DefaultComparisonService(new TestProtocol() {
                @Override
                public DirectoryTimestamp getDirectoryTimestamp() {
                    return DirectoryTimestamp.implicit;
                }
            });
            assertEquals(Comparison.equal, c.compare(Path.Type.directory, new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("1")));
            assertEquals(1519, c.hashCode(Path.Type.directory, new DefaultPathAttributes().setETag("1")));
            assertEquals(Comparison.notequal, c.compare(Path.Type.directory, new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("2")));
            assertEquals(Comparison.equal, c.compare(Path.Type.directory, new DefaultPathAttributes().setModificationDate(1680879106939L), new DefaultPathAttributes().setModificationDate(1680879106939L)));
            assertEquals(1546892887, c.hashCode(Path.Type.directory, new DefaultPathAttributes().setModificationDate(1680879106939L)));
            assertEquals(1546892887, c.hashCode(Path.Type.directory, new DefaultPathAttributes().setModificationDate(1680879106000L)));
            assertEquals(Comparison.local, c.compare(Path.Type.directory, new DefaultPathAttributes().setModificationDate(1680879107939L), new DefaultPathAttributes().setModificationDate(1680879106939L)));
        }
        {
            final DefaultComparisonService c = new DefaultComparisonService(new TestProtocol() {
                @Override
                public DirectoryTimestamp getDirectoryTimestamp() {
                    return DirectoryTimestamp.explicit;
                }
            });
            assertEquals(0, c.hashCode(Path.Type.directory, new DefaultPathAttributes()));
            assertEquals(Comparison.equal, c.compare(Path.Type.directory, new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("1")));
            assertEquals(1519, c.hashCode(Path.Type.directory, new DefaultPathAttributes().setETag("1")));
            assertEquals(Comparison.notequal, c.compare(Path.Type.directory, new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("2")));
            assertEquals(Comparison.unknown, c.compare(Path.Type.directory, new DefaultPathAttributes().setModificationDate(1680879106939L), new DefaultPathAttributes().setModificationDate(1680879106939L)));
            assertEquals(0, c.hashCode(Path.Type.directory, new DefaultPathAttributes().setModificationDate(1680879106939L)));
            assertEquals(Comparison.unknown, c.compare(Path.Type.directory, new DefaultPathAttributes().setModificationDate(1680879107939L), new DefaultPathAttributes().setModificationDate(1680879106939L)));
        }
    }

    @Test
    public void testCompareSymlink() {
        final DefaultComparisonService c = new DefaultComparisonService(new TestProtocol());
        assertEquals(Comparison.equal, c.compare(Path.Type.symboliclink, new DefaultPathAttributes().setModificationDate(1680879106939L), new DefaultPathAttributes().setModificationDate(1680879106939L)));
        assertEquals(Comparison.local, c.compare(Path.Type.symboliclink, new DefaultPathAttributes().setModificationDate(1680879107939L), new DefaultPathAttributes().setModificationDate(1680879106939L)));
    }
}