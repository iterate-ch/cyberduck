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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;
import java.io.OutputStream;

public class DefaultCopyFeature implements Copy {

    private Session<?> from;
    private Session<?> to;

    public DefaultCopyFeature(final Session<?> from) {
        this.from = from;
        this.to = from;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        if(source.isDirectory()) {
            if(!to.getFeature(Find.class).find(target)) {
                to.getFeature(Directory.class).mkdir(target, null, new TransferStatus().length(0L));
            }
        }
        else {
            if(!to.getFeature(Find.class).find(target.getParent())) {
                this.copy(source.getParent(), target.getParent(), new TransferStatus().length(source.getParent().attributes().getSize()));
            }
            InputStream in = null;
            OutputStream out = null;
            in = new ThrottledInputStream(from.getFeature(Read.class).read(source, new TransferStatus(status), new DisabledConnectionCallback(), new DisabledPasswordCallback()), new BandwidthThrottle(BandwidthThrottle.UNLIMITED));
            Write write = to.getFeature(MultipartWrite.class);
            if(null == write) {
                // Fallback if multipart write is not available
                write = to.getFeature(Write.class);
            }
            out = new ThrottledOutputStream(write.write(target, status, new DisabledConnectionCallback()), new BandwidthThrottle(BandwidthThrottle.UNLIMITED));
            new StreamCopier(status, status).transfer(in, out);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return true;
    }

    @Override
    public DefaultCopyFeature withTarget(final Session<?> session) {
        to = session;
        return this;
    }
}
