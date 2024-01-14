package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import net.schmizz.sshj.sftp.PacketType;
import net.schmizz.sshj.sftp.Request;

public class SFTPCopyFeature implements Copy {

    private final SFTPSession session;

    public SFTPCopyFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback prompt, final StreamListener listener) throws BackgroundException {
        try {
            final Request request = session.sftp().newExtendedRequest("copy-file")
                    .putString(source.getAbsolute())
                    .putString(target.getAbsolute())
                    .putBoolean(status.isExists());
            session.sftp().request(request).retrieve().ensurePacketTypeIs(PacketType.STATUS);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        return null;
    }
}
