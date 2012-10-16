package ch.cyberduck.core.transfer.normalizer;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class CopyRootPathsNormalizerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testNormalizeNone() throws Exception {
        CopyRootPathsNormalizer normalizer = new CopyRootPathsNormalizer();
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        files.put(new NullPath("/p", Path.DIRECTORY_TYPE), new NullPath("/d", Path.DIRECTORY_TYPE));
        assertEquals(files, normalizer.normalize(files));
    }

    @Test
    public void testNormalize() throws Exception {
        CopyRootPathsNormalizer normalizer = new CopyRootPathsNormalizer();
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        files.put(new NullPath("/p", Path.DIRECTORY_TYPE), new NullPath("/d", Path.DIRECTORY_TYPE));
        files.put(new NullPath("/p/child", Path.DIRECTORY_TYPE), new NullPath("/d/child", Path.DIRECTORY_TYPE));
        assertEquals(Collections.singletonMap(new NullPath("/p", Path.DIRECTORY_TYPE), new NullPath("/d", Path.DIRECTORY_TYPE)), normalizer.normalize(files));
    }

    @Test
    public void testNormalize2() throws Exception {
        CopyRootPathsNormalizer normalizer = new CopyRootPathsNormalizer();
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        files.put(new NullPath("/p/child", Path.DIRECTORY_TYPE), new NullPath("/d/child", Path.DIRECTORY_TYPE));
        files.put(new NullPath("/p", Path.DIRECTORY_TYPE), new NullPath("/d", Path.DIRECTORY_TYPE));
        assertEquals(Collections.singletonMap(new NullPath("/p", Path.DIRECTORY_TYPE), new NullPath("/d", Path.DIRECTORY_TYPE)), normalizer.normalize(files));
    }
}