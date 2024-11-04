package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

public class WriteAclWorker extends Worker<Boolean> {
    private static final Logger log = LogManager.getLogger(WriteAclWorker.class);

    /**
     * Selected files.
     */
    private final List<Path> files;

    /**
     * Permissions to apply to files.
     */
    private final Acl acl;

    /**
     * Descend into directories
     */
    private final RecursiveCallback<Acl> callback;

    private final ProgressListener listener;

    public WriteAclWorker(final List<Path> files,
                          final Acl acl, final boolean recursive,
                          final ProgressListener listener) {
        this(files, acl, new BooleanRecursiveCallback<>(recursive), listener);
    }

    public WriteAclWorker(final List<Path> files,
                          final Acl acl, final RecursiveCallback<Acl> callback,
                          final ProgressListener listener) {
        this.files = files;
        this.acl = acl;
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final AclPermission feature = session.getFeature(AclPermission.class);
        log.debug("Run with feature {}", feature);
        for(Path file : files) {
            this.write(session, feature, file);
        }
        return true;
    }

    protected void write(final Session<?> session, final AclPermission feature, final Path file) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                file.getName(), acl));
        final TransferStatus status = new TransferStatus().withAcl(acl);
        feature.setPermission(file, status);
        if(!PathAttributes.EMPTY.equals(status.getResponse())) {
            file.withAttributes(status.getResponse());
        }
        else {
            file.attributes().setAcl(acl);
        }
        if(file.isVolume()) {
            // No recursion when changing container ACL
        }
        else if(file.isDirectory()) {
            if(callback.recurse(file, acl)) {
                for(Path child : session.getFeature(ListService.class).list(file, new WorkerListProgressListener(this, listener))) {
                    this.write(session, feature, child);
                }
            }
        }
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                this.toString(files), acl);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final WriteAclWorker that = (WriteAclWorker) o;
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
        final StringBuilder sb = new StringBuilder("WriteAclWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
