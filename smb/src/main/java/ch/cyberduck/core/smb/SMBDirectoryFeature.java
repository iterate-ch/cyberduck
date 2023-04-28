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


import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

public class SMBDirectoryFeature implements Directory<Integer> {

    private final SMBSession session;
    
    public SMBDirectoryFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(Path folder, TransferStatus status) throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mkdir'");
    }

    @Override
    public Directory<Integer> withWriter(Write<Integer> writer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withWriter'");
    }
    
    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return false;
    }

}
