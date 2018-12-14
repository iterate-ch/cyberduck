package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3LoggingFeatureTest extends AbstractS3Test {

    @Test
    public void testGetConfiguration() throws Exception {
        final S3LoggingFeature feature = new S3LoggingFeature(session);
        final Path bucket = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        feature.setConfiguration(bucket, new LoggingConfiguration(true, "test-logging-us-east-1-cyberduck"));
        final LoggingConfiguration configuration = feature.getConfiguration(bucket);
        assertNotNull(configuration);
        assertEquals("test-logging-us-east-1-cyberduck", configuration.getLoggingTarget());
        assertTrue(configuration.isEnabled());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        new S3LoggingFeature(session).getConfiguration(
            new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory))
        );
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        new S3LoggingFeature(session).setConfiguration(
            new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new LoggingConfiguration(false)
        );
    }
}
