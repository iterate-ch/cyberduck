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
import ch.cyberduck.ui.cocoa.CDBrowserController;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ODBEditor extends Editor {
    private static Logger log = Logger.getLogger(ODBEditor.class);

    /**
     * @param c
     * @param bundleIdentifier
     */
    public ODBEditor(CDBrowserController c, String bundleIdentifier, final Path path) {
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
        this.edit(edited.getLocal().getAbsolute(), bundleIdentifier);
    }

    /**
     * Native implementation
     * Open the file using the ODB external editor protocol
     *
     * @param path
     * @param bundleIdentifier
     */
    private native void edit(final String path, final String bundleIdentifier);

    /**
     * Called by the native editor when the file has been closed
     */
    public void didCloseFile() {
        if(!edited.getStatus().isComplete()) {
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
