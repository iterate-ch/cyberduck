package ch.cyberduck.ui.cocoa;

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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationUserDefaultsPreferencesTest {

    @Test
    public void testTemporaryDirectory() throws Exception {
        ApplicationUserDefaultsPreferences p = new ApplicationUserDefaultsPreferences();
        p.load();
        p.setDefaults();
        assertTrue(StringUtils.startsWith(p.getProperty("tmp.dir"), System.getProperty("java.io.tmpdir")));
        assertNotEquals(System.getProperty("java.io.tmpdir"), p.getProperty("tmp.dir"));
    }
}
