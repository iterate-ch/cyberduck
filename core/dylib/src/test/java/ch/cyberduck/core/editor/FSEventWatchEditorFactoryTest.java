package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FSEventWatchEditorFactoryTest {

    @Test
    public void testGetEditor() throws Exception {
        final FSEventWatchEditorFactory f = new FSEventWatchEditorFactory(new LaunchServicesApplicationFinder());
        assertEquals("TextEdit", f.getDefaultEditor().getName());
//        assertEquals("TextEdit", f.getEditor("f.txt").getName());
        assertEquals("Preview", f.getEditor("f.png").getName());
    }

    @Test
    public void getGetConfigured() throws Exception {
        final FSEventWatchEditorFactory f = new FSEventWatchEditorFactory(new LaunchServicesApplicationFinder());
        final List<Application> e = f.getConfigured();
        assertFalse(e.isEmpty());
    }

    @Test
    public void testGetEditors() throws Exception {
        final FSEventWatchEditorFactory f = new FSEventWatchEditorFactory(new LaunchServicesApplicationFinder());
        final List<Application> e = f.getEditors();
        assertFalse(e.isEmpty());
        assertTrue(e.contains(new Application("com.apple.TextEdit", null)));
        assertFalse(f.getEditors("f.txt").isEmpty());
        assertTrue(f.getEditors("f.txt").contains(new Application("com.apple.TextEdit", null)));
//        assertTrue(new WatchEditorFactory().getEditors("f.txt").contains(new Application("com.macromates.textmate", null)));
    }
}