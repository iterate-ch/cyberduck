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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class EditorFactory {
    private static Logger log = Logger.getLogger(EditorFactory.class);

    /**
     * @param c
     * @param path
     * @return New editor instance for the given file type.
     */
    public static Editor createEditor(BrowserController c, final Path path) {
        return createEditor(c, defaultEditor(path.getLocal()), path);
    }

    /**
     * @param c
     * @param bundleIdentifier The application bundle identifier of the editor to use
     * @param path
     * @return New editor instance for the given file type.
     */
    public static Editor createEditor(BrowserController c, String bundleIdentifier, final Path path) {
        if(Preferences.instance().getBoolean("editor.odb.enable")) {
            if(ODBEditor.getInstalledEditors().containsValue(bundleIdentifier)) {
                return new ODBEditor(c, bundleIdentifier, path);
            }
        }
        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
            return new WatchEditor(c, bundleIdentifier, path);
        }
        log.error("No editor support enabled");
        return null;
    }

    /**
     * Determine the default editor set
     *
     * @return The bundle identifier of the default editor configured in
     *         Preferences or null if not installed.
     */
    public static String defaultEditor() {
        if(log.isTraceEnabled()) {
            log.trace("defaultEditor");
        }
        if(StringUtils.isEmpty(EditorFactory.getApplicationName(Preferences.instance().getProperty("editor.bundleIdentifier")))) {
            return null;
        }
        return Preferences.instance().getProperty("editor.bundleIdentifier");
    }

    /**
     * @param file
     * @return The bundle identifier of the application for this file or null if no
     *         suitable and installed editor is found.
     */
    public static String defaultEditor(final Local file) {
        if(log.isDebugEnabled()) {
            log.debug("defaultEditor:" + file);
        }
        if(null == file) {
            return defaultEditor();
        }
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
        if(Preferences.instance().getBoolean("editor.odb.enable")) {
            // Find matching ODB editor if any
            for(final String bundleIdentifier : ODBEditor.getInstalledEditors().values()) {
                if(bundleIdentifier.equals(defaultApplication)) {
                    return bundleIdentifier;
                }
            }
        }
        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
            // Use application determined by launch services using file system notifications
            return defaultApplication;
        }
        log.warn("No editor for file type " + file.getExtension());
        return defaultEditor();
    }

    /**
     * @return All statically registered but possibly not installed editors
     */
    public static Map<String, String> getSupportedEditors() {
        if(log.isTraceEnabled()) {
            log.trace("getSupportedEditors");
        }
        Map<String, String> supported = new HashMap<String, String>();
        if(Preferences.instance().getBoolean("editor.odb.enable")) {
            supported.putAll(ODBEditor.getSupportedEditors());
        }
        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
            supported.putAll(WatchEditor.getSupportedEditors());
        }
        final String defaultEditor = defaultEditor();
        if(null == defaultEditor) {
            return supported;
        }
        if(!supported.values().contains(defaultEditor)) {
            supported.put(EditorFactory.getApplicationName(defaultEditor),
                    defaultEditor);
        }
        return supported;
    }

    /**
     * @return All statically registered and installed editors
     */
    public static Map<String, String> getInstalledEditors() {
        if(log.isTraceEnabled()) {
            log.trace("getInstalledEditors");
        }
        Map<String, String> installed = new HashMap<String, String>();
        if(Preferences.instance().getBoolean("editor.odb.enable")) {
            installed.putAll(ODBEditor.getInstalledEditors());
        }
        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
            installed.putAll(WatchEditor.getInstalledEditors());
        }
        return installed;
    }

    /**
     * @param file
     * @return Installed applications suitable to edit the given file type. Does always include
     *         the default editor set in the Preferences
     */
    public static Map<String, String> getInstalledEditors(final Local file) {
        if(log.isTraceEnabled()) {
            log.trace("getInstalledEditors:" + file);
        }
        if(null == file) {
            return getInstalledEditors();
        }
        if(!Preferences.instance().getBoolean("editor.kqueue.enable")) {
            return getInstalledEditors();
        }
        Map<String, String> editors = new HashMap<String, String>();
        for(String bundleIdentifier : file.getDefaultApplications()) {
            final String name = getApplicationName(bundleIdentifier);
            if(null == name) {
                continue;
            }
            editors.put(name, bundleIdentifier);
        }
        // Add the application set as the default editor in the Preferences to be always
        // included in the list of available editors.
        final String defaultEditor = defaultEditor();
        if(null != defaultEditor) {
            if(!editors.values().contains(defaultEditor)) {
                editors.put(getApplicationName(defaultEditor), defaultEditor);
            }
        }
        return editors;
    }

    /**
     * Caching map between application bundle identifier and
     * display name of application
     */
    private static Map<String, String> applicationNameCache
            = Collections.<String, String>synchronizedMap(new LRUMap(20) {
        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    public static String getApplicationName(String bundleIdentifier) {
        if(!applicationNameCache.containsKey(bundleIdentifier)) {
            log.debug("getApplicationName:" + bundleIdentifier);
            final String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier);
            if(StringUtils.isEmpty(path)) {
                log.warn("Cannot determine installation path for " + bundleIdentifier);
                applicationNameCache.put(bundleIdentifier, null);
            }
            else {
                NSBundle app = NSBundle.bundleWithPath(path);
                String name;
                if(null != app.infoDictionary().objectForKey("CFBundleName")) {
                    applicationNameCache.put(bundleIdentifier,
                            app.infoDictionary().objectForKey("CFBundleName").toString());
                }
                else {
                    log.warn("No CFBundleName for " + bundleIdentifier);
                    applicationNameCache.put(bundleIdentifier,
                            FilenameUtils.removeExtension(LocalFactory.createLocal(path).getDisplayName()));
                }
            }
        }
        return applicationNameCache.get(bundleIdentifier);
    }
}
