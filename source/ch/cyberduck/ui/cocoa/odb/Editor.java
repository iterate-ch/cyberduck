package ch.cyberduck.ui.cocoa.odb;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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
import ch.cyberduck.core.UploadQueue;
import ch.cyberduck.ui.cocoa.CDController;

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * @version $Id$
 */
public class Editor extends CDController {

    private static Logger log = Logger.getLogger(Editor.class);

    public static Map SUPPORTED_EDITORS = new HashMap();
    public static Map INSTALLED_EDITORS = new HashMap();

    static {
        SUPPORTED_EDITORS.put("SubEthaEdit", "de.codingmonkeys.SubEthaEdit");
        SUPPORTED_EDITORS.put("BBEdit", "com.barebones.bbedit");
        SUPPORTED_EDITORS.put("TextWrangler", "com.barebones.textwrangler");
        SUPPORTED_EDITORS.put("TextMate", "com.macromates.textmate");
        SUPPORTED_EDITORS.put("Tex-Edit Plus", "com.transtex.texeditplus");
        SUPPORTED_EDITORS.put("Jedit X", "jp.co.artman21.JeditX");
        SUPPORTED_EDITORS.put("mi", "mi");
        SUPPORTED_EDITORS.put("Smultron", "org.smultron.Smultron");
        SUPPORTED_EDITORS.put("CotEditor", "com.aynimac.CotEditor");
        SUPPORTED_EDITORS.put("CSSEdit", "com.macrabbit.cssedit");
        SUPPORTED_EDITORS.put("Tag", "com.talacia.Tag");
        SUPPORTED_EDITORS.put("skEdit", "org.skti.skEdit");

        Iterator editorNames = SUPPORTED_EDITORS.keySet().iterator();
        Iterator editorIdentifiers = SUPPORTED_EDITORS.values().iterator();
        while (editorNames.hasNext()) {
            String editor = (String) editorNames.next();
            String identifier = (String) editorIdentifiers.next();
            if (NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier) != null) {
                INSTALLED_EDITORS.put(editor, identifier);
            }
        }
    }

    private String bundleIdentifier;

    /**
     * @param bundleIdentifier The bundle identifier of the external editor to use
     */
    public Editor(String bundleIdentifier) {
        this.bundleIdentifier = bundleIdentifier;
    }

    private Path path;

    public void open(Path f) {
        this.path = (Path)f.clone(f.getSession());
        String parent = NSPathUtilities.temporaryDirectory();
        String filename = this.path.getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            this.path.setLocal(new Local(parent, proposal));
            no++;
            if (index != -1) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
        }
        while (this.path.getLocal().exists());

        this.path.download();
        if (this.path.status.isComplete()) {
            this.jni_load();
            this.edit(this.path.getLocal().getAbsolute(), this.bundleIdentifier);
        }
    }

    static boolean JNI_LOADED = false;

    private boolean jni_load() {
        if(!JNI_LOADED) {
            try {
                NSBundle bundle = NSBundle.mainBundle();
                String lib = bundle.resourcePath() + "/Java/" + "libODBEdit.dylib";
                log.info("Locating libODBEdit.dylib at '" + lib + "'");
                System.load(lib);
                JNI_LOADED = true;
            }
            catch (UnsatisfiedLinkError e) {
                log.error("Could not load the ODBEdit library:" + e.getMessage());
            }
        }
        return JNI_LOADED;
    }

    private native void edit(String path, String bundleIdentifier);

    public void didCloseFile() {
        this.path.getLocal().delete();
        this.invalidate();
    }

    public void didModifyFile() {
        UploadQueue q = new UploadQueue(this.path);
        q.process(false, true);
    }
}