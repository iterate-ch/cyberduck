package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.DisabledTransferItemCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.ui.action.SingleTransferWorker;
import ch.cyberduck.ui.action.Worker;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class EditBackgroundAction extends Worker<Transfer> {
    private static final Logger log = Logger.getLogger(EditBackgroundAction.class);

    private AbstractEditor editor;

    private Session session;

    private Transfer download;

    private TransferErrorCallback callback;

    private ProgressListener listener;

    public EditBackgroundAction(final AbstractEditor editor, final Session session,
                                final TransferErrorCallback callback, final ProgressListener listener) {
        this.editor = editor;
        this.session = session;
        this.callback = callback;
        this.download = new DownloadTransfer(session.getHost(), editor.getRemote(), editor.getLocal()) {
            @Override
            public TransferAction action(final Session<?> session, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt) throws BackgroundException {
                return TransferAction.trash;
            }
        };
        this.listener = listener;
    }

    @Override
    public Transfer run() throws BackgroundException {
        final Path file = editor.getRemote();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run edit action for editor %s", file));
        }
        // Delete any existing file which might be used by a watch editor already
        final TransferOptions options = new TransferOptions();
        options.quarantine = false;
        options.open = false;
        final SingleTransferWorker worker
                = new SingleTransferWorker(session, download, options, new TransferSpeedometer(download),
                new DisabledTransferPrompt(), callback, new DisabledTransferItemCallback(), listener, new DisabledLoginController());
        worker.run();
        if(!download.isComplete()) {
            log.warn(String.format("File size changed for %s", file));
        }
        try {
            editor.edit();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return download;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Downloading {0}", "Status"),
                editor.getRemote().getName());
    }

    @Override
    public Transfer initialize() {
        return download;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        EditBackgroundAction that = (EditBackgroundAction) o;
        if(editor != null ? !editor.equals(that.editor) : that.editor != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return editor != null ? editor.hashCode() : 0;
    }
}
