package ch.cyberduck.core.transfer.copy;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransferFilter implements TransferPathFilter {
    private static final Logger log = Logger.getLogger(CopyTransferFilter.class);

    private Session<?> destination;

    private Find find;

    private Attributes attribute;

    private AclPermission acl;

    private final Map<Path, Path> files;

    private UploadFilterOptions options;

    public CopyTransferFilter(final Session<?> source, final Session<?> destination, final Map<Path, Path> files) {
        this(source, destination, files, new UploadFilterOptions(),
                new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size")));
    }

    public CopyTransferFilter(final Session<?> source, final Session<?> destination,
                              final Map<Path, Path> files, final UploadFilterOptions options, final PathCache cache) {
        this.destination = destination;
        this.files = files;
        this.options = options;
        this.find = destination.getFeature(Find.class).withCache(cache);
        this.attribute = source.getFeature(Attributes.class).withCache(cache);
        this.acl = source.getFeature(AclPermission.class);
    }

    @Override
    public TransferPathFilter withCache(final PathCache cache) {
        attribute.withCache(cache);
        return this;
    }

    @Override
    public boolean accept(final Path source, final Local local, final TransferStatus parent) throws BackgroundException {
        return true;
    }

    @Override
    public TransferStatus prepare(final Path source, final Local local, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = new TransferStatus();
        // Read remote attributes
        final PathAttributes attributes = attribute.find(source);
        if(source.isFile()) {
            // Content length
            status.setLength(attributes.getSize());
        }
        status.setRemote(attributes);
        if(this.options.permissions) {
            status.setPermission(attributes.getPermission());
        }
        if(this.options.timestamp) {
            status.setTimestamp(attributes.getModificationDate());
        }
        if(this.options.acl) {
            if(acl != null) {
                status.setAcl(acl.getPermission(source));
            }
        }
        if(parent.isExists()) {
            // Do not attempt to create a directory that already exists
            final Path destination = files.get(source);
            if(find.find(destination)) {
                status.setExists(true);
            }
        }
        return status;
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status,
                      final ProgressListener listener) {
        //
    }

    @Override
    public void complete(final Path source, final Local local, final TransferOptions options,
                         final TransferStatus status, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", source.getAbsolute(), status));
        }
        if(status.isComplete()) {
            if(!Permission.EMPTY.equals(status.getPermission())) {
                final UnixPermission feature = destination.getFeature(UnixPermission.class);
                if(feature != null) {
                    if(!Permission.EMPTY.equals(status.getPermission())) {
                        try {
                            listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                                    files.get(source).getName(), status.getPermission()));
                            feature.setUnixPermission(files.get(source), status.getPermission());
                        }
                        catch(BackgroundException e) {
                            // Ignore
                            log.warn(e.getMessage());
                        }
                    }
                }
            }
            if(!Acl.EMPTY.equals(status.getAcl())) {
                final AclPermission feature = destination.getFeature(AclPermission.class);
                if(feature != null) {
                    try {
                        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                                files.get(source).getName(), status.getAcl()));
                        feature.setPermission(files.get(source), status.getAcl());
                    }
                    catch(BackgroundException e) {
                        // Ignore
                        log.warn(e.getMessage());
                    }
                }
            }
            if(status.getTimestamp() != null) {
                final Timestamp timestamp = destination.getFeature(Timestamp.class);
                if(timestamp != null) {
                    listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing timestamp of {0} to {1}", "Status"),
                            source.getName(), UserDateFormatterFactory.get().getShortFormat(status.getTimestamp())));
                    try {
                        timestamp.setTimestamp(files.get(source), status.getTimestamp());
                    }
                    catch(BackgroundException e) {
                        // Ignore
                        log.warn(e.getMessage());
                    }
                }
            }
        }
    }
}