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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.UnixPermission;

import java.text.MessageFormat;
import java.util.List;

public class WritePermissionWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private List<Path> files;

    /**
     * Permissions to apply to files.
     */
    private Permission permission;

    /**
     * Descend into directories
     */
    private RecursiveCallback<Permission> callback;

    private ProgressListener listener;

    public WritePermissionWorker(final List<Path> files,
                                 final Permission permission, final boolean recursive,
                                 final ProgressListener listener) {
        this(files, permission, new RecursiveCallback<Permission>() {
            @Override
            public boolean recurse(final Path directory, final Permission value) {
                return recursive;
            }
        }, listener);
    }

    public WritePermissionWorker(final List<Path> files,
                                 final Permission permission, final RecursiveCallback<Permission> callback,
                                 final ProgressListener listener) {
        this.files = files;
        this.permission = permission;
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final UnixPermission feature = session.getFeature(UnixPermission.class);
        for(Path next : files) {
            this.write(session, feature, next);
        }
        return true;
    }

    protected void write(final Session<?> session, final UnixPermission feature, final Path file) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        if(callback.recurse(file, permission) && file.isFile()) {
            // Do not write executable bit for files if not already set when recursively updating directory. See #1787
            final Permission modified = new Permission(permission.getMode());
            if(!file.attributes().getPermission().getUser().implies(Permission.Action.execute)) {
                modified.setUser(modified.getUser().and(Permission.Action.execute.not()));
            }
            if(!file.attributes().getPermission().getGroup().implies(Permission.Action.execute)) {
                modified.setGroup(modified.getGroup().and(Permission.Action.execute.not()));
            }
            if(!file.attributes().getPermission().getOther().implies(Permission.Action.execute)) {
                modified.setOther((modified.getOther().and(Permission.Action.execute.not())));
            }
            this.write(feature, file, modified);
        }
        else if(callback.recurse(file, permission) && file.isDirectory()) {
            // Do not remove executable bit for folders. See #7316
            final Permission modified = new Permission(permission.getMode());
            if(file.attributes().getPermission().getUser().implies(Permission.Action.execute)) {
                modified.setUser(modified.getUser().or(Permission.Action.execute));
            }
            if(file.attributes().getPermission().getGroup().implies(Permission.Action.execute)) {
                modified.setGroup(modified.getGroup().or(Permission.Action.execute));
            }
            if(file.attributes().getPermission().getOther().implies(Permission.Action.execute)) {
                modified.setOther(modified.getOther().or(Permission.Action.execute));
            }
            this.write(feature, file, modified);
        }
        else {
            this.write(feature, file, permission);
        }
        if(file.isDirectory()) {
            if(callback.recurse(file, permission)) {
                for(Path child : session.list(file, new ActionListProgressListener(this, listener))) {
                    this.write(session, feature, child);
                }
            }
        }
    }

    private void write(final UnixPermission feature, final Path file, final Permission permission) throws BackgroundException {
        final Permission previous = file.attributes().getPermission();
        if(!permission.equals(previous)) {
            final Permission merged = new Permission(permission.getUser(), permission.getGroup(), permission.getOther(),
                    previous.isSticky(), previous.isSetuid(), previous.isSetgid());
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                    file.getName(), merged));
            feature.setUnixPermission(file, merged);
        }
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                this.toString(files), permission);
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
        if(files != null ? !files.equals(that.files) : that.files != null) {
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
