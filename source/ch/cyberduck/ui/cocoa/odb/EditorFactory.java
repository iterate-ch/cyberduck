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

import com.apple.cocoa.application.NSWorkspace;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.CDBrowserController;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id:$
 */
public class EditorFactory {
    private static Logger log = Logger.getLogger(EditorFactory.class);

    public static final Map SUPPORTED_ODB_EDITORS = new HashMap();
    public static final Map INSTALLED_ODB_EDITORS = new HashMap();

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

        Iterator editorNames = SUPPORTED_ODB_EDITORS.keySet().iterator();
        Iterator editorIdentifiers = SUPPORTED_ODB_EDITORS.values().iterator();
        while(editorNames.hasNext()) {
            String editor = (String) editorNames.next();
            String identifier = (String) editorIdentifiers.next();
            if(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier) != null) {
                INSTALLED_ODB_EDITORS.put(editor, identifier);
            }
        }
    }

    public static String defaultEditor() {
        if(null == NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                Preferences.instance().getProperty("editor.bundleIdentifier")
        )) {
            return null;
        }
        return Preferences.instance().getProperty("editor.bundleIdentifier");
    }


    /**
     *
     * @param file
     * @return The bundle identifier of the editor for this file.
     * Null if no suitable and installed editor is found.
     */
    public static String editorBundleIdentifierForFile(final Local file) {
        final String defaultApplication = file.getDefaultEditor();
        if(null == defaultApplication) {
            // Use default editor
            return defaultEditor();
        }
        for(Iterator iter = INSTALLED_ODB_EDITORS.values().iterator(); iter.hasNext(); ) {
            final String identifier = (String) iter.next();
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
        return null;
    }

    public static Editor createEditor(CDBrowserController c, Local file) {
        return createEditor(c, editorBundleIdentifierForFile(file));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Editor createEditor(CDBrowserController c) {
//        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
//            return createEditor(c, null);
//        }
        return createEditor(c, Preferences.instance().getProperty("editor.bundleIdentifier"));
    }

    /**
     * @param c
     * @param bundleIdentifier
     * @return
     */
    public static Editor createEditor(CDBrowserController c, String bundleIdentifier) {
        return new ODBEditor(c, bundleIdentifier);
//        if(null == bundleIdentifier) {
//            return new WatchEditor(c);
//        }
//        if(INSTALLED_ODB_EDITORS.containsValue(bundleIdentifier)) {
//            return new ODBEditor(c, bundleIdentifier);
//        }
//        if(!Preferences.instance().getBoolean("editor.kqueue.enable")) {
//            log.error("Support for non ODB editors must be enabled first");
//            return null;
//        }
//        return new WatchEditor(c, bundleIdentifier);
    }

    private static String SELECTED_EDITOR;

    static {
        if(INSTALLED_ODB_EDITORS.containsValue(Preferences.instance().getProperty("editor.bundleIdentifier"))) {
            SELECTED_EDITOR = Preferences.instance().getProperty("editor.bundleIdentifier");
        }
    }

    public static void setSelectedEditor(String editorBundleIdentifier) {
        SELECTED_EDITOR = editorBundleIdentifier;
    }

    public static String getSelectedEditor() {
        return SELECTED_EDITOR;
    }
}
