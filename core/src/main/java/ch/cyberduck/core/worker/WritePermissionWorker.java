package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.PermissionOverwrite;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WritePermissionWorker extends Worker<Boolean> {
    private static final Logger log = LogManager.getLogger(WritePermissionWorker.class);

    /**
     * Selected files.
     */
    private final List<Path> files;

    /**
     * Permissions to apply to files.
     */
    private final Map<Path, Permission> permissions;

    /**
     * Descend into directories
     */
    private final RecursiveCallback<Permission> callback;
    private final ProgressListener listener;

    public WritePermissionWorker(final List<Path> files,
                                 final Permission permission,
                                 final RecursiveCallback<Permission> callback,
                                 final ProgressListener listener) {
        this.files = files;
        this.permissions = new HashMap<>();
        for(Path f : files) {
            this.permissions.put(f, permission);
        }
        this.callback = callback;
        this.listener = listener;
    }

    public WritePermissionWorker(final List<Path> files,
                                 final PermissionOverwrite overwrite,
                                 final RecursiveCallback<Permission> callback,
                                 final ProgressListener listener) {
        this.files = files;
        this.permissions = new HashMap<>();
        for(Path f : files) {
            this.permissions.put(f, overwrite.resolve(f.attributes().getPermission()));
        }
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final UnixPermission feature = session.getFeature(UnixPermission.class);
        log.debug("Run with feature {}", feature);
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            final Permission merged = permissions.get(file);
            this.write(session, feature, file, merged);
        }
        return true;
    }

    protected void write(final Session<?> session, final UnixPermission feature, final Path file, final Permission permission) throws BackgroundException {
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
            file.getName(), permission));
        final TransferStatus status = new TransferStatus().withPermission(permission);
        feature.setUnixPermission(file, status);
        if(!PathAttributes.EMPTY.equals(status.getResponse())) {
            file.withAttributes(status.getResponse());
        }
        else {
            file.attributes().setPermission(permission);
        }
        if(file.isDirectory()) {
            if(callback.recurse(file, permission)) {
                for(Path child : session.getFeature(ListService.class).list(file, new WorkerListProgressListener(this, listener))) {
                    this.write(session, feature, child, permission);
                }
            }
        }
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
            this.toString(files), permissions.values().iterator().next());
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final WritePermissionWorker that = (WritePermissionWorker) o;
        if(!Objects.equals(files, that.files)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WritePermissionWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }

}
