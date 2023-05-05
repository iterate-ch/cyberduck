package ch.cyberduck.core.smb;

import java.util.Map;

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;

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

import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

public class SMBDeleteFeature implements Delete {

    private final SMBSession session;

    public SMBDeleteFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public void delete(Map<Path, TransferStatus> files, PasswordCallback prompt, Callback callback) {
        for (Path file : files.keySet()) {
            callback.delete(file);

            if (file.isFile() || file.isSymbolicLink()) {
                session.share.rm(file.getAbsolute());
            } else if (file.isDirectory()) {
                session.share.rmdir(file.getAbsolute(), true);
            }

        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }

}
