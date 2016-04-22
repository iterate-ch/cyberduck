package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import synapticloop.b2.exception.B2ApiException;

public class B2ReadFeature implements Read {
    private static final Logger log = Logger.getLogger(B2ReadFeature.class);

    private final B2Session session;

    public B2ReadFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                return session.getClient().downloadFileRangeByIdToStream(
                        new B2FileidProvider(session).getFileid(file),
                        range.getStart(), range.getEnd()
                );
            }
            return session.getClient().downloadFileByIdToStream(new B2FileidProvider(session).getFileid(file));
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return true;
    }
}
