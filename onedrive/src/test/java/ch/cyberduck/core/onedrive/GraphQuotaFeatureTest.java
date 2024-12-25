package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.onedrive.features.GraphQuotaFeature;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphQuotaFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testQuotaSimple() throws BackgroundException {
        assertEquals(Quota.unknown, new GraphQuotaFeature(session, fileid, () -> Home.ROOT).get());
        final Quota quota = new GraphQuotaFeature(session, fileid, new OneDriveHomeFinderService());
        Quota.Space space = quota.get();
        assertNotEquals(Quota.unknown, space);
        assertTrue(space.available > 0);
        assertTrue(space.used >= 0);
    }
}
