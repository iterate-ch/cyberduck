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
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class S3LifecycleConfigurationTest extends AbstractS3Test {

    @Test
    public void testGetConfiguration() throws Exception {
        assertEquals(30, new S3LifecycleConfiguration(session).getConfiguration(
            new Path("test-lifecycle-us-east-1-cyberduck", EnumSet.of(Path.Type.directory))
        ).getExpiration(), 0L);
        assertEquals(1, new S3LifecycleConfiguration(session).getConfiguration(
            new Path("test-lifecycle-us-east-1-cyberduck", EnumSet.of(Path.Type.directory))
        ).getTransition(), 0L);
        session.close();
    }

    @Test
    public void testGetConfigurationAccessDenied() throws Exception {
        assertEquals(LifecycleConfiguration.empty(), new S3LifecycleConfiguration(session).getConfiguration(
            new Path("bucket", EnumSet.of(Path.Type.directory))
        ));
        session.close();
    }
}
