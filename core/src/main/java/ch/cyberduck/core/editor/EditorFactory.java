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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class EditorFactory extends Factory<EditorFactory> {
    private static final Logger log = LogManager.getLogger(EditorFactory.class);

    private static final Preferences preferences = PreferencesFactory.get();
    private static final ApplicationFinder finder = ApplicationFinderFactory.get();

    public static synchronized EditorFactory instance() {
        try {
            final String clazz = PreferencesFactory.get().getProperty("factory.editorfactory.class");
            if(null == clazz) {
                throw new FactoryException();
            }
            final Class<EditorFactory> name = (Class<EditorFactory>) Class.forName(clazz);
            return name.getDeclaredConstructor().newInstance();
        }
        catch(InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    /**
     * @return All statically registered but possibly not installed editors.
     */
    protected abstract List<Application> getConfigured();

    public List<Application> getEditors() {
        final List<Application> editors = new ArrayList<>(getConfigured());
        // Add the application set as the default editor in the Preferences to be always
        // included in the list of available editors.
        final Application defaultEditor = getDefaultEditor();
        if(finder.isInstalled(defaultEditor)) {
            if(!editors.contains(defaultEditor)) {
                editors.add(defaultEditor);
            }
        }
        return editors;
    }

    /**
     * @param host     Bookmark
     * @param file     Remote file to download
     * @param listener Controller
     * @return New editor instance for the given file type.
     */
    public abstract Editor create(final Host host, Path file, ProgressListener listener);

    /**
     * Determine the default editor set
     *
     * @return The bundle identifier of the default editor configured in Preferences or com.apple.TextEdit if not
     * installed.
     */
    public static Application getDefaultEditor() {
        final Application application = finder.getDescription(
                preferences.getProperty("editor.bundleIdentifier"));
        if(finder.isInstalled(application)) {
            return application;
        }
        return Application.notfound;
    }

    /**
     * @param filename File type
     * @return The bundle identifier of the application for this file or null if no suitable and installed editor is
     * found.
     */
    public static Application getEditor(final String filename) {
        final Application application = getDefaultEditor();
        if(preferences.getBoolean("editor.alwaysUseDefault")) {
            return application;
        }
        // The default application set by launch services to open files of the given type
        final Application editor = finder.find(filename);
        if(!finder.isInstalled(editor)) {
            log.warn("No editor found for {}", filename);
            // Use default editor if not applicable application found which handles this file type
            return application;
        }
        // Use application determined by launch services using file system notifications
        return editor;
    }

    /**
     * @param filename File type
     * @return Installed applications suitable to edit the given file type. Does always include the default editor set
     * in the Preferences
     */
    public static List<Application> getEditors(final String filename) {
        if(log.isDebugEnabled()) {
            log.debug("Find installed editors for file {}", filename);
        }
        final List<Application> editors = new ArrayList<>(
                finder.findAll(filename));
        // Add the application set as the default editor in the Preferences to be always
        // included in the list of available editors.
        final Application defaultEditor = getDefaultEditor();
        if(finder.isInstalled(defaultEditor)) {
            if(!editors.contains(defaultEditor)) {
                editors.add(defaultEditor);
            }
        }
        return editors;
    }
}
