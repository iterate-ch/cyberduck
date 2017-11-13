package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;
import java.util.Objects;

public class DefaultCopyFeature implements Copy {

    private Session<?> from;
    private Session<?> to;

    public DefaultCopyFeature(final Session<?> from) {
        this.from = from;
        this.to = from;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        InputStream in;
        StatusOutputStream out;
        in = from.getFeature(Read.class).read(source, new TransferStatus(status), callback);
        Write write = to.getFeature(MultipartWrite.class);
        if(null == write) {
            // Fallback if multipart write is not available
            write = to.getFeature(Write.class);
        }
        out = write.write(target, status, callback);
        new StreamCopier(status, status).transfer(in, out);
        final Object reply = out.getStatus();
        if(reply instanceof VersionId) {
            return new Path(target.getParent(), target.getName(), target.getType(),
                target.attributes().withVersionId(((VersionId) reply).id));
        }
        return target;
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        switch(from.getHost().getProtocol().getType()) {
            case ftp:
            case irods:
                // Stateful
                return !Objects.equals(from, to);
        }
        return true;
    }

    @Override
    public DefaultCopyFeature withTarget(final Session<?> session) {
        to = session;
        return this;
    }
}
