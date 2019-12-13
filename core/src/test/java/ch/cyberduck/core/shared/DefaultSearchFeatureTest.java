package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class DefaultSearchFeatureTest {

    @Test
    public void testSearch() throws Exception {
        final Path workdir = new Path("/", EnumSet.of(Path.Type.directory));
        final Path f1 = new Path(workdir, "f1", EnumSet.of(Path.Type.file));
        final Path f2 = new Path(workdir, "f2", EnumSet.of(Path.Type.file));
        final DefaultSearchFeature feature = new DefaultSearchFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                if(folder.equals(workdir)) {
                    final AttributedList<Path> list = new AttributedList<>(Arrays.asList(f1, f2));
                    listener.chunk(folder, list);
                    return list;
                }
                return AttributedList.emptyList();
            }
        });
        final Filter<Path> filter = new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                if(file.isDirectory()) {
                    return true;
                }
                return file.getName().equals("f1");
            }
        };
        final AttributedList<Path> search = feature.search(workdir, filter, new DisabledListProgressListener());
        assertTrue(search.contains(f1));
        assertFalse(search.contains(f2));
        assertEquals(1, search.size());
    }
}
