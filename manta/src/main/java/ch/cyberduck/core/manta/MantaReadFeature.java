package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import com.joyent.manta.client.MantaObject;

public class MantaReadFeature implements Read {
    private static final Logger log = Logger.getLogger(MantaReadFeature.class);

    private final MantaSession session;

    public MantaReadFeature(final MantaSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String remotePath = session.pathMapper.requestPath(file);

        try {
            // requesting an empty file as an InputStream doesn't work, but we also don't want to
            // perform a HEAD request for every read so we'll opt to handle the exception
            return session.getClient().getAsInputStream(remotePath);
        }
        catch(UnsupportedOperationException e) {
            return emptyFileFallback(remotePath);
        }
        catch(IOException e) {
            throw session.exceptionMapper.map(e);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return true;
    }

    private InputStream emptyFileFallback(final String remotePath) throws BackgroundException {
        final MantaObject probablyEmptyFile;
        try {
            probablyEmptyFile = session.getClient().head(remotePath);
        }
        catch(IOException e) {
            throw session.exceptionMapper.map("Cannot read file {0}", e);
        }

        if(probablyEmptyFile.getContentLength() != 0) {
            throw session.exceptionMapper.map(
                    "Cannot read file {0}",
                    new RuntimeException("Empty file was not actually empty. Concurrent Access detected."));
        }

        return new NullInputStream(0);
    }
}
