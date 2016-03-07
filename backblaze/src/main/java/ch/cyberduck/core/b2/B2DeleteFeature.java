package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import java.util.List;

import synapticloop.b2.exception.B2Exception;

public class B2DeleteFeature implements Delete {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2DeleteFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            try {
                if(containerService.isContainer(file)) {
                    continue;
                }
                callback.delete(file);
                session.getClient().deleteFileVersion(containerService.getKey(file),
                        new B2FileidProvider(session).getFileid(file));
            }
            catch(B2Exception e) {
                throw new B2ExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
        for(Path file : files) {
            try {
                if(containerService.isContainer(file)) {
                    callback.delete(file);
                    // Finally delete bucket itself
                    session.getClient().deleteBucket(new B2FileidProvider(session).getFileid(file));
                }
            }
            catch(B2Exception e) {
                throw new B2ExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }
}

