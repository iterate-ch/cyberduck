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
import ch.cyberduck.ui.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id$
 */
public class EditorFactory {
    private static Logger log = Logger.getLogger(EditorFactory.class);

    private static final Map<String, String> SUPPORTED_ODB_EDITORS = new HashMap<String, String>();
    private static final Map<String, String> INSTALLED_ODB_EDITORS = new HashMap<String, String>();

    static {
        SUPPORTED_ODB_EDITORS.put("SubEthaEdit", "de.codingmonkeys.SubEthaEdit");
        SUPPORTED_ODB_EDITORS.put("BBEdit", "com.barebones.bbedit");
        SUPPORTED_ODB_EDITORS.put("TextWrangler", "com.barebones.textwrangler");
        SUPPORTED_ODB_EDITORS.put("TextMate", "com.macromates.textmate");
        SUPPORTED_ODB_EDITORS.put("Tex-Edit Plus", "com.transtex.texeditplus");
        SUPPORTED_ODB_EDITORS.put("Jedit X", "jp.co.artman21.JeditX");
        SUPPORTED_ODB_EDITORS.put("mi", "mi");
        SUPPORTED_ODB_EDITORS.put("Smultron", "org.smultron.Smultron");
        SUPPORTED_ODB_EDITORS.put("CotEditor", "com.aynimac.CotEditor");
        SUPPORTED_ODB_EDITORS.put("CSSEdit", "com.macrabbit.cssedit");
        SUPPORTED_ODB_EDITORS.put("Tag", "com.talacia.Tag");
        SUPPORTED_ODB_EDITORS.put("skEdit", "org.skti.skEdit");
        SUPPORTED_ODB_EDITORS.put("JarInspector", "com.cgerdes.ji");
        SUPPORTED_ODB_EDITORS.put("PageSpinner", "com.optima.PageSpinner");
        SUPPORTED_ODB_EDITORS.put("WriteRoom", "com.hogbaysoftware.WriteRoom");
        SUPPORTED_ODB_EDITORS.put("MacVim", "org.vim.MacVim");
        SUPPORTED_ODB_EDITORS.put("ForgEdit", "com.forgedit.ForgEdit");
        SUPPORTED_ODB_EDITORS.put("Taco HTML Edit", "com.tacosw.TacoHTMLEdit");
        SUPPORTED_ODB_EDITORS.put("Espresso", "com.macrabbit.Espresso");

        Iterator<String> editorNames = SUPPORTED_ODB_EDITORS.keySet().iterator();
        Iterator<String> editorIdentifiers = SUPPORTED_ODB_EDITORS.values().iterator();
        while(editorNames.hasNext()) {
            String editor = editorNames.next();
            String identifier = editorIdentifiers.next();
            if(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier) != null) {
                INSTALLED_ODB_EDITORS.put(editor, identifier);
            }
        }
    }

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
        final String defaultApplication = file.getDefaultEditor();
        if(null == defaultApplication) {
            // Use default editor
            return defaultEditor();
        }
        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
            return defaultApplication;
        }
        for(final String identifier : INSTALLED_ODB_EDITORS.values()) {
            final String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier);
            if(null == path) {
                continue;
            }
            if(path.equals(defaultApplication)) {
                return identifier;
            }
        }
        if(INSTALLED_ODB_EDITORS.containsValue(Preferences.instance().getProperty("editor.bundleIdentifier"))) {
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
     * @param bundleIdentifier
     * @param path
     * @return
     */
    public static Editor createEditor(BrowserController c, String bundleIdentifier, final Path path) {
        if(getInstalledOdbEditors().containsValue(bundleIdentifier)) {
            return new ODBEditor(c, bundleIdentifier, path);
        }
        if(!Preferences.instance().getBoolean("editor.kqueue.enable")) {
            log.error("Support for non ODB editors must be enabled first");
            return null;
        }
        return new WatchEditor(c, bundleIdentifier, path);
    }

    public static Map<String, String> getSupportedOdbEditors() {
        return SUPPORTED_ODB_EDITORS;
    }

    public static Map<String, String> getInstalledOdbEditors() {
        return INSTALLED_ODB_EDITORS;
    }
}
