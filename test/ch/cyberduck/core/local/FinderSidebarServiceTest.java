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
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;
import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * @version $Id:$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class FinderSidebarServiceTest extends AbstractTestCase {

    @Test(expected = LocalAccessDeniedException.class)
    public void testAddNotFound() throws Exception {
        FinderSidebarService f = new FinderSidebarService();
        f.add(LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir")));
    }

    @Test
    public void testAddHarddisk() throws Exception {
        FinderSidebarService f = new FinderSidebarService();
        final NSArray volumes = NSWorkspace.sharedWorkspace().mountedLocalVolumePaths();
        for(int i = 0; i < volumes.count().intValue(); i++) {
            final Local volume = LocalFactory.get(volumes.objectAtIndex(new NSUInteger(i)).toString());
            f.add(volume);
            f.remove(volume);
        }
    }

    @Test
    public void testAddMountedVolume() throws Exception {
        FinderSidebarService f = new FinderSidebarService();
        f.add(LocalFactory.get("/Users/dkocher/Library/Containers/io.mountainduck/Data/Library/Application Support/Mountain Duck/Volumes/s3.amazonaws.com"));
    }

    @Test
    public void testRemove() throws Exception {
        FinderSidebarService f = new FinderSidebarService();
        f.remove(LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir")));
    }
}