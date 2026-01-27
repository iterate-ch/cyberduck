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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;

public class DAVUploadFeature extends HttpUploadFeature<Void, MessageDigest> {
    private static final Logger log = LogManager.getLogger(DAVUploadFeature.class);

    private final DAVSession.HttpCapabilities capabilities;

    public DAVUploadFeature(final DAVSession session) {
        this(session, new DAVSession.HttpCapabilities(session.getHost()));
    }

    public DAVUploadFeature(final DAVSession session, final DAVSession.HttpCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(status.getLength() == TransferStatus.UNKNOWN_LENGTH) {
            return new Write.Append(false).withStatus(status);
        }
        if(capabilities.iis) {
            return new Write.Append(false).withStatus(status);
        }
        final Write.Append append = new Write.Append(status.isExists()).withStatus(status);
        log.debug("Determined append {} for file {} with status {}", append, file, status);
        return append;
    }
}
