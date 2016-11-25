package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ContentReader {

    private final Session<?> session;

    public ContentReader(final Session<?> session) {
        this.session = session;
    }

    public String readToString(final Path file) throws BackgroundException {
        final Read read = session.getFeature(Read.class);
        final InputStream stream = read.read(file, new TransferStatus());
        try {
            return IOUtils.toString(stream, "UTF-8");
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public byte[] readToByteArray(final Path file) throws BackgroundException {
        final Read read = session.getFeature(Read.class);
        final InputStream stream = read.read(file, new TransferStatus());
        try {
            return IOUtils.toByteArray(stream);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
