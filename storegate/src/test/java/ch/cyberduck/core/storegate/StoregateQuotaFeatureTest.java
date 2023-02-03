package ch.cyberduck.core.storegate;

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

import ch.cyberduck.core.features.Quota;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class StoregateQuotaFeatureTest extends AbstractStoregateTest {

    @Test
    public void get() throws Exception {
        final Quota.Space quota = new StoregateQuotaFeature(session, new StoregateIdProvider(session)).get();
        assertNotNull(quota.available);
        assertNotNull(quota.used);
        assertNotEquals(0L, quota.available, 0L);
        assertNotEquals(0L, quota.used, 0L);
        assertTrue(quota.available < quota.available + quota.used);
    }
}
