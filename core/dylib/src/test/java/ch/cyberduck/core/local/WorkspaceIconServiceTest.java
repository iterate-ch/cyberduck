/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.local;

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkspaceIconServiceTest {

    @Test
    public void testUpdateProgressNoFile() {
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(WorkspaceIconService.update(file, NSImage.imageWithContentsOfFile("../../img/download0.icns")));
    }

    @Test
    public void testUpdateProgressFolder() throws Exception {
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        new DefaultLocalDirectoryFeature().mkdir(file);
        assertTrue(WorkspaceIconService.update(file, NSImage.imageWithContentsOfFile("../../img/download0.icns")));
    }

    @Test
    public void testUpdateProgress() throws Exception {
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        final WorkspaceIconService s = new WorkspaceIconService();
        LocalTouchFactory.get().touch(file);
        assertTrue(WorkspaceIconService.update(file, NSImage.imageWithContentsOfFile("../../img/download0.icns")));
        file.delete();
    }
}
