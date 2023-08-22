package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.DiskShare;

public class SMBDirectoryFeature implements Directory<Integer> {

    private final SMBSession session;

    public SMBDirectoryFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try (final DiskShare share = session.openShare(folder)) {
            share.mkdir(new SMBPathContainerService(session).getKey(folder));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read container configuration", e);
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
        finally {
            session.releaseShare(folder);
        }
        return folder;
    }

    @Override
    public SMBDirectoryFeature withWriter(final Write<Integer> writer) {
        return this;
    }
}
