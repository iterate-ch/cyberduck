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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.ThreadedDeleteFeature;

import java.io.IOException;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;

public class B2DeleteFeature extends ThreadedDeleteFeature implements Delete {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2DeleteFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            if(containerService.isContainer(file)) {
                continue;
            }
            if(file.getType().contains(Path.Type.upload)) {
                new B2LargeUploadPartService(session).delete(file.attributes().getVersionId());
            }
            this.submit(file, new Implementation() {
                @Override
                public void delete(final Path file) throws BackgroundException {
                    callback.delete(file);
                    try {
                        if(file.isPlaceholder()) {
                            session.getClient().deleteFileVersion(String.format("%s%s", containerService.getKey(file), B2DirectoryFeature.PLACEHOLDER),
                                    new B2FileidProvider(session).getFileid(file));
                        }
                        else if(file.isFile()) {
                            session.getClient().deleteFileVersion(containerService.getKey(file),
                                    new B2FileidProvider(session).getFileid(file));
                        }
                    }
                    catch(B2ApiException e) {
                        throw new B2ExceptionMappingService(session).map("Cannot delete {0}", e, file);
                    }
                    catch(IOException e) {
                        throw new DefaultIOExceptionMappingService().map(e);
                    }
                }
            });
        }
        this.await();
        for(Path file : files) {
            try {
                if(containerService.isContainer(file)) {
                    callback.delete(file);
                    // Finally delete bucket itself
                    session.getClient().deleteBucket(new B2FileidProvider(session).getFileid(file));
                }
            }
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService(session).map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
    }
}
