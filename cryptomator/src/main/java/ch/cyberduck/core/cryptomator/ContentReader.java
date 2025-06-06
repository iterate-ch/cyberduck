package ch.cyberduck.core.cryptomator;

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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ContentReader {

    private final Session<?> session;

    public ContentReader(final Session<?> session) {
        this.session = session;
    }

    public String read(final Path file) throws BackgroundException {
        final Read read = session._getFeature(Read.class);
        try(final InputStream in = read.read(file, new TransferStatus().setLength(file.attributes().getSize()), new DisabledConnectionCallback())) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public Reader getReader(final Path file) throws BackgroundException {
        final Read read = session._getFeature(Read.class);
        return new InputStreamReader(read.read(file, new TransferStatus().setLength(file.attributes().getSize()), new DisabledConnectionCallback()), StandardCharsets.UTF_8);
    }
}
