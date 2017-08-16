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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

import java.io.IOException;
import java.io.InputStream;

import com.joyent.manta.client.MantaObject;
import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaException;
import com.joyent.manta.http.MantaHttpHeaders;

public class MantaReadFeature implements Read {

    private final MantaSession session;

    public MantaReadFeature(final MantaSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback connectionCallback) throws BackgroundException {
        final MantaHttpHeaders headers = new MantaHttpHeaders();
        try {
            try {
                if(status.isAppend()) {
                    final HttpRange range = HttpRange.withStatus(status);
                    headers.setByteRange(range.getStart(), range.getEnd() < 0 ? null : range.getEnd());
                }
                // Requesting an empty file as an InputStream doesn't work, but we also don't want to
                // perform a HEAD request for every read so we'll opt to handle the exception instead
                // see https://github.com/joyent/java-manta/issues/248
                return session.getClient().getAsInputStream(file.getAbsolute(), headers);
            }
            catch(UnsupportedOperationException e) {
                final MantaObject probablyEmptyFile = session.getClient().head(file.getAbsolute());
                if(probablyEmptyFile.getContentLength() != 0) {
                    throw new AccessDeniedException();
                }
                return new NullInputStream(0L);
            }
        }
        catch(MantaException e) {
            throw new MantaExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(MantaClientHttpResponseException e) {
            throw new MantaHttpExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return true;
    }
}
