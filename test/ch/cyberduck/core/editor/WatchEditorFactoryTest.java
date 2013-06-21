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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class WatchEditorFactoryTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        LaunchServicesApplicationFinder.register();
        WatchEditorFactory.register();
    }

    @Test
    public void testGetEditor() throws Exception {
        assertEquals("TextEdit", EditorFactory.instance().getDefaultEditor().getName());
        assertEquals("TextEdit", EditorFactory.instance().getEditor("f.txt").getName());
        assertEquals("Preview", EditorFactory.instance().getEditor("f.png").getName());
    }

    @Test
    public void getGetConfigured() throws Exception {
        final List<Application> e = EditorFactory.instance().getConfigured();
        assertFalse(e.isEmpty());
    }

    @Test
    public void testGetEditors() throws Exception {
        final List<Application> e = EditorFactory.instance().getEditors();
        assertFalse(e.isEmpty());
        assertTrue(e.contains(new Application("com.apple.TextEdit", null)));
        assertFalse(EditorFactory.instance().getEditors("f.txt").isEmpty());
        assertTrue(EditorFactory.instance().getEditors("f.txt").contains(new Application("com.apple.TextEdit", null)));
//        assertTrue(EditorFactory.instance().getEditors("f.txt").contains(new Application("com.macromates.textmate", null)));
    }
}