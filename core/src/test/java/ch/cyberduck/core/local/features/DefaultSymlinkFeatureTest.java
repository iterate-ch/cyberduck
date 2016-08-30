package ch.cyberduck.core.local.features;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultSymlinkFeatureTest {

    @Test
    public void testSymlink() throws Exception {
        final Local target = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(target);
        final Local symlink = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(symlink.exists());
        new DefaultSymlinkFeature().symlink(symlink, target.getAbsolute());
        assertTrue(symlink.exists());
        target.delete();
        symlink.delete();
    }
}