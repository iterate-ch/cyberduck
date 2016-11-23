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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.filter.DownloadDuplicateFilter;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.worker.SingleTransferWorker;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

public class EditOpenWorker extends Worker<Transfer> {
    private static final Logger log = Logger.getLogger(EditOpenWorker.class);

    private final AbstractEditor editor;

    private final Transfer download;

    private final TransferErrorCallback callback;

    private final ApplicationQuitCallback quit;

    private final ProgressListener listener;

    private final FileWatcherListener watcher;

    public EditOpenWorker(final Host bookmark, final AbstractEditor editor,
                          final TransferErrorCallback callback,
                          final ApplicationQuitCallback quit,
                          final ProgressListener listener,
                          final FileWatcherListener watcher) {
        this.editor = editor;
        this.callback = callback;
        this.quit = quit;
        this.download = new DownloadTransfer(bookmark, editor.getRemote(), editor.getLocal(),
                new DownloadDuplicateFilter()) {
            @Override
            public TransferAction action(final Session<?> session, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
                return TransferAction.trash;
            }
        };
        this.listener = listener;
        this.watcher = watcher;
    }

    @Override
    public Transfer run(final Session<?> session) throws BackgroundException {
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
                new DisabledTransferPrompt(), callback,
                listener, new DisabledStreamListener(), new DisabledLoginCallback());
        worker.run(session);
        if(!download.isComplete()) {
            log.warn(String.format("File size changed for %s", file));
        }
        try {
            editor.edit(quit, watcher);
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
        EditOpenWorker that = (EditOpenWorker) o;
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
