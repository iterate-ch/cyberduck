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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.ui.Controller;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public class ODBEditor extends BrowserBackgroundEditor {
    private static final Logger log = Logger.getLogger(ODBEditor.class);

    static {
        Native.load("ODBEdit");
    }

    private Session<?> session;

    public ODBEditor(final Controller controller, final Session session,
                     final Application application, final Path path) {
        super(controller, session, application, path);
        this.session = session;
    }

    /**
     * Open the file using the ODB external editor protocol
     */
    @Override
    public void edit() throws IOException {
        final Path file = this.getEdited();
        // Important, should always be run on the main thread; otherwise applescript crashes
        final UrlProvider provider = session.getFeature(UrlProvider.class);
        if(!this.edit(file.getLocal().getAbsolute(), provider.toUrl(file).find(DescriptiveUrl.Type.provider).getUrl(), this.getApplication().getIdentifier())) {
            throw new IOException(String.format("Edit failed for %s", file.getLocal().getAbsolute()));
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
        if(log.isDebugEnabled()) {
            log.debug(String.format("Received notification from editor to close file %s",
                    edited.getLocal().getAbsolute()));
        }
        if(this.isModified()) {
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
        if(log.isDebugEnabled()) {
            log.debug(String.format("Received notification from editor to save file %s",
                    edited.getLocal().getAbsolute()));
        }
        this.setModified(true);
        this.save();
    }
}