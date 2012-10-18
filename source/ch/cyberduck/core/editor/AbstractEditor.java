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
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class AbstractEditor implements Editor {
    private static final Logger log = Logger.getLogger(AbstractEditor.class);

    /**
     * The file has been closed in the editor while the upload was in progress
     */
    private boolean closed;

    /**
     * File has changed but not uploaded yet
     */
    private boolean dirty;

    /**
     * The edited path
     */
    private Path edited;

    /**
     * The editor application
     */
    private Application application;

    /**
     * Store checksum of downloaded file to detect modifications
     */
    private String checksum;

    public AbstractEditor(final Application application, final Path path) {
        this.application = application;
        // Create a copy of the path as to not interfere with the browser. #5524
        this.edited = PathFactory.createPath(path.getSession(), path.<String>getAsDictionary());
        final Local folder = LocalFactory.createLocal(
                new File(Preferences.instance().getProperty("editor.tmp.directory"),
                        edited.getHost().getUuid() + String.valueOf(Path.DELIMITER) + edited.getParent().getAbsolute()));
        edited.setLocal(LocalFactory.createLocal(folder, edited.unique()));
    }

    /**
     * @param background Download transfer
     */
    protected abstract void open(BackgroundAction<Void> background);


    /**
     * @param background Upload transfer
     */
    protected abstract void save(BackgroundAction<Void> background);

    public Path getEdited() {
        return edited;
    }

    public Application getApplication() {
        return application;
    }

    protected void setClosed(boolean closed) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set deferred delete flag for %s", edited.getLocal().getAbsolute()));
        }
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    protected void delete() {
        final Local file = edited.getLocal();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Delete edited file %s", file.getAbsolute()));
        }
        file.trash();
    }

    /**
     * @return The transfer action for edited file
     */
    protected TransferAction getAction() {
        return TransferAction.ACTION_OVERWRITE;
    }

    /**
     * Open the file in the parent directory
     */
    @Override
    public void open() {
        final BackgroundAction<Void> background = new AbstractBackgroundAction<Void>() {
            private final Transfer download = new DownloadTransfer(edited) {
                @Override
                public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
                    return getAction();
                }

                @Override
                protected boolean shouldOpenWhenComplete() {
                    return false;
                }
            };

            @Override
            public void run() {
                // Delete any existing file which might be used by a watch editor already
                edited.getLocal().trash();
                final TransferOptions options = new TransferOptions();
                options.closeSession = false;
                options.quarantine = false;
                download.start(new TransferPrompt() {
                    @Override
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
                if(download.isComplete()) {
                    edited.getSession().message(MessageFormat.format(
                            Locale.localizedString("Compute MD5 hash of {0}", "Status"), edited.getName()));
                    checksum = edited.getLocal().attributes().getChecksum();
                }
            }

            @Override
            public void cleanup() {
                if(download.isComplete()) {
                    final Permission permissions = edited.getLocal().attributes().getPermission();
                    // Update local permissions to make sure the file is readable and writable for editing.
                    permissions.getOwnerPermissions()[Permission.READ] = true;
                    permissions.getOwnerPermissions()[Permission.WRITE] = true;
                    edited.getLocal().writeUnixPermission(permissions);
                    // Important, should always be run on the main thread; otherwise applescript crashes
                    AbstractEditor.this.edit();
                }
            }
        };
        if(log.isDebugEnabled()) {
            log.debug(String.format("Download file for edit %s", edited.getLocal().getAbsolute()));
        }
        this.open(background);
    }

    /**
     * Upload changes to server if checksum of local file has changed since last edit.
     */
    @Override
    public void save() {
        final BackgroundAction<Void> background = new AbstractBackgroundAction<Void>() {
            @Override
            public void run() {
                // If checksum still the same no need for save
                edited.getSession().message(MessageFormat.format(
                        Locale.localizedString("Compute MD5 hash of {0}", "Status"), edited.getName()));
                if(checksum.equals(edited.getLocal().attributes().getChecksum())) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("File %s not modified", edited.getLocal()));
                    }
                    return;
                }
                checksum = edited.getLocal().attributes().getChecksum();
                final TransferOptions options = new TransferOptions();
                options.closeSession = false;
                final Transfer upload = new UploadTransfer(edited) {
                    @Override
                    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                };
                upload.start(new TransferPrompt() {
                    @Override
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
                if(upload.isComplete()) {
                    if(isClosed()) {
                        delete();
                    }
                    setDirty(false);
                }
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        edited.getName());
            }
        };
        if(log.isDebugEnabled()) {
            log.debug(String.format("Upload changes for %s", edited.getLocal().getAbsolute()));
        }
        this.save(background);
    }
}