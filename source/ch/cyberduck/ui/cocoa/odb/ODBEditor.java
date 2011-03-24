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

import ch.cyberduck.core.Native;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id$
 */
public class ODBEditor extends Editor {
    private static Logger log = Logger.getLogger(ODBEditor.class);

    private static final Map<String, String> SUPPORTED_ODB_EDITORS = new HashMap<String, String>();
    private static final Map<String, String> INSTALLED_ODB_EDITORS = new HashMap<String, String>();

    static {
        SUPPORTED_ODB_EDITORS.put("SubEthaEdit", "de.codingmonkeys.SubEthaEdit");
        SUPPORTED_ODB_EDITORS.put("BBEdit", "com.barebones.bbedit");
        SUPPORTED_ODB_EDITORS.put("TextWrangler", "com.barebones.textwrangler");
        SUPPORTED_ODB_EDITORS.put("TextMate", "com.macromates.textmate");
        SUPPORTED_ODB_EDITORS.put("Tex-Edit Plus", "com.transtex.texeditplus");
        SUPPORTED_ODB_EDITORS.put("Jedit X", "jp.co.artman21.JeditX");
        SUPPORTED_ODB_EDITORS.put("mi", "net.mimikaki.mi");
        SUPPORTED_ODB_EDITORS.put("Smultron", "org.smultron.Smultron");
        SUPPORTED_ODB_EDITORS.put("Fraise", "org.fraise.Fraise");
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
        SUPPORTED_ODB_EDITORS.put("ScinteX", "net.experiya.ScinteX");

        Iterator<String> editorNames = SUPPORTED_ODB_EDITORS.keySet().iterator();
        Iterator<String> editorIdentifiers = SUPPORTED_ODB_EDITORS.values().iterator();
        while(editorNames.hasNext()) {
            String editor = editorNames.next();
            String identifier = editorIdentifiers.next();
            final String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier);
            if(StringUtils.isEmpty(path)) {
                continue;
            }
            INSTALLED_ODB_EDITORS.put(editor, identifier);
        }
    }

    public static Map<String, String> getSupportedEditors() {
        return SUPPORTED_ODB_EDITORS;
    }

    public static Map<String, String> getInstalledEditors() {
        return INSTALLED_ODB_EDITORS;
    }

    /**
     * @param c
     * @param bundleIdentifier
     */
    public ODBEditor(BrowserController c, String bundleIdentifier, final Path path) {
        super(c, bundleIdentifier, path);
    }

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("ODBEdit");
        }
        return JNI_LOADED;
    }

    /**
     * Open the file using the ODB external editor protocol
     */
    @Override
    public void edit() {
        if(!ODBEditor.loadNative()) {
            return;
        }
        this.edit(edited.getLocal().getAbsolute(), edited.toURL(), bundleIdentifier);
    }

    /**
     * Native implementation
     * Open the file using the ODB external editor protocol
     *
     * @param local            Absolute path on the local file system
     * @param url              The remote URL
     * @param bundleIdentifier
     */
    private native void edit(String local, String url, String bundleIdentifier);

    /**
     * Called by the native editor when the file has been closed
     */
    public void didCloseFile() {
        if(!edited.status().isComplete()) {
            this.setDeferredDelete(true);
        }
        else {
            this.delete();
        }
    }

    /**
     * called by the native editor when the file has been saved
     */
    public void didModifyFile() {
        this.save();
    }
}
