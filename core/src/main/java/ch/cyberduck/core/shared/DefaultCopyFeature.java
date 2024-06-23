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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Objects;

public class DefaultCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(DefaultCopyFeature.class);

    private Session<?> from;
    private Session<?> to;

    public DefaultCopyFeature(final Session<?> from) {
        this.from = from;
        this.to = from;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        InputStream in;
        StatusOutputStream out;
        in = from.getFeature(Read.class).read(source, new TransferStatus(status), callback);
        Write writer = to.getFeature(MultipartWrite.class);
        if(null == writer) {
            // Fallback if multipart write is not available
            writer = to.getFeature(Write.class);
        }
        out = writer.write(target, status, callback);
        new StreamCopier(status, status).withListener(listener).transfer(in, out);
        if(!PathAttributes.EMPTY.equals(status.getResponse())) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received reply %s for creating file %s", status.getResponse(), target));
            }
            return new Path(target).withAttributes(status.getResponse());
        }
        log.warn(String.format("Missing status from writer %s", writer));
        return target;
    }

    @Override
    public void preflight(final Path source, final Path directory, final String filename) throws BackgroundException {
        switch(from.getHost().getProtocol().getType()) {
            case ftp:
            case irods:
                // Stateful
                if(Objects.equals(from, to)) {
                    throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(source);
                }
        }
    }

    @Override
    public DefaultCopyFeature withTarget(final Session<?> session) {
        to = session;
        return this;
    }
}
