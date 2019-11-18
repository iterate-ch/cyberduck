package ch.cyberduck.core.local;

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

import ch.cyberduck.binding.foundation.NSProgress;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class NSProgressTest {

    @Test
    public void progressWithTotalUnitCount() {
        assertNotNull(NSProgress.progressWithTotalUnitCount(0L));
    }
}
