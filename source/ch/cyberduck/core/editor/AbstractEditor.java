package ch.cyberduck.core.editor;

/*
 *  Copyright (c) 2010 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.BackgroundAction;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.MessageFormat;

public abstract class AbstractEditor {

    private static Logger log = Logger.getLogger(AbstractEditor.class);

    /**
     * The file has been closed in the editor while the upload was in progress
     */
    private boolean deferredDelete;
    /**
     * The edited path
     */
    protected Path edited;

    /**
     *
     */
    private String lastchecksum;

    public AbstractEditor(Path path) {
        // Create a copy of the path as to not interfere with the browser. #5524
        this.edited = PathFactory.createPath(path.getSession(), path.<String>getAsDictionary());
        final Local folder = LocalFactory.createLocal(
                new File(Preferences.instance().getProperty("editor.tmp.directory"),
                        edited.getHost().getUuid() + String.valueOf(Path.DELIMITER) + edited.getParent().getAbsolute()));
        final Local local = LocalFactory.createLocal(folder, edited.getName());
        edited.setLocal(local);
    }

    protected void setDeferredDelete(boolean deferredDelete) {
        this.deferredDelete = deferredDelete;
    }

    public boolean isDeferredDelete() {
        return deferredDelete;
    }

    /**
     *
     */
    protected void delete() {
        log.debug("delete");
        edited.getLocal().delete(Preferences.instance().getBoolean("editor.file.trash"));
    }

    /**
     * @return The transfer action for edited file
     */
    protected TransferAction getAction() {
        return TransferAction.ACTION_OVERWRITE;
    }

    /**
     *
     */
    protected abstract void edit();

    /**
     * Open the file in the parent directory
     */
    public void open() {
        BackgroundAction background = new AbstractBackgroundAction() {
            public void run() {
                // Delete any existing file which might be used by a watch editor already
                edited.getLocal().delete(Preferences.instance().getBoolean("editor.file.trash"));
                TransferOptions options = new TransferOptions();
                options.closeSession = false;
                Transfer download = new DownloadTransfer(edited) {
                    @Override
                    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
                        return getAction();
                    }

                    @Override
                    protected boolean shouldOpenWhenComplete() {
                        return false;
                    }
                };
                download.start(new TransferPrompt() {
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
                if(edited.status().isComplete()) {
                    edited.getSession().message(MessageFormat.format(
                            Locale.localizedString("Compute MD5 hash of {0}", "Status"), edited.getName()));
                    lastchecksum = edited.getLocal().attributes().getChecksum();
                }
            }

            @Override
            public void cleanup() {
                if(edited.status().isComplete()) {
                    final Permission permissions = edited.getLocal().attributes().getPermission();
                    // Update local permissions to make sure the file is readable and writable for editing.
                    permissions.getOwnerPermissions()[Permission.READ] = true;
                    permissions.getOwnerPermissions()[Permission.WRITE] = true;
                    edited.getLocal().writeUnixPermission(permissions, false);
                    // Important, should always be run on the main thread; otherwise applescript crashes
                    AbstractEditor.this.edit();
                }
            }
        };
        this.open(background);
    }

    /**
     * @param background
     */
    protected abstract void open(BackgroundAction background);

    public void save() {
        BackgroundAction background = new AbstractBackgroundAction() {
            public void run() {
                // If checksum still the same no need for save
                edited.getSession().message(MessageFormat.format(
                        Locale.localizedString("Compute MD5 hash of {0}", "Status"), edited.getName()));
                if(lastchecksum.equals(edited.getLocal().attributes().getChecksum())) {
                    log.info("File not modified:" + edited.getLocal());
                    return;
                }
                lastchecksum = edited.getLocal().attributes().getChecksum();
                edited.status().reset();
                TransferOptions options = new TransferOptions();
                options.closeSession = false;
                Transfer upload = new UploadTransfer(edited) {
                    @Override
                    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                };
                upload.start(new TransferPrompt() {
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        edited.getName());
            }

            @Override
            public void cleanup() {
                if(edited.status().isComplete()) {
                    if(AbstractEditor.this.isDeferredDelete()) {
                        AbstractEditor.this.delete();
                    }
                    AbstractEditor.this.setDeferredDelete(false);
                }
            }
        };
        this.save(background);
    }

    /**
     * @param background
     */
    protected abstract void save(BackgroundAction background);
}
