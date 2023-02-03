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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.local.TemporaryFileService;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

public abstract class AbstractEditor implements Editor {
    private static final Logger log = LogManager.getLogger(AbstractEditor.class);

    private final Host host;

    private final TemporaryFileService temp = TemporaryFileServiceFactory.instance();

    /**
     * File has changed but not uploaded yet
     */
    private boolean modified;

    /**
     * The edited path
     */
    private final Path file;
    private final Local temporary;

    /**
     * Store checksum of downloaded file to detect modifications
     */
    private Checksum checksum;

    private final ProgressListener progress;
    private final ApplicationLauncher launcher;
    private final ApplicationFinder finder;
    private final NotificationService notification = NotificationServiceFactory.get();

    public AbstractEditor(final Host host, final Path file, final ProgressListener listener) {
        this(host, file, ApplicationLauncherFactory.get(), ApplicationFinderFactory.get(), listener);
    }

    public AbstractEditor(final Host host, final Path file, final ApplicationLauncher launcher,
                          final ApplicationFinder finder,
                          final ProgressListener listener) {
        this.host = host;
        if(file.isSymbolicLink() && PreferencesFactory.get().getBoolean("editor.upload.symboliclink.resolve")) {
            this.file = file.getSymlinkTarget();
        }
        else {
            this.file = file;
        }
        this.temporary = temp.create(String.format("editor-%s", host.getUuid()), this.file);
        this.launcher = launcher;
        this.finder = finder;
        this.progress = listener;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public void delete() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Delete edited file %s", temporary));
        }
        try {
            temporary.delete();
        }
        catch(AccessDeniedException | NotfoundException e) {
            log.warn(String.format("Failure trashing edited file %s. %s", temporary, e.getMessage()));
        }
    }

    /**
     * @param application Editor application
     * @param callback    Quit callback notified when editor application is closed
     */
    @Override
    public Worker<Transfer> open(final Application application, final ApplicationQuitCallback callback, final FileWatcherListener listener) {
        final Worker<Transfer> worker = new EditOpenWorker(host, this, application, file,
                temporary, progress, listener, notification) {
            @Override
            public void cleanup(final Transfer download) {
                // Save checksum before edit
                try {
                    checksum = ChecksumComputeFactory.get(HashAlgorithm.md5).compute(temporary.getInputStream(), new TransferStatus());
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Error computing checksum for %s. %s", temporary, e));
                }

            }
        };
        if(log.isDebugEnabled()) {
            log.debug(String.format("Download file for edit %s", temporary));
        }
        return worker;
    }

    /**
     * Watch for changes in external editor
     *
     * @param application Editor
     * @param file        Remote file
     * @param temporary   Temporary file
     */
    protected void edit(final Application application, final Path file, final Local temporary, final FileWatcherListener listener) throws IOException {
        final ApplicationQuitCallback quit = new ApplicationQuitCallback() {
            @Override
            public void callback() {
                close();
                delete();
            }
        };
        if(!finder.isInstalled(application)) {
            log.warn(String.format("No editor application configured for %s", temporary));
            if(launcher.open(temporary)) {
                this.watch(application, temporary, listener, quit);
            }
            else {
                throw new IOException(String.format("Failed to open default application for %s",
                        temporary.getName()));
            }
        }
        else if(launcher.open(temporary, application)) {
            this.watch(application, temporary, listener, quit);
        }
        else {
            throw new IOException(String.format("Failed to open application %s for %s",
                    application.getName(), temporary.getName()));
        }
    }

    protected abstract void watch(Application application, Local temporary,
                                  FileWatcherListener listener, ApplicationQuitCallback quit) throws IOException;

    /**
     * Upload changes to server if checksum of local file has changed since last edit.
     */
    @Override
    public Worker<Transfer> save(final TransferErrorCallback error) {
        // If checksum still the same no need for save
        final Checksum current;
        try {
            progress.message(MessageFormat.format(
                    LocaleFactory.localizedString("Compute MD5 hash of {0}", "Status"), temporary.getName()));
            current = ChecksumComputeFactory.get(HashAlgorithm.md5).compute(temporary.getInputStream(), new TransferStatus());
        }
        catch(BackgroundException e) {
            log.warn(String.format("Error computing checksum for %s. %s", temporary, e));
            return Worker.empty();
        }
        if(current.equals(checksum)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("File %s not modified with checksum %s", temporary, current));
            }
        }
        else {
            if(log.isInfoEnabled()) {
                log.info(String.format("Save new checksum %s for file %s", current, temporary));
            }
            // Store current checksum
            checksum = current;
            final Worker<Transfer> worker = new EditSaveWorker(host, this, file, temporary, error, progress, notification);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Upload changes for %s", temporary));
            }
            return worker;
        }
        return Worker.empty();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractEditor{");
        sb.append("modified=").append(modified);
        sb.append(", checksum=").append(checksum);
        sb.append('}');
        return sb.toString();
    }
}
