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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;
import java.io.OutputStream;

public class DefaultCopyFeature implements Copy {

    private final Session<?> session;

    public DefaultCopyFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        if(source.isDirectory()) {
            if(!session.getFeature(Find.class).find(target)) {
                session.getFeature(Directory.class).mkdir(target, null, status);
            }
        }
        else {
            if(!session.getFeature(Find.class).find(target.getParent())) {
                this.copy(source.getParent(), target.getParent(), new TransferStatus());
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new ThrottledInputStream(session.getFeature(Read.class).read(source, new TransferStatus(), new DisabledConnectionCallback()), new BandwidthThrottle(BandwidthThrottle.UNLIMITED));
                out = new ThrottledOutputStream(session.getFeature(MultipartWrite.class).write(target, status, new DisabledConnectionCallback()), new BandwidthThrottle(BandwidthThrottle.UNLIMITED));
                final TransferStatus progress = new TransferStatus();
                new StreamCopier(progress, progress).transfer(in, out);
            }
            finally {
                new DefaultStreamCloser().close(in);
                new DefaultStreamCloser().close(out);
            }
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }
}
