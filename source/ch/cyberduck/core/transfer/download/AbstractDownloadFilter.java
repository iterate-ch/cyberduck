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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;
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

    private SymlinkResolver<Path> symlinkResolver;

    private final QuarantineService quarantine
            = QuarantineServiceFactory.get();

    private final ApplicationLauncher launcher
            = ApplicationLauncherFactory.get();

    private Preferences preferences
            = Preferences.instance();

    private final IconService icon
            = IconServiceFactory.get();

    private Session<?> session;

    private Attributes attribute;

    private DownloadFilterOptions options;

    protected AbstractDownloadFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session,
                                     final DownloadFilterOptions options) {
        this.symlinkResolver = symlinkResolver;
        this.session = session;
        this.options = options;
        this.attribute = session.getFeature(Attributes.class);
    }

    @Override
    public AbstractDownloadFilter withCache(final Cache<Path> cache) {
        attribute.withCache(cache);
        return this;
    }

    public AbstractDownloadFilter withOptions(final DownloadFilterOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        final Local volume = local.getVolume();
        if(!volume.exists()) {
            throw new NotfoundException(String.format("Volume %s not mounted", volume.getAbsolute()));
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = new TransferStatus();
        if(parent.isExists()) {
            if(local.exists()) {
                // Do not attempt to create a directory that already exists
                status.setExists(true);
            }
        }
        final PathAttributes attributes;
        if(file.isSymbolicLink()) {
            // A server will resolve the symbolic link when the file is requested.
            final Path target = file.getSymlinkTarget();
            // Read remote attributes of symlink target
            attributes = attribute.find(target);
            if(!symlinkResolver.resolve(file)) {
                if(file.isFile()) {
                    // Content length
                    status.setLength(attributes.getSize());
                }
            }
            // No file size increase for symbolic link to be created locally
        }
        else {
            // Read remote attributes
            attributes = attribute.find(file);
            if(file.isFile()) {
                // Content length
                status.setLength(attributes.getSize());
            }
        }
        status.setRemote(attributes);
        if(this.options.timestamp) {
            status.setTimestamp(attributes.getModificationDate());
        }
        if(this.options.permissions) {
            Permission permission = Permission.EMPTY;
            if(preferences.getBoolean("queue.download.permissions.default")) {
                if(file.isFile()) {
                    permission = new Permission(
                            preferences.getInteger("queue.download.permissions.file.default"));
                }
                if(file.isDirectory()) {
                    permission = new Permission(
                            preferences.getInteger("queue.download.permissions.folder.default"));
                }
            }
            else {
                permission = attributes.getPermission();
            }
            status.setPermission(permission);
        }
        status.setAcl(attributes.getAcl());
        return status;
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status,
                      final ProgressListener listener) throws BackgroundException {
        if(file.isFile()) {
            // No icon update if disabled
            if(options.icon) {
                icon.set(local, new TransferStatus());
            }
        }
    }

    /**
     * Update timestamp and permission
     */
    @Override
    public void complete(final Path file, final Local local,
                         final TransferOptions options, final TransferStatus status,
                         final ProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", file.getAbsolute(), status));
        }
        if(status.isComplete()) {
            if(file.isFile()) {
                // Remove custom icon if complete. The Finder will display the default icon for this file type
                if(this.options.icon) {
                    icon.set(local, status);
                    icon.remove(local);
                }
                final DescriptiveUrl provider = session.getFeature(UrlProvider.class).toUrl(file).find(DescriptiveUrl.Type.provider);
                if(!DescriptiveUrl.EMPTY.equals(provider)) {
                    if(options.quarantine) {
                        // Set quarantine attributes
                        quarantine.setQuarantine(local, new HostUrlProvider(false).get(session.getHost()),
                                provider.getUrl());
                    }
                    if(this.options.wherefrom) {
                        // Set quarantine attributes
                        quarantine.setWhereFrom(local, provider.getUrl());
                    }
                }
                if(options.open) {
                    launcher.open(local);
                }
            }
            launcher.bounce(local);
        }
        if(status.isComplete()) {
            if(!Permission.EMPTY.equals(status.getPermission())) {
                if(file.isDirectory()) {
                    // Make sure we can read & write files to directory created.
                    status.getPermission().setUser(status.getPermission().getUser().or(Permission.Action.read).or(Permission.Action.write).or(Permission.Action.execute));
                }
                if(file.isFile()) {
                    // Make sure the owner can always read and write.
                    status.getPermission().setUser(status.getPermission().getUser().or(Permission.Action.read).or(Permission.Action.write));
                }
                if(log.isInfoEnabled()) {
                    log.info(String.format("Updating permissions of %s to %s", local, status.getPermission()));
                }
                try {
                    local.attributes().setPermission(status.getPermission());
                }
                catch(AccessDeniedException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
            if(status.getTimestamp() != null) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Updating timestamp of %s to %d", local, status.getTimestamp()));
                }
                try {
                    local.attributes().setModificationDate(status.getTimestamp());
                }
                catch(AccessDeniedException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
        }
    }
}
