package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class WatchEditorFactory extends EditorFactory {
    private final List<Application> editors = new ArrayList<Application>();

    public static void register() {
        EditorFactory.addFactory(WatchEditorFactory.NATIVE_PLATFORM, new WatchEditorFactory());
    }

    protected WatchEditorFactory() {
        editors.add(new Application("de.codingmonkeys.SubEthaEdit", "SubEthaEdit"));
        editors.add(new Application("com.apple.TextEdit", "TextEdit"));
        editors.add(new Application("com.apple.Xcode", "Xcode"));
    }

    @Override
    public List<Application> getConfigured() {
        return editors;
    }

    @Override
    public Editor create(final Controller c, final Application application, final Path path) {
        return new WatchEditor(c, application, path);
    }

    @Override
    protected Editor create() {
        throw new FactoryException("Not supported");
    }
}