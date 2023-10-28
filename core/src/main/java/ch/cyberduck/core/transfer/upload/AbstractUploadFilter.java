package ch.cyberduck.core.transfer.upload;

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
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LocalNotfoundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;
import ch.cyberduck.ui.browser.SearchFilterFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;

public abstract class AbstractUploadFilter implements TransferPathFilter {
    private static final Logger log = LogManager.getLogger(AbstractUploadFilter.class);

    private final PreferencesReader preferences;
    private final Session<?> session;
    private final SymlinkResolver<Local> symlinkResolver;
    private final Filter<Path> hidden = SearchFilterFactory.HIDDEN_FILTER;

    protected Find find;
    protected AttributesFinder attribute;
    protected UploadFilterOptions options;

    public AbstractUploadFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                                final UploadFilterOptions options) {
        this.symlinkResolver = symlinkResolver;
        this.session = session;
        this.options = options;
        this.find = session.getFeature(Find.class);
        this.attribute = session.getFeature(AttributesFinder.class);
        this.preferences = new HostPreferences(session.getHost());
    }

    @Override
    public AbstractUploadFilter withFinder(final Find finder) {
        this.find = finder;
        return this;
    }

    @Override
    public AbstractUploadFilter withAttributes(final AttributesFinder attributes) {
        this.attribute = attributes;
        return this;
    }

    public AbstractUploadFilter withOptions(final UploadFilterOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        if(!local.exists()) {
            // Local file is no more here
            throw new LocalNotfoundException(local.getAbsolute());
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Prepare %s", file));
        }
        final TransferStatus status = new TransferStatus()
                .hidden(!hidden.accept(file))
                .withLockId(parent.getLockId());
        // Read remote attributes first
        if(parent.isExists()) {
            if(find.find(file)) {
                status.setExists(true);
                // Read remote attributes
                final PathAttributes attributes = attribute.find(file);
                status.setRemote(attributes);
            }
            else {
                // Look if there is directory or file that clashes with this upload
                if(file.getType().contains(Path.Type.file)) {
                    if(find.find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory)))) {
                        throw new AccessDeniedException(String.format("Cannot replace folder %s with file %s", file.getAbsolute(), local.getName()));
                    }
                }
                if(file.getType().contains(Path.Type.directory)) {
                    if(find.find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.file)))) {
                        throw new AccessDeniedException(String.format("Cannot replace file %s with folder %s", file.getAbsolute(), local.getName()));
                    }
                }
            }
        }
        if(file.isFile()) {
            // Set content length from local file
            if(local.isSymbolicLink()) {
                if(!symlinkResolver.resolve(local)) {
                    // Will resolve the symbolic link when the file is requested.
                    final Local target = local.getSymlinkTarget();
                    status.setLength(target.attributes().getSize());
                }
                // No file size increase for symbolic link to be created on the server
            }
            else {
                // Read file size from filesystem
                status.setLength(local.attributes().getSize());
            }
            if(options.temporary) {
                final Move feature = session.getFeature(Move.class);
                final Path renamed = new Path(file.getParent(),
                        MessageFormat.format(preferences.getProperty("queue.upload.file.temporary.format"),
                                file.getName(), new AlphanumericRandomStringService().random()), file.getType());
                if(feature.isSupported(file, renamed)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Set temporary filename %s", renamed));
                    }
                    // Set target name after transfer
                    status.withRename(renamed).withDisplayname(file);
                    // Remember status of target file for later rename
                    status.getDisplayname().exists(status.isExists());
                    // Keep exist flag for subclasses to determine additional rename strategy
                }
                else {
                    log.warn(String.format("Cannot use temporary filename for upload with missing rename support for %s", file));
                }
            }
            status.withMime(new MappingMimeTypeService().getMime(file.getName()));
        }
        if(file.isDirectory()) {
            status.setLength(0L);
        }
        if(options.permissions) {
            final UnixPermission feature = session.getFeature(UnixPermission.class);
            if(feature != null) {
                if(status.isExists()) {
                    // Already set when reading attributes of file
                    status.setPermission(status.getRemote().getPermission());
                }
                else {
                    status.setPermission(feature.getDefault(local));
                }
            }
            else {
                // Setting target UNIX permissions in transfer status
                status.setPermission(Permission.EMPTY);
            }
        }
        if(options.acl) {
            final AclPermission feature = session.getFeature(AclPermission.class);
            if(feature != null) {
                if(status.isExists()) {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Getting permission of {0}", "Status"),
                            file.getName()));
                    try {
                        status.setAcl(feature.getPermission(file));
                    }
                    catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                        status.setAcl(feature.getDefault(file, local));
                    }
                }
                else {
                    status.setAcl(feature.getDefault(file, local));
                }
            }
            else {
                // Setting target ACL in transfer status
                status.setAcl(Acl.EMPTY);
            }
        }
        if(options.timestamp) {
            final Timestamp feature = session.getFeature(Timestamp.class);
            if(feature != null) {
                if(1L != local.attributes().getModificationDate()) {
                    status.setModified(local.attributes().getModificationDate());
                }
                if(1L != local.attributes().getCreationDate()) {
                    status.setCreated(local.attributes().getCreationDate());
                }
            }
        }
        if(options.metadata) {
            final Headers feature = session.getFeature(Headers.class);
            if(feature != null) {
                if(status.isExists()) {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                            file.getName()));
                    try {
                        status.setMetadata(feature.getMetadata(file));
                    }
                    catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                        status.setMetadata(feature.getDefault(local));
                    }
                }
                else {
                    status.setMetadata(feature.getDefault(local));
                }
            }
        }
        if(options.encryption) {
            final Encryption feature = session.getFeature(Encryption.class);
            if(feature != null) {
                if(status.isExists()) {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                            file.getName()));
                    try {
                        status.setEncryption(feature.getEncryption(file));
                    }
                    catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                        status.setEncryption(feature.getDefault(file));
                    }
                }
                else {
                    status.setEncryption(feature.getDefault(file));
                }
            }
        }
        if(options.redundancy) {
            if(file.isFile()) {
                final Redundancy feature = session.getFeature(Redundancy.class);
                if(feature != null) {
                    if(status.isExists()) {
                        progress.message(MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                                file.getName()));
                        try {
                            status.setStorageClass(feature.getClass(file));
                        }
                        catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                            status.setStorageClass(feature.getDefault());
                        }
                    }
                    else {
                        status.setStorageClass(feature.getDefault());
                    }
                }
            }
        }
        if(options.checksum) {
            if(file.isFile()) {
                final ChecksumCompute feature = session.getFeature(Write.class).checksum(file, status);
                if(feature != null) {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Calculate checksum for {0}", "Status"),
                            file.getName()));
                    try {
                        status.setChecksum(feature.compute(local.getInputStream(), status));
                    }
                    catch(LocalAccessDeniedException e) {
                        // Ignore failure reading file when in sandbox when we miss a security scoped access bookmark.
                        // Lock for files is obtained only later in Transfer#pre
                        log.warn(e.getMessage());
                    }
                }
            }
        }
        return status;
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status,
                      final ProgressListener listener) throws BackgroundException {
        if(file.isFile()) {
            if(status.isExists() && !status.isAppend()) {
                if(options.versioning) {
                    final Versioning feature = session.getFeature(Versioning.class);
                    if(feature != null && feature.getConfiguration(file).isEnabled()) {
                        if(feature.save(file)) {
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Clear exist flag for file %s", file));
                            }
                            status.exists(false).getDisplayname().exists(false);
                        }
                    }
                }
            }
        }
        if(status.getRename().remote != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Clear exist flag for file %s", local));
            }
            // Reset exist flag after subclass hae applied strategy
            status.setExists(false);
        }
    }

    @Override
    public void complete(final Path file, final Local local,
                         final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", file.getAbsolute(), status));
        }
        if(status.isComplete()) {
            if(!Permission.EMPTY.equals(status.getPermission())) {
                final UnixPermission feature = session.getFeature(UnixPermission.class);
                if(feature != null) {
                    try {
                        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                                file.getName(), status.getPermission()));
                        feature.setUnixPermission(file, status.getPermission());
                    }
                    catch(BackgroundException e) {
                        // Ignore
                        log.warn(e.getMessage());
                    }
                }
            }
            if(!Acl.EMPTY.equals(status.getAcl())) {
                final AclPermission feature = session.getFeature(AclPermission.class);
                if(feature != null) {
                    try {
                        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                                file.getName(), status.getAcl()));
                        feature.setPermission(file, status.getAcl());
                    }
                    catch(BackgroundException e) {
                        // Ignore
                        log.warn(e.getMessage());
                    }
                }
            }
            if(status.getModified() != null) {
                if(!session.getFeature(Write.class).timestamp()) {
                    final Timestamp feature = session.getFeature(Timestamp.class);
                    if(feature != null) {
                        try {
                            listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing timestamp of {0} to {1}", "Status"),
                                    file.getName(), UserDateFormatterFactory.get().getShortFormat(status.getModified())));
                            feature.setTimestamp(file, status);
                        }
                        catch(BackgroundException e) {
                            // Ignore
                            log.warn(e.getMessage());
                        }
                    }
                }
            }
            if(file.isFile()) {
                if(status.getDisplayname().remote != null) {
                    final Move move = session.getFeature(Move.class);
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Rename file %s to %s", file, status.getDisplayname().remote));
                    }
                    move.move(file, status.getDisplayname().remote, new TransferStatus(status).exists(status.getDisplayname().exists),
                            new Delete.DisabledCallback(), new DisabledConnectionCallback());
                }
            }
        }
    }
}
