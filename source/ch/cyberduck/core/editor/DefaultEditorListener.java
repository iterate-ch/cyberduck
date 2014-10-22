package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.FileWatcherListener;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class DefaultEditorListener implements FileWatcherListener {
    private static final Logger log = Logger.getLogger(DefaultEditorListener.class);

    private Editor editor;

    public DefaultEditorListener(final Editor editor) {
        this.editor = editor;
    }

    @Override
    public void fileWritten(final Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s written", file));
        }
        editor.save();
    }

    @Override
    public void fileDeleted(final Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s deleted", file));
        }
        editor.delete();
    }

    @Override
    public void fileCreated(final Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s created", file));
        }
        editor.save();
    }

}
