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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISO8601FilenameVersionIdentifierTest {

    @Test
    public void testToVersioned() {
        {
            final String filename = "f";
            final String versioned = new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().toVersion(filename);
            assertEquals(filename, new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().fromVersion(versioned));
        }
        {
            final String filename = "f.ext";
            final String versioned = new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().toVersion(filename);
            assertEquals(filename, new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().fromVersion(versioned));
        }
        {
            final String filename = "w-f";
            final String versioned = new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().toVersion(filename);
            assertEquals(filename, new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().fromVersion(versioned));
        }
        {
            final String filename = "w-f.ext";
            final String versioned = new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().toVersion(filename);
            assertEquals(filename, new DefaultVersioningFeature.DefaultFilenameVersionIdentifier().fromVersion(versioned));
        }
    }
}