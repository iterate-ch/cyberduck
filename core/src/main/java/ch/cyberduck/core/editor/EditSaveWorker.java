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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.DisabledTransferItemCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.transfer.upload.AbstractUploadFilter;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.core.worker.SingleTransferWorker;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

public class EditSaveWorker extends Worker<Transfer> {
    private static final Logger log = Logger.getLogger(EditSaveWorker.class);

    private AbstractEditor editor;

    private Transfer upload;

    private TransferErrorCallback callback;

    private ProgressListener listener;

    public EditSaveWorker(final Host bookmark, final AbstractEditor editor,
                          final TransferErrorCallback callback, final ProgressListener listener) {
        this.editor = editor;
        this.callback = callback;
        this.upload = new UploadTransfer(bookmark, editor.getRemote(), editor.getLocal(), new NullFilter<Local>()) {
            @Override
            public TransferAction action(final Session<?> session,
                                         final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
                return TransferAction.overwrite;
            }

            @Override
            public AbstractUploadFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
                return super.filter(session, action, listener).withOptions(new UploadFilterOptions()
                        .withTemporary(PreferencesFactory.get().getBoolean("queue.upload.file.temporary"))
                        .withPermission(PreferencesFactory.get().getBoolean("queue.upload.permissions.change")));
            }
        };
        this.listener = listener;
    }

    @Override
    public Transfer run(final Session<?> session) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run upload action for editor %s", editor));
        }
        final SingleTransferWorker worker
                = new SingleTransferWorker(session, upload, new TransferOptions(),
                new TransferSpeedometer(upload), new DisabledTransferPrompt(), callback, new DisabledTransferItemCallback(),
                listener, new DisabledStreamListener(), new DisabledLoginCallback());
        worker.run(session);
        if(!upload.isComplete()) {
            log.warn(String.format("File size changed for %s", editor.getRemote()));
        }
        else {
            // Update known remote file size
            editor.getRemote().attributes().setSize(upload.getTransferred());
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
                editor.getRemote().getName());
    }

    @Override
    public Transfer initialize() {
        return upload;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        EditSaveWorker that = (EditSaveWorker) o;
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
