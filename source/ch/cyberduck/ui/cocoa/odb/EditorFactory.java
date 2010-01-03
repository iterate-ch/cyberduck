package ch.cyberduck.ui.cocoa.odb;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class EditorFactory {
    private static Logger log = Logger.getLogger(EditorFactory.class);

    /**
     * @return The bundle identifier of the default editor configured in
     *         Preferences or null if not installed.
     */
    public static String defaultEditor() {
        if(null == NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                Preferences.instance().getProperty("editor.bundleIdentifier")
        )) {
            return null;
        }
        return Preferences.instance().getProperty("editor.bundleIdentifier");
    }

    /**
     * @param file
     * @return The bundle identifier of the editor for this file or null if no
     *         suitable and installed editor is found.
     */
    public static String editorForFile(final Local file) {
        if(Preferences.instance().getBoolean("editor.alwaysUseDefault")) {
            return defaultEditor();
        }
        // The default application set by launch services to open files of
        // the given type
        final String defaultApplication = file.getDefaultApplication();
        if(null == defaultApplication) {
            // Use default editor if not applicable application found which handles this file type
            return defaultEditor();
        }
        // Find matching ODB editor if any
        for(final String identifier : ODBEditor.getInstalledEditors().values()) {
            final String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier);
            if(null == path) {
                continue;
            }
            if(path.equals(defaultApplication)) {
                return identifier;
            }
        }
        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
            // Use application determined by launch services using file system notifications
            return defaultApplication;
        }
        if(ODBEditor.getInstalledEditors().containsValue(Preferences.instance().getProperty("editor.bundleIdentifier"))) {
            // Use default editor
            return defaultEditor();
        }
        log.warn("No editor for file type " + file.getExtension());
        return null;
    }

    /**
     * @param c
     * @param path
     * @return
     */
    public static Editor createEditor(BrowserController c, final Path path) {
        return createEditor(c, editorForFile(path.getLocal()), path);
    }

    /**
     * @param c
     * @param bundleIdentifier The application bundle identifier of the editor to use
     * @param path
     * @return
     */
    public static Editor createEditor(BrowserController c, String bundleIdentifier, final Path path) {
        if(ODBEditor.getInstalledEditors().containsValue(bundleIdentifier)) {
            return new ODBEditor(c, bundleIdentifier, path);
        }
        if(!Preferences.instance().getBoolean("editor.kqueue.enable")) {
            log.error("Support for watch editors must be enabled first");
            return null;
        }
        return new WatchEditor(c, bundleIdentifier, path);
    }

    public static Map<String, String> getSupportedEditors() {
        Map<String, String> supported = new HashMap<String, String>();
        supported.putAll(ODBEditor.getSupportedEditors());
        supported.putAll(WatchEditor.getSupportedEditors());
        return supported;
    }

    public static Map<String, String> getInstalledEditors() {
        Map<String, String> installed = new HashMap<String, String>();
        installed.putAll(ODBEditor.getInstalledEditors());
        installed.putAll(WatchEditor.getInstalledEditors());
        return installed;
    }
}
