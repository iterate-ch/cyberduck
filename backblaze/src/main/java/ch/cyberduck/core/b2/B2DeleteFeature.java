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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import synapticloop.b2.exception.B2ApiException;

public class B2DeleteFeature implements Delete {
    private static final Logger log = LogManager.getLogger(B2DeleteFeature.class);

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2DeleteFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            if(containerService.isContainer(file)) {
                continue;
            }
            callback.delete(file);
            if(file.getType().contains(Path.Type.upload)) {
                new B2LargeUploadPartService(session, fileid).delete(file.attributes().getVersionId());
            }
            else {
                if(file.isDirectory()) {
                    // Delete /.bzEmpty if any
                    final String placeholder;
                    try {
                        placeholder = fileid.getVersionId(file);
                    }
                    catch(NotfoundException e) {
                        log.warn(String.format("Ignore failure %s deleting placeholder file for %s", e, file));
                        continue;
                    }
                    try {
                        session.getClient().deleteFileVersion(containerService.getKey(file), placeholder);
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
                        if(null == file.attributes().getVersionId()) {
                            // Add hide marker
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Add hide marker %s of %s", file.attributes().getVersionId(), file));
                            }
                            session.getClient().hideFile(fileid.getVersionId(containerService.getContainer(file)), containerService.getKey(file));
                        }
                        else {
                            // Delete specific version
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Delete version %s of %s", file.attributes().getVersionId(), file));
                            }
                            session.getClient().deleteFileVersion(containerService.getKey(file), file.attributes().getVersionId());
                        }
                    }
                    catch(B2ApiException e) {
                        throw new B2ExceptionMappingService(fileid).map("Cannot delete {0}", e, file);
                    }
                    catch(IOException e) {
                        throw new DefaultIOExceptionMappingService().map(e);
                    }
                }
                fileid.cache(file, null);
            }
        }
        for(Path file : files.keySet()) {
            try {
                if(containerService.isContainer(file)) {
                    callback.delete(file);
                    // Finally delete bucket itself
                    session.getClient().deleteBucket(fileid.getVersionId(file));
                }
            }
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService(fileid).map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
    }
}
