package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.ISO8601DateFormatter;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class DefaultVersioningFeatureTest {

    @Test
    public void testToVersioned() {
        {
            Path f = new Path("/d/f", EnumSet.of(Path.Type.file));
            final Path versioned = DefaultVersioningFeature.toVersioned(f, new ISO8601DateFormatter());
            assertEquals(".cyberduckversions", versioned.getParent().getName());
            assertEquals(f, DefaultVersioningFeature.fromVersioned(versioned));
        }
        {
            Path f = new Path("/d/f.ext", EnumSet.of(Path.Type.file));
            final Path versioned = DefaultVersioningFeature.toVersioned(f, new ISO8601DateFormatter());
            assertEquals(".cyberduckversions", versioned.getParent().getName());
            assertEquals(f, DefaultVersioningFeature.fromVersioned(versioned));
        }
        {
            Path f = new Path("/d/f-f", EnumSet.of(Path.Type.file));
            final Path versioned = DefaultVersioningFeature.toVersioned(f, new ISO8601DateFormatter());
            assertEquals(".cyberduckversions", versioned.getParent().getName());
            assertEquals(f, DefaultVersioningFeature.fromVersioned(versioned));
        }
        {
            Path f = new Path("/d/f-f.ext", EnumSet.of(Path.Type.file));
            final Path versioned = DefaultVersioningFeature.toVersioned(f, new ISO8601DateFormatter());
            assertEquals(".cyberduckversions", versioned.getParent().getName());
            assertEquals(f, DefaultVersioningFeature.fromVersioned(versioned));
        }
    }
}