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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.DiskShare;

public class SMBDeleteFeature implements Delete {

    private final SMBSession session;

    public SMBDeleteFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            callback.delete(file);
            try (final DiskShare share = session.openShare(file)) {
                if(file.isFile() || file.isSymbolicLink()) {
                    share.rm(new SMBPathContainerService(session).getKey(file));
                }
                else if(file.isDirectory()) {
                    share.rmdir(new SMBPathContainerService(session).getKey(file), true);
                }
            }
            catch(SMBRuntimeException e) {
                throw new SMBExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new SMBTransportExceptionMappingService().map("Cannot read container configuration", e);
            }
            finally {
                session.releaseShare(file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
