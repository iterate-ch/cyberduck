package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DriveDefaultPathHomeFeatureTest {

    @Test
    public void testFindDefaultPath() throws Exception {
        final Path home = new DriveDefaultPathHomeFeature(this.bookmark("/My Drive/Documents/Invoices"),
                new TestAttributesFinder()
                        .add("f1", "My Drive")
                        .add("f2", "My Drive", "Documents")
                        .add("f3", "My Drive", "Documents", "Invoices")).find();
        assertNotNull(home);
        assertEquals("Invoices", home.getName());
        assertEquals("f3", home.attributes().getFileId());
        assertEquals("Documents", home.getParent().getName());
        assertEquals("My Drive", home.getParent().getParent().getName());
        assertTrue(home.getParent().getParent().getParent().isRoot());
    }

    @Test
    public void testFindSharedDriveNameWithDelimiter() throws Exception {
        // Shared Drive named Либо/Либо with trailing space in bookmark /Shared Drives/Либо/Либо /Подкасты
        final Path home = new DriveDefaultPathHomeFeature(this.bookmark("/Shared Drives/Либо/Либо /Подкасты"),
                new TestAttributesFinder()
                        .add("f1", "Shared Drives")
                        .add("f2", "Shared Drives", "Либо/Либо ")
                        .add("f3", "Shared Drives", "Либо/Либо ", "Подкасты")).find();
        assertNotNull(home);
        assertEquals("Подкасты", home.getName());
        assertEquals("f3", home.attributes().getFileId());
        assertEquals("Либо/Либо ", home.getParent().getName());
        assertEquals("f2", home.getParent().attributes().getFileId());
        assertEquals("Shared Drives", home.getParent().getParent().getName());
    }

    @Test
    public void testFindBacktrackAmbiguousMatch() throws Exception {
        // Both a Shared Drive named a and a Shared Drive named a/b exist with only the latter containing c
        final Path home = new DriveDefaultPathHomeFeature(this.bookmark("/Shared Drives/a/b/c"),
                new TestAttributesFinder()
                        .add("f1", "Shared Drives")
                        .add("f2", "Shared Drives", "a")
                        .add("f3", "Shared Drives", "a/b")
                        .add("f4", "Shared Drives", "a/b", "c")).find();
        assertNotNull(home);
        assertEquals("c", home.getName());
        assertEquals("f4", home.attributes().getFileId());
        assertEquals("a/b", home.getParent().getName());
    }

    @Test(expected = NotfoundException.class)
    public void testFindNotfound() throws Exception {
        new DriveDefaultPathHomeFeature(this.bookmark("/Shared Drives/a/b"),
                new TestAttributesFinder().add("f1", "Shared Drives")).find();
    }

    @Test
    public void testFindNoDefaultPath() throws Exception {
        assertNull(new DriveDefaultPathHomeFeature(this.bookmark(null), new TestAttributesFinder()).find());
    }

    @Test
    public void testFindRootDefaultPath() throws Exception {
        assertTrue(new DriveDefaultPathHomeFeature(this.bookmark("/"), new TestAttributesFinder()).find().isRoot());
    }

    private Host bookmark(final String defaultpath) {
        return new Host(new DriveProtocol(), "www.googleapis.com").setDefaultPath(defaultpath);
    }

    /**
     * Lookup of files by name in parent directory ignoring the ambiguous absolute path
     */
    private static final class TestAttributesFinder implements AttributesFinder {
        private final Map<List<String>, String> files = new HashMap<>();

        public TestAttributesFinder add(final String fileid, final String... path) {
            files.put(Arrays.asList(path), fileid);
            return this;
        }

        @Override
        public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
            if(file.isRoot()) {
                return PathAttributes.EMPTY;
            }
            final List<String> path = new LinkedList<>();
            for(Path parent = file; !parent.isRoot(); parent = parent.getParent()) {
                path.add(0, parent.getName());
            }
            if(!files.containsKey(path)) {
                throw new NotfoundException(file.getAbsolute());
            }
            return new DefaultPathAttributes().setFileId(files.get(path));
        }
    }
}
