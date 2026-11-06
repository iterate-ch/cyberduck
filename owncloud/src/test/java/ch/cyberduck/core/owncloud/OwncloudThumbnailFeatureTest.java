package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

public class OwncloudThumbnailFeatureTest {

    @Test
    public void testDimension() {
        assertEquals(1, OwncloudThumbnailFeature.toDimension(0));
        assertEquals(128, OwncloudThumbnailFeature.toDimension(128));
        assertEquals(2048, OwncloudThumbnailFeature.toDimension(4096));
    }
}
