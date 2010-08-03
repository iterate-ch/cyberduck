package ch.cyberduck.ui.cocoa.odb;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import org.rococoa.Rococoa;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class Editor {
    private static Logger log = Logger.getLogger(Editor.class);

    private BrowserController controller;

    /**
     * The edited path
     */
    protected Path edited;

    /**
     * The editor application
     */
    protected String bundleIdentifier;

    /**
     * @param controller
     * @param bundleIdentifier
     */
    public Editor(BrowserController controller, String bundleIdentifier, Path path) {
        this.controller = controller;
        this.bundleIdentifier = bundleIdentifier;
        this.edited = path;
    }

    /**
     * @return The transfer action for edited file
     */
    protected TransferAction getAction() {
        return TransferAction.ACTION_OVERWRITE;
    }

    public void open() {
        final Local folder = LocalFactory.createLocal(
                new File(Preferences.instance().getProperty("editor.tmp.directory"),
                        edited.getHost().getHostname() + String.valueOf(Path.DELIMITER) + edited.getParent().getAbsolute()));
        this.open(folder);
    }

    /**
     * Open the file in the parent directory
     * @param folder Where to temporary save the file to.
     */
    public void open(Local folder) {
        this.edited.setLocal(LocalFactory.createLocal(folder, edited.getName()));
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
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
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                        edited.getName());
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
                    Editor.this.edit();
                }
            }
        });
    }

    /**
     * @return True if the editor application is running
     */
    public boolean isOpen() {
        final NSEnumerator apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
        NSObject next;
        while(((next = apps.nextObject()) != null)) {
            NSDictionary app = Rococoa.cast(next, NSDictionary.class);
            final NSObject identifier = app.objectForKey("NSApplicationBundleIdentifier");
            if(identifier.toString().equals(bundleIdentifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    protected abstract void edit();

    /**
     *
     */
    protected void delete() {
        log.debug("delete");
        edited.getLocal().delete(Preferences.instance().getBoolean("editor.file.trash"));
    }

    /**
     * The file has been closed in the editor while the upload was in progress
     */
    private boolean deferredDelete;


    protected void setDeferredDelete(boolean deferredDelete) {
        this.deferredDelete = deferredDelete;
    }

    public boolean isDeferredDelete() {
        return deferredDelete;
    }

    /**
     * Upload the edited file to the server
     */
    protected void save() {
        log.debug("save");
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
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
                    if(Editor.this.isDeferredDelete()) {
                        Editor.this.delete();
                    }
                    Editor.this.setDeferredDelete(false);
                }
            }
        });
    }
}