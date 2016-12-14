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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
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
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

public abstract class AbstractEditor implements Editor {
    private static final Logger log = Logger.getLogger(AbstractEditor.class);

    /**
     * File has changed but not uploaded yet
     */
    private boolean modified;

    /**
     * The edited path
     */
    private final Path remote;

    private final Local local;

    /**
     * The editor application
     */
    private final Application application;

    /**
     * Store checksum of downloaded file to detect modifications
     */
    private Checksum checksum;

    /**
     * Session for transfers
     */
    private final SessionPool session;

    private final ProgressListener listener;

    private final ApplicationLauncher applicationLauncher;

    private final ApplicationFinder applicationFinder;

    public AbstractEditor(final Application application,
                          final SessionPool session,
                          final Path file,
                          final ProgressListener listener) {
        this(application, session, file, ApplicationLauncherFactory.get(), ApplicationFinderFactory.get(),
                listener);
    }

    public AbstractEditor(final Application application,
                          final SessionPool session,
                          final Path file,
                          final ApplicationLauncher launcher,
                          final ApplicationFinder finder,
                          final ProgressListener listener) {
        this.applicationLauncher = launcher;
        this.applicationFinder = finder;
        this.application = application;
        if(file.isSymbolicLink() && PreferencesFactory.get().getBoolean("editor.upload.symboliclink.resolve")) {
            this.remote = file.getSymlinkTarget();
        }
        else {
            this.remote = file;
        }
        this.local = TemporaryFileServiceFactory.get().create(session.getHost().getUuid(), remote);
        this.session = session;
        this.listener = listener;
    }

    public Path getRemote() {
        return remote;
    }

    public Local getLocal() {
        return local;
    }

    public Application getApplication() {
        return application;
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
            log.debug(String.format("Delete edited file %s", local));
        }
        try {
            LocalTrashFactory.get().trash(local);
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Failure trashing edited file %s %s", local, e.getMessage()));
        }
    }

    /**
     * Open the file in the parent directory
     */
    @Override
    public Worker<Transfer> open(final ApplicationQuitCallback quit, final TransferErrorCallback error,
                                 final FileWatcherListener listener) {
        final Worker<Transfer> worker = new EditOpenWorker(session.getHost(), this, error,
                new ApplicationQuitCallback() {
                    @Override
                    public void callback() {
                        quit.callback();
                        delete();
                    }
                }, this.listener, listener) {
            @Override
            public void cleanup(final Transfer download) {
                // Save checksum before edit
                try {
                    checksum = ChecksumComputeFactory.get(HashAlgorithm.md5).compute(local.getInputStream(), new TransferStatus());
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Error computing checksum for %s. %s", local, e.getDetail()));
                }

            }
        };
        if(log.isDebugEnabled()) {
            log.debug(String.format("Download file for edit %s", local));
        }
        return worker;
    }

    /**
     * Watch for changes in external editor
     *
     * @param quit Callback
     */
    protected void edit(final ApplicationQuitCallback quit, final FileWatcherListener listener) throws IOException {
        if(!applicationFinder.isInstalled(application)) {
            log.warn(String.format("No editor application configured for %s", local));
            if(applicationLauncher.open(local)) {
                this.watch(local, listener);
            }
            else {
                throw new IOException(String.format("Failed to open default application for %s",
                        local.getName()));
            }
        }
        else if(applicationLauncher.open(local, application, quit)) {
            this.watch(local, listener);
        }
        else {
            throw new IOException(String.format("Failed to open application %s for %s",
                    application.getName(), local.getName()));
        }
    }

    protected abstract void watch(Local local, FileWatcherListener listener) throws IOException;

    /**
     * Upload changes to server if checksum of local file has changed since last edit.
     */
    @Override
    public Worker<Transfer> save(final TransferErrorCallback error) {
        // If checksum still the same no need for save
        final Checksum current;
        try {
            listener.message(MessageFormat.format(
                    LocaleFactory.localizedString("Compute MD5 hash of {0}", "Status"), local.getName()));
            current = ChecksumComputeFactory.get(HashAlgorithm.md5).compute(local.getInputStream(), new TransferStatus());
        }
        catch(BackgroundException e) {
            log.warn(String.format("Error computing checksum for %s. %s", local, e.getDetail()));
            return Worker.empty();
        }
        if(current.equals(checksum)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("File %s not modified with checksum %s", local, current));
            }
        }
        else {
            if(log.isInfoEnabled()) {
                log.info(String.format("Save new checksum %s for file %s", current, local));
            }
            // Store current checksum
            checksum = current;
            final Worker<Transfer> worker = new EditSaveWorker(session.getHost(), this, error, listener);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Upload changes for %s", local));
            }
            return worker;
        }
        return Worker.empty();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            this.delete();
        }
        finally {
            super.finalize();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractEditor that = (AbstractEditor) o;
        if(application != null ? !application.equals(that.application) : that.application != null) {
            return false;
        }
        if(local != null ? !local.equals(that.local) : that.local != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = local != null ? local.hashCode() : 0;
        result = 31 * result + (application != null ? application.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractEditor{");
        sb.append("application=").append(application);
        sb.append(", local=").append(local);
        sb.append('}');
        return sb.toString();
    }
}