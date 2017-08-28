package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DropboxQuotaFeatureTest extends AbstractDropboxTest {

    @Test
    public void testGet() throws Exception {
        final Quota.Space quota = new DropboxQuotaFeature(session).get();
        assertNotNull(quota);
        assertNotEquals(-1, quota.available, 0L);
        assertNotEquals(-1, quota.used, 0L);
    }
}