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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.Transfer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultEditorListener implements FileWatcherListener {
    private static final Logger log = LogManager.getLogger(DefaultEditorListener.class);

    private final Controller controller;
    private final SessionPool session;
    private final Editor editor;
    private final Listener listener;

    public DefaultEditorListener(final Controller controller, final SessionPool session, final Editor editor, final Listener listener) {
        this.controller = controller;
        this.session = session;
        this.editor = editor;
        this.listener = listener;
    }

    @Override
    public void fileWritten(final Local temporary) {
        log.info("File {} written", temporary);
        controller.background(new WorkerBackgroundAction<Transfer>(controller, session, editor.save(new DisabledTransferErrorCallback())) {
            @Override
            public void cleanup() {
                super.cleanup();
                listener.saved();
            }
        });
    }

    @Override
    public void fileDeleted(final Local temporary) {
        log.info("File {} deleted", temporary);
    }

    @Override
    public void fileCreated(final Local temporary) {
        log.info("File {} created", temporary);
        controller.background(new WorkerBackgroundAction<Transfer>(controller, session, editor.save(new DisabledTransferErrorCallback())) {
            @Override
            public void cleanup() {
                super.cleanup();
                listener.saved();
            }
        });
    }

    /**
     * Listener receiving notifications after saving changes from the editor
     */
    public interface Listener {
        /**
         * Callback after edited file was uploaded to server
         */
        void saved();
    }
}
