package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVReadFeature implements Read {
    private static final Logger log = Logger.getLogger(DAVReadFeature.class);

    private DAVSession session;

    public DAVReadFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        final List<Header> headers = new ArrayList<Header>();
        if(status.isAppend()) {
            headers.add(new BasicHeader(HttpHeaders.RANGE, String.format("bytes=%d-", status.getSkip())));
            // Disable compression
            headers.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
        }
        try {
            return session.getClient().get(
                    new DAVPathEncoder().encode(file), headers);
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean append(final Path file) {
        return true;
    }
}
