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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.io.FileWatcherListener;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDController;
import ch.cyberduck.ui.cocoa.CDIconCache;
import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
        SUPPORTED_EDITORS.put("JarInspector", "com.cgerdes.ji");
        SUPPORTED_EDITORS.put("PageSpinner", "com.optima.PageSpinner");

        Iterator editorNames = SUPPORTED_EDITORS.keySet().iterator();
        Iterator editorIdentifiers = SUPPORTED_EDITORS.values().iterator();
        while(editorNames.hasNext()) {
            String editor = (String) editorNames.next();
            String identifier = (String) editorIdentifiers.next();
            if(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier) != null) {
                INSTALLED_EDITORS.put(editor, identifier);
            }
        }
    }

    private CDBrowserController controller;

    /**
     * @param controller
     */
    public Editor(CDBrowserController controller) {
        this.controller = controller;
    }

    private Path path;

    public void open(Path f) {
        this.open(f, Preferences.instance().getProperty("editor.bundleIdentifier"));
    }

    /**
     * @param bundleIdentifier The bundle identifier of the external editor to use
     *                         or null if the default application for this file type should be opened
     */
    public void open(Path f, final String bundleIdentifier) {
        this.path = (Path) f.clone();
        String parent = NSPathUtilities.temporaryDirectory();
        String filename = this.path.getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            this.path.setLocal(new Local(parent, proposal));
            no++;
            if(index != -1 && index != 0) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
        }
        while(this.path.getLocal().exists());

        controller.background(new BackgroundAction() {
            public void run() {
                path.download();
            }

            public void cleanup() {
                if(path.status.isComplete()) {
                    path.getSession().message(NSBundle.localizedString("Download complete", "Growl", "Growl Notification"));
                    // Important, should always be run on the main thread; otherwise applescript crashes
                    edit(bundleIdentifier);
                }
            }
        });
    }

    private static boolean JNI_LOADED = false;

    private static final Object lock = new Object();

    private boolean jni_load() {
        if(!JNI_LOADED) {
            try {
                synchronized(lock) {
                    NSBundle bundle = NSBundle.mainBundle();
                    String lib = bundle.resourcePath() + "/Java/" + "libODBEdit.dylib";
                    log.info("Locating libODBEdit.dylib at '" + lib + "'");
                    System.load(lib);
                    JNI_LOADED = true;
                    log.info("libODBEdit.dylib loaded");
                }
            }
            catch(UnsatisfiedLinkError e) {
                log.error("Could not load the libODBEdit.dylib library:" + e.getMessage());
                throw e;
            }
        }
        return JNI_LOADED;
    }

    private void edit(final String bundleIdentifier) {
        jni_load();
        this.edit(path.getLocal().getAbsolute(), bundleIdentifier);
    }

    /**
     * Open the file using the ODB external editor protocol
     */
    private native void edit(String path, String bundleIdentifier);

//    private void edit() {
//        final String defaultAppPath
//                = NSWorkspace.sharedWorkspace().applicationForFile(path.getLocal().getAbsolute());
//        //Returns null if the file cannot be found or if the file is of an unknown type.
//        if(null == defaultAppPath) {
//            log.warn("No application found to edit "+path.getName());
//            return;
//        }
////        for(Iterator iter = INSTALLED_EDITORS.values().iterator(); iter.hasNext();) {
////            String bundleIdentifier = (String) iter.next();
////            if(defaultAppPath.equals(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
////                    bundleIdentifier))) {
////                // Open with ODB as the default app for this file happens to support the ODB protocol
////                this.edit(path.getLocal().getAbsolute(), bundleIdentifier);
////            }
////        }
//        final Local l = path.getLocal();
//        l.watch(new FileWatcherListener() {
//            public void fileWritten(Local file) {
//                didModifyFile();
//            }
//
//            public void fileRenamed(Local file) {
//                //path.rename();
//            }
//
//            public void fileDeleted(Local file) {
//                invalidate();
//            }
//        });
//        log.info("Opening " + l.getName() + " with " + defaultAppPath);
//        NSWorkspace.sharedWorkspace().openFile(l.getAbsolute(), defaultAppPath, true);
//    }

    public void didCloseFile() {
        if(!uploadInProgress) {
            this.path.getLocal().delete();
            this.invalidate();
        }
        else {
            shouldCloseFile = true;
        }
    }

    private boolean uploadInProgress;

    private boolean shouldCloseFile;

    public void didModifyFile() {
        controller.background(new BackgroundAction() {
            public void run() {
                uploadInProgress = true;
                try {
                    path.upload();
                }
                finally {
                    uploadInProgress = false;
                }
            }

            public void cleanup() {
                if(shouldCloseFile) {
                    didCloseFile();
                }
                if(path.status.isComplete()) {
                    path.getSession().message(
                            NSBundle.localizedString("Upload complete", "Growl", "Growl Notification"));
                    Growl.instance().notify("Upload complete", path.getName());
                }
            }
        });
    }
}
