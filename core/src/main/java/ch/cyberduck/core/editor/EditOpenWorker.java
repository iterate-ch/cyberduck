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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.filter.DownloadDuplicateFilter;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;
import ch.cyberduck.core.worker.SingleTransferWorker;
import ch.cyberduck.core.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Objects;

public class EditOpenWorker extends Worker<Transfer> {
    private static final Logger log = LogManager.getLogger(EditOpenWorker.class);

    private final AbstractEditor editor;
    private final Transfer download;
    private final Path file;
    private final Local local;
    private final ApplicationQuitCallback quit;
    private final NotificationService notification;
    private final ProgressListener listener;
    private final FileWatcherListener watcher;
    private final Application application;

    public EditOpenWorker(final Host bookmark, final AbstractEditor editor,
                          final Application application,
                          final Path file, final Local local,
                          final ProgressListener listener,
                          final ApplicationQuitCallback quit, final FileWatcherListener watcher,
                          final NotificationService notification) {
        this.application = application;
        this.file = file;
        this.editor = editor;
        this.local = local;
        this.quit = quit;
        this.notification = notification;
        final DownloadFilterOptions options = new DownloadFilterOptions(bookmark);
        options.quarantine = false;
        options.wherefrom = false;
        options.open = false;
        this.download = new DownloadTransfer(bookmark, file, local, new DownloadDuplicateFilter()) {
            @Override
            public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt, final ListProgressListener listener) {
                return TransferAction.trash;
            }
        }.withOptions(options);
        this.listener = listener;
        this.watcher = watcher;
    }

    @Override
    public Transfer run(final Session<?> session) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run edit action for editor %s", file));
        }
        // Delete any existing file which might be used by a watch editor already
        final TransferOptions options = new TransferOptions();
        final SingleTransferWorker worker
                = new SingleTransferWorker(session, session, download, options, new TransferSpeedometer(download),
                new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                listener, new DisabledStreamListener(), new DisabledLoginCallback(), notification);
        worker.run(session);
        if(!download.isComplete()) {
            log.warn(String.format("File size changed for %s", file));
        }
        try {
            editor.edit(application, file, local, watcher, quit);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return download;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Downloading {0}", "Status"), file.getName());
    }

    @Override
    public Transfer initialize() {
        return download;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final EditOpenWorker that = (EditOpenWorker) o;
        return Objects.equals(editor, that.editor) && Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(editor, file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EditOpenWorker{");
        sb.append("editor=").append(editor);
        sb.append(", file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
