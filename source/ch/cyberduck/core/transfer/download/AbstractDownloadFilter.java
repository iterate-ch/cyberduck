package ch.cyberduck.core.transfer.download;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.IconService;
import ch.cyberduck.core.local.IconServiceFactory;
import ch.cyberduck.core.local.QuarantineService;
import ch.cyberduck.core.local.QuarantineServiceFactory;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractDownloadFilter implements TransferPathFilter {
    private static final Logger log = Logger.getLogger(AbstractDownloadFilter.class);

    private SymlinkResolver symlinkResolver;

    private final QuarantineService quarantine
            = QuarantineServiceFactory.get();

    private final ApplicationLauncher launcher
            = ApplicationLauncherFactory.get();

    private final IconService icon
            = IconServiceFactory.get();

    private Session<?> session;

    protected AbstractDownloadFilter(final SymlinkResolver symlinkResolver, final Session<?> session) {
        this.symlinkResolver = symlinkResolver;
        this.session = session;
    }

    @Override
    public boolean accept(final Path file, final TransferStatus parent) throws BackgroundException {
        if(file.attributes().isSymbolicLink()) {
            if(!symlinkResolver.resolve(file)) {
                return symlinkResolver.include(file);
            }
        }
        final Local volume = file.getLocal().getVolume();
        if(!volume.exists()) {
            throw new NotfoundException(String.format("Volume %s not mounted", volume.getAbsolute()));
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = new TransferStatus();
        if(file.attributes().isFile()) {
            if(file.attributes().isSymbolicLink()) {
                if(symlinkResolver.resolve(file)) {
                    // No file size increase for symbolic link to be created locally
                }
                else {
                    // A server will resolve the symbolic link when the file is requested.
                    final Path target = file.getSymlinkTarget();
                    status.setLength(target.attributes().getSize());
                }
            }
            else {
                // Read file size
                status.setLength(file.attributes().getSize());
            }
        }
        if(file.attributes().isDirectory()) {
            // Do not attempt to create a directory that already exists
            if(file.getLocal().exists()) {
                status.setExists(true);
            }
        }
        return status;
    }

    @Override
    public void apply(final Path file, final TransferStatus parent) throws BackgroundException {
        if(file.attributes().isFile()) {
            // No icon update if disabled
            if(Preferences.instance().getBoolean("queue.download.icon.update")) {
                icon.set(file.getLocal(), 0);
            }
        }
    }

    /**
     * Update timestamp and permission
     */
    @Override
    public void complete(final Path file,
                         final TransferOptions options, final TransferStatus status,
                         final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", file.getAbsolute(), status));
        }
        if(status.isComplete()) {
            if(file.attributes().isFile()) {
                // Remove custom icon if complete. The Finder will display the default icon for this file type
                if(Preferences.instance().getBoolean("queue.download.icon.update")) {
                    icon.remove(file.getLocal());
                }
                final DescriptiveUrl provider = session.getFeature(UrlProvider.class).toUrl(file).find(DescriptiveUrl.Type.provider);
                if(!DescriptiveUrl.EMPTY.equals(provider)) {
                    if(options.quarantine) {
                        // Set quarantine attributes
                        quarantine.setQuarantine(file.getLocal(), new HostUrlProvider(false).get(session.getHost()),
                                provider.getUrl());
                    }
                    if(Preferences.instance().getBoolean("queue.download.wherefrom")) {
                        // Set quarantine attributes
                        quarantine.setWhereFrom(file.getLocal(), provider.getUrl());
                    }
                }
                if(options.open) {
                    launcher.open(file.getLocal());
                }
            }
            launcher.bounce(file.getLocal());
        }
        if(!status.isCanceled()) {
            if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
                this.permissions(file);
            }
            if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
                this.timestamp(file);
            }
        }
    }

    private void timestamp(final Path file) {
        if(file.attributes().getModificationDate() != -1) {
            long timestamp = file.attributes().getModificationDate();
            if(log.isInfoEnabled()) {
                log.info(String.format("Updating timestamp of %s to %d", file.getLocal(), timestamp));
            }
            file.getLocal().writeTimestamp(-1, timestamp, -1);
        }
    }

    private void permissions(final Path file) {
        Permission permission = Permission.EMPTY;
        if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")) {
            if(file.attributes().isFile()) {
                permission = new Permission(
                        Preferences.instance().getInteger("queue.download.permissions.file.default"));
            }
            if(file.attributes().isDirectory()) {
                permission = new Permission(
                        Preferences.instance().getInteger("queue.download.permissions.folder.default"));
            }
        }
        else {
            permission = file.attributes().getPermission();
        }
        if(!Permission.EMPTY.equals(permission)) {
            if(file.attributes().isDirectory()) {
                // Make sure we can read & write files to directory created.
                permission.setUser(permission.getUser().or(Permission.Action.read).or(Permission.Action.write).or(Permission.Action.execute));
            }
            if(file.attributes().isFile()) {
                // Make sure the owner can always read and write.
                permission.setUser(permission.getUser().or(Permission.Action.read).or(Permission.Action.write));
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Updating permissions of %s to %s", file.getLocal(), permission));
            }
            file.getLocal().writeUnixPermission(permission);
        }
    }
}
