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
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.ui.browser.SearchFilterFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Map;

public abstract class AbstractCopyFilter implements TransferPathFilter {
    private static final Logger log = LogManager.getLogger(AbstractCopyFilter.class);

    protected final Session<?> sourceSession;
    protected final Session<?> targetSession;
    protected final Map<Path, Path> files;

    private final Filter<Path> hidden = SearchFilterFactory.HIDDEN_FILTER;

    protected Find find;
    protected AttributesFinder attribute;
    private final UploadFilterOptions options;

    public AbstractCopyFilter(final Session<?> source, final Session<?> destination, final Map<Path, Path> files) {
        this(source, destination, files, new UploadFilterOptions(destination.getHost()));
    }

    public AbstractCopyFilter(final Session<?> source, final Session<?> destination,
                              final Map<Path, Path> files, final UploadFilterOptions options) {
        this.sourceSession = source;
        this.targetSession = destination;
        this.files = files;
        this.options = options;
        this.find = destination.getFeature(Find.class, new DefaultFindFeature(destination));
        this.attribute = destination.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(destination));
    }

    @Override
    public AbstractCopyFilter withFinder(final Find finder) {
        this.find = finder;
        return this;
    }

    @Override
    public AbstractCopyFilter withAttributes(final AttributesFinder attributes) {
        this.attribute = attributes;
        return this;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local n, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        final TransferStatus status = new TransferStatus()
            .hidden(!hidden.accept(file))
            .withLockId(parent.getLockId());
        if(parent.isExists()) {
            final Path target = files.get(file);
            if(find.find(target)) {
                // Do not attempt to create a directory that already exists
                status.setExists(true);
                // Read remote attributes
                status.setRemote(attribute.find(target));
            }
        }
        // Read remote attributes from source
        final PathAttributes attributes = sourceSession.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(sourceSession)).find(file);
        if(file.isFile()) {
            // Content length
            status.setLength(attributes.getSize());
        }
        if(file.isDirectory()) {
            status.setLength(0L);
        }
        if(options.permissions) {
            status.setPermission(attributes.getPermission());
        }
        if(options.acl) {
            final AclPermission sourceFeature = sourceSession.getFeature(AclPermission.class);
            if(sourceFeature != null) {
                progress.message(MessageFormat.format(LocaleFactory.localizedString("Getting permission of {0}", "Status"),
                    file.getName()));
                try {
                    status.setAcl(sourceFeature.getPermission(file));
                }
                catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                    final AclPermission targetFeature = targetSession.getFeature(AclPermission.class);
                    if(targetFeature != null) {
                        status.setAcl(targetFeature.getDefault(file.getType()));
                    }
                }
            }
            else {
                final AclPermission targetFeature = targetSession.getFeature(AclPermission.class);
                if(targetFeature != null) {
                    status.setAcl(targetFeature.getDefault(file.getType()));
                }
            }
        }
        if(options.timestamp) {
            status.setModified(attributes.getModificationDate());
            status.setCreated(attributes.getCreationDate());
        }
        if(options.metadata) {
            final Headers sourceFeature = sourceSession.getFeature(Headers.class);
            if(sourceFeature != null) {
                progress.message(MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                    file.getName()));
                try {
                    status.setMetadata(sourceFeature.getMetadata(file));
                }
                catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                    // Ignore
                }
            }
        }
        if(options.encryption) {
            final Encryption sourceFeature = sourceSession.getFeature(Encryption.class);
            if(sourceFeature != null) {
                progress.message(MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                    file.getName()));
                try {
                    status.setEncryption(sourceFeature.getEncryption(file));
                }
                catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                    final Encryption targetFeature = targetSession.getFeature(Encryption.class);
                    if(targetFeature != null) {
                        status.setEncryption(targetFeature.getDefault(file));
                    }
                }
            }
            else {
                final Encryption targetFeature = targetSession.getFeature(Encryption.class);
                if(targetFeature != null) {
                    status.setEncryption(targetFeature.getDefault(file));
                }
            }
        }
        if(options.redundancy) {
            if(file.isFile()) {
                final Redundancy sourceFeature = sourceSession.getFeature(Redundancy.class);
                if(sourceFeature != null) {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                        file.getName()));
                    try {
                        status.setStorageClass(sourceFeature.getClass(file));
                    }
                    catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                        final Redundancy targetFeature = targetSession.getFeature(Redundancy.class);
                        if(targetFeature != null) {
                            status.setStorageClass(targetFeature.getDefault());
                        }
                    }
                }
                else {
                    final Redundancy targetFeature = targetSession.getFeature(Redundancy.class);
                    if(targetFeature != null) {
                        status.setStorageClass(targetFeature.getDefault());
                    }
                }
            }
        }
        if(options.checksum) {
            // Save checksum and pass to transfer status when copying from file
            status.setChecksum(file.attributes().getChecksum());
        }
        return status;
    }

    @Override
    public void apply(final Path source, final Local n, final TransferStatus status, final ProgressListener listener) {
        //
    }

    @Override
    public void complete(final Path source, final Local n, final TransferStatus status, final ProgressListener listener) {
        log.debug("Complete {} with status {}", source.getAbsolute(), status);
        if(status.isComplete()) {
            final Path target = files.get(source);
            if(!Permission.EMPTY.equals(status.getPermission())) {
                final UnixPermission feature = targetSession.getFeature(UnixPermission.class);
                if(feature != null) {
                    if(!Permission.EMPTY.equals(status.getPermission())) {
                        try {
                            listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                                target.getName(), status.getPermission()));
                            feature.setUnixPermission(target, status);
                        }
                        catch(BackgroundException e) {
                            // Ignore
                            log.warn(e.getMessage());
                        }
                    }
                }
            }
            if(!Acl.EMPTY.equals(status.getAcl())) {
                final AclPermission feature = targetSession.getFeature(AclPermission.class);
                if(feature != null) {
                    try {
                        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                            target.getName(), status.getAcl()));
                        feature.setPermission(target, status.getAcl());
                    }
                    catch(BackgroundException e) {
                        // Ignore
                        log.warn(e.getMessage());
                    }
                }
            }
            if(status.getModified() != null) {
                final Timestamp timestamp = targetSession.getFeature(Timestamp.class);
                if(timestamp != null) {
                    listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing timestamp of {0} to {1}", "Status"),
                            target.getName(), UserDateFormatterFactory.get().getShortFormat(status.getModified())));
                    try {
                        timestamp.setTimestamp(target, status);
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
