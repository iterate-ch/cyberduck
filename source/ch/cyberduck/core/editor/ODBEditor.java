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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.ui.Controller;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ODBEditor extends BrowserBackgroundEditor {
    private static final Logger log = Logger.getLogger(ODBEditor.class);

    public ODBEditor(Controller c, Application application, final Path path) {
        super(c, application, path);
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
        if(!loadNative()) {
            return;
        }
        if(!this.edit(this.getEdited().getLocal().getAbsolute(), this.getEdited().toURL(), this.getApplication().getIdentifier())) {
            log.warn(String.format("Edit failed for %s", this.getEdited().getLocal().getAbsolute()));
        }
    }

    /**
     * Native implementation
     * Open the file using the ODB external editor protocol
     *
     * @param local            Absolute path on the local file system
     * @param url              The remote URL
     * @param bundleIdentifier Application bundle identifier
     * @return False if opening editor fails
     */
    private native boolean edit(String local, String url, String bundleIdentifier);

    /**
     * Called by the native editor when the file has been closed
     */
    public void didCloseFile() {
        log.debug(String.format("Received notification from editor to close file %s",
                this.getEdited().getLocal().getAbsolute()));
        if(this.isDirty()) {
            this.setClosed(true);
        }
        else {
            // Delete immediately
            this.delete();
        }
    }

    /**
     * called by the native editor when the file has been saved
     */
    public void didModifyFile() {
        log.debug(String.format("Received notification from editor to save file %s",
                this.getEdited().getLocal().getAbsolute()));
        this.setDirty(true);
        this.save();
    }
}