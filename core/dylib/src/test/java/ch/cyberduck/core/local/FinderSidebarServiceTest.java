package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2015 David Kocher & Yves Langisch. All rights reserved.
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.io
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FinderSidebarServiceTest {

    @Test
    public void testAddDirectoryFavorite() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        final Local file = new FinderLocal(PreferencesFactory.get().getProperty("tmp.dir"));
        f.add(file);
        assertTrue(f.contains(file));
        f.remove(file);
        assertFalse(f.contains(file));
    }

    @Test
    public void testAddDirectoryVolumes() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.volume);
        final Local file = new FinderLocal(PreferencesFactory.get().getProperty("tmp.dir"));
        f.add(file);
        assertTrue(f.contains(file));
        f.remove(file);
    }

    @Ignore
    @Test
    public void testAddDirectoryServer() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.server);
        final Local file = new FinderLocal(PreferencesFactory.get().getProperty("tmp.dir"));
        f.add(file);
        assertTrue(f.contains(file));
        f.remove(file);
    }

    @Test
    public void testAddFile() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        new DefaultLocalTouchFeature().touch(l);
        f.add(l);
        assertTrue(f.contains(l));
        f.remove(l);
        assertFalse(f.contains(l));
        l.delete();
    }
}
