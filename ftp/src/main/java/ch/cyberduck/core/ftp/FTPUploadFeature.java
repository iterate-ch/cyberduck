package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.net.ftp.FTP;

public class FTPUploadFeature extends DefaultUploadFeature<Void> {

    private final Host host;

    public FTPUploadFeature(final Session<?> session) {
        super(session);
        this.host = session.getHost();
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(FTP.ASCII_FILE_TYPE == FTPFileType.of(host, file)) {
            // The size on the server after converting line breaks is not the number of bytes read from the local file
            return new Write.Append(false).withStatus(status);
        }
        return new Write.Append(status.isExists()).withStatus(status);
    }
}
