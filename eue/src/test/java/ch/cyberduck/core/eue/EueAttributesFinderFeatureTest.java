package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class EueAttributesFinderFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testReadTrash() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path directory = new Path("Gelöschte Dateien", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(new EueFindFeature(session, fileid).find(directory, new DisabledListProgressListener()));
    }
}