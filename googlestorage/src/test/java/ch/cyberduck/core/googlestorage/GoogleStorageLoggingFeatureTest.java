package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageLoggingFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testGetConfiguration() throws Exception {
        final GoogleStorageLoggingFeature feature = new GoogleStorageLoggingFeature(session);
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        feature.setConfiguration(bucket, new LoggingConfiguration(true, "cyberduck-test-eu"));
        final LoggingConfiguration configuration = feature.getConfiguration(bucket);
        assertNotNull(configuration);
        assertEquals("cyberduck-test-eu", configuration.getLoggingTarget());
        assertTrue(configuration.isEnabled());
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        new GoogleStorageLoggingFeature(session).getConfiguration(
            new Path(new AsciiRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory))
        );
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        new GoogleStorageLoggingFeature(session).setConfiguration(
            new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory)), new LoggingConfiguration(false)
        );
    }
}
