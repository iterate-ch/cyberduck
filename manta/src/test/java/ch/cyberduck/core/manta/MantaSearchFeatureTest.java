package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.SearchFilter;

import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MantaSearchFeatureTest extends AbstractMantaTest {

    private static final EnumSet<AbstractPath.Type> TYPE_DIRECTORY = EnumSet.of(AbstractPath.Type.directory);
    private static final EnumSet<AbstractPath.Type> TYPE_FILE = EnumSet.of(AbstractPath.Type.file);

    @Test
    public void testSearchFileNotFound() throws Exception {
        Assume.assumeTrue(session.getClient().existsAndIsAccessible(testPathPrefix.getAbsolute()));

        final String emptyDirectoryName = new AlphanumericRandomStringService().random();
        final Path emptyDirectory = new Path(testPathPrefix, emptyDirectoryName, EnumSet.of(AbstractPath.Type.directory));
        new MantaDirectoryFeature(session).mkdir(emptyDirectory, null, null);

        final MantaSearchFeature s = new MantaSearchFeature(session);
        final AttributedList<Path> search = s.search(emptyDirectory, new NullFilter<>(), new DisabledListProgressListener());

        assertTrue(search.isEmpty());
    }

    @Test
    public void testSearchSameDirectory() throws Exception {
        Assume.assumeTrue(session.getClient().existsAndIsAccessible(testPathPrefix.getAbsolute()));

        final String newDirectoryName = new AlphanumericRandomStringService().random();
        final Path newDirectory = new Path(testPathPrefix, newDirectoryName, TYPE_DIRECTORY);
        final String newFileName = new AlphanumericRandomStringService().random();
        new MantaDirectoryFeature(session).mkdir(newDirectory, null, null);
        new MantaTouchFeature(session).touch(new Path(newDirectory, newFileName, TYPE_FILE), null);

        final MantaSearchFeature s = new MantaSearchFeature(session);
        final AttributedList<Path> search = s.search(newDirectory, new NullFilter<>(), new DisabledListProgressListener());

        assertEquals(1, search.size());
        assertEquals(newFileName, search.get(0).getName());
    }

    @Test
    public void testSearchNestedDirectory() throws Exception {
        Assume.assumeTrue(session.getClient().existsAndIsAccessible(testPathPrefix.getAbsolute()));

        final String newDirectoryName = new AlphanumericRandomStringService().random();
        final String intermediateDirectoryName = new AlphanumericRandomStringService().random();
        final String intermediateFileName = new AlphanumericRandomStringService().random();
        final String nestedFileName = new AlphanumericRandomStringService().random();

        final Path newDirectory = new Path(testPathPrefix, newDirectoryName, TYPE_DIRECTORY);
        final Path intermediateDirectory = new Path(newDirectory, intermediateDirectoryName, TYPE_DIRECTORY);
        new MantaDirectoryFeature(session).mkdir(newDirectory, null, null);
        new MantaDirectoryFeature(session).mkdir(intermediateDirectory, null, null);
        new MantaTouchFeature(session).touch(new Path(newDirectory, intermediateFileName, TYPE_FILE), null);
        new MantaTouchFeature(session).touch(new Path(intermediateDirectory, nestedFileName, TYPE_FILE), null);

        final MantaSearchFeature s = new MantaSearchFeature(session);
        final AttributedList<Path> search = s.search(newDirectory, new NullFilter<>(), new DisabledListProgressListener());
        final Path foundIntermediateFile =
            search.find(f -> f.getName().equals(intermediateFileName) && f.getParent().getName().equals(newDirectoryName));
        final Path foundNestedFile =
            search.find(f -> f.getName().equals(nestedFileName) && f.getParent().getName().equals(intermediateDirectoryName));

        assertEquals(3, search.size());
        assertNotNull(foundIntermediateFile);
        assertNotNull(foundNestedFile);

        final AttributedList<Path> filteredIntermediateFileSearch = s.search(newDirectory, new SearchFilter(intermediateFileName), new DisabledListProgressListener());
        final Path foundFilteredIntermediateFile =
            search.find(f -> f.getName().equals(intermediateFileName) && f.getParent().getName().equals(newDirectoryName));
        assertEquals(1, filteredIntermediateFileSearch.size());
        assertNotNull(foundFilteredIntermediateFile);

        final AttributedList<Path> filteredNestedFileSearch = s.search(newDirectory, new Filter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return file.isDirectory() || file.getName().matches(nestedFileName);
            }

            @Override
            public Pattern toPattern() {
                return Pattern.compile(nestedFileName);
            }
        }, new DisabledListProgressListener());
        final Path foundFilteredNestedFile =
            search.find(f -> f.getName().equals(nestedFileName) && f.getParent().getName().equals(intermediateDirectoryName));
        assertEquals(1, filteredNestedFileSearch.size());
        assertNotNull(foundFilteredNestedFile);
    }

}
