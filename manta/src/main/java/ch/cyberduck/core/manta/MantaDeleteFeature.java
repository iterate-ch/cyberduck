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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import java.io.IOException;
import java.util.List;

import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaException;

public class MantaDeleteFeature implements Delete {

    private final MantaSession session;

    public MantaDeleteFeature(MantaSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            callback.delete(file);
            try {
                session.getClient().deleteRecursive(file.getAbsolute());
            }
            catch(MantaException e) {
                throw new MantaExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(MantaClientHttpResponseException e) {
                throw new MantaHttpExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return session.isUserWritable(file);
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
