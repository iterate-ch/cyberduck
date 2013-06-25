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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.ui.Controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Id$
 */
public class MultipleEditorFactory extends EditorFactory {

    public static void register() {
        EditorFactory.addFactory(Factory.NATIVE_PLATFORM, new MultipleEditorFactory());
    }

    private EditorFactory watchEditorFactory
            = new WatchEditorFactory();

    private EditorFactory odbEditorFactory
            = new ODBEditorFactory();

    private final Set<Application> editors = new HashSet<Application>();

    protected MultipleEditorFactory() {
        editors.addAll(watchEditorFactory.getEditors());
        editors.addAll(odbEditorFactory.getEditors());
    }

    @Override
    public List<Application> getConfigured() {
        return new ArrayList<Application>(editors);
    }

    @Override
    public Editor create(final Controller c, final Session session, final Application application, final Path path) {
        if(odbEditorFactory.getConfigured().contains(application)) {
            return odbEditorFactory.create(c, session, application, path);
        }
        return watchEditorFactory.create(c, session, application, path);
    }

    @Override
    protected Editor create() {
        throw new FactoryException("Not supported");
    }
}
