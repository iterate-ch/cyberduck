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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import java.io.IOException;

import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.DiskShare;

public class SMBFindFeature implements Find {

    private final SMBSession session;

    public SMBFindFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            try (final DiskShare share = session.openShare(file)) {
                if(new SMBPathContainerService(session).isContainer(file)) {
                    return true;
                }
                if(file.isDirectory()) {
                    return share.folderExists(new SMBPathContainerService(session).getKey(file));
                }
                return share.fileExists(new SMBPathContainerService(session).getKey(file));
            }
            catch(IOException e) {
                throw new SMBTransportExceptionMappingService().map("Cannot read container configuration", e);
            }
            catch(SMBRuntimeException e) {
                throw new SMBExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            finally {
                session.releaseShare(file);
            }
        }
        catch(NotfoundException e) {
            return false;
        }
    }
}
