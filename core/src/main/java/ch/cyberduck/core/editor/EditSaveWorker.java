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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.core.worker.SingleTransferWorker;
import ch.cyberduck.core.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Objects;

public class EditSaveWorker extends Worker<Transfer> {
    private static final Logger log = LogManager.getLogger(EditSaveWorker.class);

    private final AbstractEditor editor;
    private final Transfer upload;
    private final Path file;
    private final TransferErrorCallback callback;
    private final NotificationService notification;
    private final ProgressListener listener;

    public EditSaveWorker(final Host bookmark, final AbstractEditor editor,
                          final Path file, final Local local,
                          final TransferErrorCallback callback, final ProgressListener listener, final NotificationService notification) {
        this.editor = editor;
        this.file = file;
        this.callback = callback;
        this.notification = notification;
        this.upload = new UploadTransfer(bookmark, file, local, new NullFilter<>()) {
            @Override
            public TransferAction action(final Session<?> source,
                                         final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt, final ListProgressListener listener) {
                return TransferAction.overwrite;
            }
        }.withOptions(new UploadFilterOptions(bookmark)
                .withVersioning(PreferencesFactory.get().getBoolean("editor.upload.file.versioning"))
                .withPermission(PreferencesFactory.get().getBoolean("editor.upload.permissions.change")));
        this.listener = listener;
    }

    @Override
    public Transfer run(final Session<?> session) throws BackgroundException {
        log.debug("Run upload action for editor {}", editor);
        final SingleTransferWorker worker
                = new SingleTransferWorker(session, session, upload, new TransferOptions(),
                new TransferSpeedometer(upload), new DisabledTransferPrompt(), callback,
                listener, new DisabledStreamListener(), new DisabledLoginCallback(), notification);
        worker.run(session);
        if(!upload.isComplete()) {
            log.warn("File size changed for {}", file);
        }
        else {
            // Update known remote file size
            file.attributes().setSize(upload.getTransferred());
        }
        return upload;
    }

    @Override
    public void cleanup(final Transfer upload) {
        editor.setModified(false);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"),
                file.getName());
    }

    @Override
    public Transfer initialize() {
        return upload;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final EditSaveWorker that = (EditSaveWorker) o;
        return Objects.equals(editor, that.editor) && Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(editor, file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EditSaveWorker{");
        sb.append("editor=").append(editor);
        sb.append(", file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
