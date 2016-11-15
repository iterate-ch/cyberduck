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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class EditorFactory extends Factory<EditorFactory> {
    private static final Logger log = Logger.getLogger(EditorFactory.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final ApplicationFinder applicationFinder;

    public EditorFactory() {
        this(ApplicationFinderFactory.get());
    }

    public EditorFactory(final ApplicationFinder applicationFinder) {
        this.applicationFinder = applicationFinder;
    }

    private static EditorFactory factory;

    public static synchronized EditorFactory instance() {
        if(null == factory) {
            try {
                final String clazz = PreferencesFactory.get().getProperty("factory.editorfactory.class");
                if(null == clazz) {
                    throw new FactoryException();
                }
                final Class<EditorFactory> name = (Class<EditorFactory>) Class.forName(clazz);
                factory = name.newInstance();
            }
            catch(InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                throw new FactoryException(e.getMessage(), e);
            }
        }
        return factory;
    }

    /**
     * @return All statically registered but possibly not installed editors.
     */
    protected abstract List<Application> getConfigured();

    public List<Application> getEditors() {
        final List<Application> editors = new ArrayList<Application>(this.getConfigured());
        // Add the application set as the default editor in the Preferences to be always
        // included in the list of available editors.
        final Application defaultEditor = this.getDefaultEditor();
        if(applicationFinder.isInstalled(defaultEditor)) {
            if(!editors.contains(defaultEditor)) {
                editors.add(defaultEditor);
            }
        }
        return editors;
    }

    /**
     * @param listener Controller
     * @param path     File to edit
     * @return New editor instance for the given file type.
     */
    public Editor create(final ProgressListener listener, final Session session, final Path path) {
        return this.create(listener, session, this.getEditor(path.getName()), path);
    }

    /**
     * @param listener    Controller
     * @param application The application bundle identifier of the editor to use
     * @param path        File to edit
     * @return New editor instance for the given file type.
     */
    public abstract Editor create(ProgressListener listener, Session session, Application application, Path path);

    /**
     * Determine the default editor set
     *
     * @return The bundle identifier of the default editor configured in
     * Preferences or com.apple.TextEdit if not installed.
     */
    public Application getDefaultEditor() {
        final Application application = applicationFinder.getDescription(
                preferences.getProperty("editor.bundleIdentifier"));
        if(applicationFinder.isInstalled(application)) {
            return application;
        }
        return Application.notfound;
    }

    /**
     * @param filename File type
     * @return The bundle identifier of the application for this file or null if no
     * suitable and installed editor is found.
     */
    public Application getEditor(final String filename) {
        final Application application = this.getDefaultEditor();
        if(preferences.getBoolean("editor.alwaysUseDefault")) {
            return application;
        }
        // The default application set by launch services to open files of the given type
        final Application editor = applicationFinder.find(filename);
        if(!applicationFinder.isInstalled(editor)) {
            log.warn(String.format("No editor found for %s", filename));
            // Use default editor if not applicable application found which handles this file type
            return application;
        }
        // Use application determined by launch services using file system notifications
        return editor;
    }

    /**
     * @param filename File type
     * @return Installed applications suitable to edit the given file type. Does always include
     * the default editor set in the Preferences
     */
    public List<Application> getEditors(final String filename) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find installed editors for file %s", filename));
        }
        final List<Application> editors = new ArrayList<Application>(
                applicationFinder.findAll(filename));
        // Add the application set as the default editor in the Preferences to be always
        // included in the list of available editors.
        final Application defaultEditor = this.getDefaultEditor();
        if(applicationFinder.isInstalled(defaultEditor)) {
            if(!editors.contains(defaultEditor)) {
                editors.add(defaultEditor);
            }
        }
        return editors;
    }
}