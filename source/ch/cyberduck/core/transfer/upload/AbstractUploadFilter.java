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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @version $Id$
 */
public abstract class AbstractUploadFilter implements TransferPathFilter {
    private static final Logger log = Logger.getLogger(AbstractUploadFilter.class);

    private SymlinkResolver symlinkResolver;

    private Session<?> session;

    protected Map<Path, Path> temporary
            = new HashMap<Path, Path>();

    private UploadFilterOptions options;

    protected Find find;

    protected Attributes attribute;

    private MimeTypeService mapping
            = new MappingMimeTypeService();

    protected Cache cache;

    public AbstractUploadFilter(final SymlinkResolver symlinkResolver, final Session<?> session, final UploadFilterOptions options) {
        this(symlinkResolver, session, options, new Cache(Preferences.instance().getInteger("transfer.cache.size")));
    }

    public AbstractUploadFilter(final SymlinkResolver symlinkResolver, final Session<?> session,
                                final UploadFilterOptions options, final Cache cache) {
        this.symlinkResolver = symlinkResolver;
        this.session = session;
        this.options = options.withTemporary(options.temporary && session.getFeature(Write.class).temporary());
        this.cache = cache;
        this.find = session.getFeature(Find.class).withCache(cache);
        this.attribute = session.getFeature(Attributes.class).withCache(cache);
    }

    public AbstractUploadFilter withCache(final Cache cache) {
        this.cache = cache;
        find.withCache(cache);
        attribute.withCache(cache);
        return this;
    }

    public AbstractUploadFilter withOptions(final UploadFilterOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public boolean accept(final Path file, final TransferStatus parent) throws BackgroundException {
        if(!file.getLocal().exists()) {
            // Local file is no more here
            throw new NotfoundException(file.getLocal().getAbsolute());
        }
        if(file.attributes().isFile()) {
            if(file.getLocal().attributes().isSymbolicLink()) {
                if(!symlinkResolver.resolve(file)) {
                    return symlinkResolver.include(file);
                }
            }
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = new TransferStatus();
        if(file.attributes().isFile()) {
            if(file.getLocal().attributes().isSymbolicLink()) {
                if(symlinkResolver.resolve(file)) {
                    // No file size increase for symbolic link to be created on the server
                }
                else {
                    // Will resolve the symbolic link when the file is requested.
                    final Local target = file.getLocal().getSymlinkTarget();
                    status.setLength(target.attributes().getSize());
                }
            }
            else {
                // Read file size from filesystem
                status.setLength(file.getLocal().attributes().getSize());
            }
            if(options.temporary) {
                final Path renamed = new Path(file.getParent(), MessageFormat.format(Preferences.instance().getProperty("queue.upload.file.temporary.format"),
                        file.getName(), UUID.randomUUID().toString()), file.attributes(), file.getLocal());
                status.setRenamed(renamed);
                temporary.put(file, renamed);
            }
            status.setMime(mapping.getMime(file.getName()));
        }
        if(parent.isExists()) {
            if(find.find(file)) {
                status.setExists(true);
                if(file.attributes().isFile()) {
                    // Read remote attributes
                    file.setAttributes(attribute.find(file));
                }
            }
        }
        Permission permission = Permission.EMPTY;
        if(status.isExists()) {
            permission = attribute.find(file).getPermission();
        }
        else {
            if(this.options.permissions) {
                if(Preferences.instance().getBoolean("queue.upload.permissions.default")) {
                    if(file.attributes().isFile()) {
                        permission = new Permission(
                                Preferences.instance().getInteger("queue.upload.permissions.file.default"));
                    }
                    else {
                        permission = new Permission(
                                Preferences.instance().getInteger("queue.upload.permissions.folder.default"));
                    }
                }
                else {
                    // Read permissions from local file
                    permission = file.getLocal().attributes().getPermission();
                }
            }
        }
        // Setting target UNIX permissions in transfer status
        status.setPermission(permission);
        Acl acl = Acl.EMPTY;
        if(status.isExists()) {
            final AclPermission feature = session.getFeature(AclPermission.class);
            if(feature != null) {
                acl = feature.getPermission(file);
            }
        }
        else {
            if(this.options.acl) {
                final Permission p;
                if(Preferences.instance().getBoolean("queue.upload.permissions.default")) {
                    if(file.attributes().isFile()) {
                        p = new Permission(
                                Preferences.instance().getInteger("queue.upload.permissions.file.default"));
                    }
                    else {
                        p = new Permission(
                                Preferences.instance().getInteger("queue.upload.permissions.folder.default"));
                    }
                }
                else {
                    // Read permissions from local file
                    p = file.getLocal().attributes().getPermission();
                }
                acl = new Acl();
                if(p.getOther().implies(Permission.Action.read)) {
                    acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
                }
                if(p.getGroup().implies(Permission.Action.read)) {
                    acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
                }
                if(p.getGroup().implies(Permission.Action.write)) {
                    acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.WRITE));
                }
            }
        }
        // Setting target ACL in transfer status
        status.setAcl(acl);
        if(options.timestamp) {
            // Read timestamps from local file
            status.setTimestamp(file.getLocal().attributes().getModificationDate());
        }
        return status;
    }

    @Override
    public void apply(final Path file, final TransferStatus status) throws BackgroundException {
        //
    }

    @Override
    public void complete(final Path file, final TransferOptions options,
                         final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", file.getAbsolute(), status));
        }
        if(status.isComplete()) {
            if(file.attributes().isFile()) {
                if(this.options.temporary) {
                    final Move move = session.getFeature(Move.class);
                    move.move(temporary.get(file), file, status.isExists());
                    temporary.remove(file);
                }
            }
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
            if(status.getTimestamp() != null) {
                final Timestamp feature = session.getFeature(Timestamp.class);
                if(feature != null) {
                    try {
                        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing timestamp of {0} to {1}", "Status"),
                                file.getName(), UserDateFormatterFactory.get().getShortFormat(file.getLocal().attributes().getModificationDate())));
                        feature.setTimestamp(file, status.getTimestamp());
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