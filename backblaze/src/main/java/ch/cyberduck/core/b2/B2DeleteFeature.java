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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;

public class B2DeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(B2DeleteFeature.class);

    private final PathContainerService containerService
            = new PathContainerService();

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
            if(file.isPlaceholder()) {
                continue;
            }
            if(file.getType().contains(Path.Type.upload)) {
                new B2LargeUploadPartService(session).delete(file.attributes().getVersionId());
            }
            callback.delete(file);
            if(file.isDirectory()) {
                // Delete /.bzEmpty if any
                final String fileid;
                try {
                    fileid = new B2FileidProvider(session).getFileid(new Path(containerService.getContainer(file),
                            String.format("%s%s", containerService.getKey(file), B2DirectoryFeature.PLACEHOLDER), EnumSet.of(Path.Type.file)));
                }
                catch(NotfoundException e) {
                    log.warn(String.format("Ignore failure %s deleting placeholder file for %s", e.getDetail(), file));
                    return;
                }
                try {
                    session.getClient().deleteFileVersion(String.format("%s%s", containerService.getKey(file), B2DirectoryFeature.PLACEHOLDER), fileid);
                }
                catch(B2ApiException e) {
                    log.warn(String.format("Ignore failure %s deleting placeholder file for %s", e.getMessage(), file));
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
            }
            else if(file.isFile()) {
                try {
                    session.getClient().deleteFileVersion(containerService.getKey(file), new B2FileidProvider(session).getFileid(file));
                }
                catch(B2ApiException e) {
                    throw new B2ExceptionMappingService(session).map("Cannot delete {0}", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
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
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService(session).map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }

    @Override
    public boolean isRecursive() {
        return false;
    }
}
