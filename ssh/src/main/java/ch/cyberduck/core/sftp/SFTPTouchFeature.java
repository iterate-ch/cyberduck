package ch.cyberduck.core.sftp;

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
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.EnumSet;

import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;

public class SFTPTouchFeature implements Touch {

    private final SFTPSession session;

    public SFTPTouchFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file, final TransferStatus transferStatus) throws BackgroundException {
        if(file.isFile()) {
            try {
                final RemoteFile handle = session.sftp().open(file.getAbsolute(), EnumSet.of(OpenMode.CREAT, OpenMode.TRUNC));
                handle.close();
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map("Cannot create file {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }
}
