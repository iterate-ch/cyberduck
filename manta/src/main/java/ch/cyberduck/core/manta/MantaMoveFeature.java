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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import com.joyent.manta.exception.MantaException;

public class MantaMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(MantaMoveFeature.class);

    private final MantaSession session;
    private Delete delete;

    private final PathContainerService containerService
            = new PathContainerService();

    public MantaMoveFeature(MantaSession session) {
        this.session = session;
        this.delete = new MantaDeleteFeature(session);
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            if(exists) {
                MantaSession.log.error("deleting target during move: " + renamed);
                delete.delete(Collections.singletonList(renamed), new DisabledLoginCallback(), callback);
            }

            session.getClient().move(session.requestPath(file), session.requestPath(renamed));
        }
        catch(MantaException e) {
            throw new MantaExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return session.isUserWritable(target);
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }
}
