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

import java.security.MessageDigest;

public class DAVUploadFeature extends HttpUploadFeature<Void, MessageDigest> {

    public DAVUploadFeature(final DAVSession session) {

    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(status.getLength() == TransferStatus.UNKNOWN_LENGTH) {
            return new Write.Append(false).withStatus(status);
        }
        return new Write.Append(status.isExists()).withStatus(status);
    }
}
