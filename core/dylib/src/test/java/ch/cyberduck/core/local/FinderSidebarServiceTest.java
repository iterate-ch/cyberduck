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

import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.UUID;

@Ignore
public class FinderSidebarServiceTest {

    @Test
    public void testAddNotFound() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        final Local file = LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir"));
        f.add(file);
        f.remove(file);
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testRemoveNotfound() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        final Local file = LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir"));
        f.remove(file);
    }

    @Test
    public void testAddMountedVolumesInFavorites() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        final NSArray volumes = NSWorkspace.sharedWorkspace().mountedLocalVolumePaths();
        for(int i = 0; i < volumes.count().intValue(); i++) {
            final Local volume = LocalFactory.get(volumes.objectAtIndex(new NSUInteger(i)).toString());
            f.add(volume);
            f.remove(volume);
        }
    }

    @Test
    @Ignore
    public void testAddMountedVolumesInVolumes() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.volume);
        final NSArray volumes = NSWorkspace.sharedWorkspace().mountedLocalVolumePaths();
        for(int i = 0; i < volumes.count().intValue(); i++) {
            final Local volume = LocalFactory.get(volumes.objectAtIndex(new NSUInteger(i)).toString());
            f.add(volume);
            f.remove(volume);
        }
    }

    @Test
    @Ignore
    public void testAddMountedVolumesInServers() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.server);
        final NSArray volumes = NSWorkspace.sharedWorkspace().mountedLocalVolumePaths();
        for(int i = 0; i < volumes.count().intValue(); i++) {
            final Local volume = LocalFactory.get(volumes.objectAtIndex(new NSUInteger(i)).toString());
            f.add(volume);
            f.remove(volume);
        }
    }

    @Test
    public void testAddTemporaryFileInFavorites() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        new DefaultLocalTouchFeature().touch(l);
        f.add(l);
        f.remove(l);
        l.delete();
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testRemoveFail() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        new DefaultLocalTouchFeature().touch(l);
        f.add(l);
        l.delete();
        f.remove(l);
    }

    @Test
    public void testAddTemporaryDirectoryInFavorites() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        l.mkdir();
        FinderLocal t = new FinderLocal(l, name);
        new DefaultLocalTouchFeature().touch(t);
        f.add(l);
        f.remove(l);
        t.delete();
        l.delete();
    }

    @Test
    @Ignore
    public void testAddTemporaryFileInVolumes() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.volume);
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        new DefaultLocalTouchFeature().touch(l);
        f.add(l);
        f.remove(l);
        l.delete();
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testRemove() throws Exception {
        FinderSidebarService f = new FinderSidebarService(SidebarService.List.favorite);
        f.remove(LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir")));
    }
}