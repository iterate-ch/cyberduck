package ch.cyberduck.ui.action;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.i18n.Locale;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class WritePermissionWorker extends Worker<Void> {

    private Session<?> session;

    private UnixPermission feature;

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
    private boolean recursive;

    public WritePermissionWorker(final Session session, final UnixPermission feature, final List<Path> files,
                                 final Permission permission, final boolean recursive) {
        this.session = session;
        this.feature = feature;
        this.files = files;
        this.permission = permission;
        this.recursive = recursive;
    }

    @Override
    public Void run() throws BackgroundException {
        for(Path next : files) {
            this.write(next);
        }
        return null;
    }

    private void write(final Path file) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                file.getName(), permission));
        if(recursive && file.attributes().isFile()) {
            // Do not write executable bit for files if not already set when recursively updating directory.
            // See #1787
            final Permission modified = new Permission(permission);
            if(!file.attributes().getPermission().getOwnerPermissions()[Permission.EXECUTE]) {
                modified.getOwnerPermissions()[Permission.EXECUTE] = false;
            }
            if(!file.attributes().getPermission().getGroupPermissions()[Permission.EXECUTE]) {
                modified.getGroupPermissions()[Permission.EXECUTE] = false;
            }
            if(!file.attributes().getPermission().getOtherPermissions()[Permission.EXECUTE]) {
                modified.getOtherPermissions()[Permission.EXECUTE] = false;
            }
            if(!file.attributes().getPermission().equals(modified)) {
                feature.setUnixPermission(file, modified);
            }
        }
        else {
            if(!file.attributes().getPermission().equals(permission)) {
                feature.setUnixPermission(file, permission);
            }
        }
        if(recursive) {
            if(file.attributes().isDirectory()) {
                for(Path child : session.list(file, new DisabledListProgressListener())) {
                    this.write(child);
                }
            }
        }
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                this.toString(files), permission);
    }
}
