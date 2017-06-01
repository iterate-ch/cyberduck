package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import com.joyent.manta.exception.MantaException;
import com.joyent.manta.exception.MantaIOException;

public class MantaTouchFeature implements Touch {

    private final MantaSession session;

    public MantaTouchFeature(final MantaSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(!session.getClient().existsAndIsAccessible(file.getParent().getAbsolute())) {
                session.getClient().putDirectory(file.getParent().getAbsolute());
            }

            final String remotePath = session.pathMapper.requestPath(file);
            // put(String, byte[]) does not urlencode paths, manta bug forthcoming
            session.getClient().put(remotePath, "");
        }
        catch(MantaException | MantaIOException e) {
            throw session.exceptionMapper.map("Cannot create file {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create file {0}", e, file);
        }
        return file;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return session.pathMapper.isUserWritable(workdir);
    }

    @Override
    public Touch withWriter(final Write writer) {
        return this;
    }
}
