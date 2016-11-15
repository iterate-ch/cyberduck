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
    public void testSetProgressNoFile() throws Exception {
        final WorkspaceIconService s = new WorkspaceIconService();
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(s.update(file, NSImage.imageWithContentsOfFile("../../img/download0.icns")));
    }

    @Test
    public void testSetProgressFolder() throws Exception {
        final WorkspaceIconService s = new WorkspaceIconService();
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        file.mkdir();
        assertTrue(s.update(file, NSImage.imageWithContentsOfFile("../../img/download0.icns")));
    }

    @Test
    public void testSetProgress() throws Exception {
        final WorkspaceIconService s = new WorkspaceIconService();
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(file);
        assertTrue(s.update(file, NSImage.imageWithContentsOfFile("../../img/download0.icns")));
        file.delete();
    }

    @Test
    public void testRemove() throws Exception {
        final WorkspaceIconService s = new WorkspaceIconService();
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(s.remove(file));
        LocalTouchFactory.get().touch(file);
        assertFalse(s.remove(file));
        assertTrue(s.update(file, NSImage.imageWithContentsOfFile("../../img/download0.icns")));
        assertTrue(s.remove(file));
        file.delete();
    }
}