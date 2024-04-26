package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftWriteFeature extends AbstractHttpWriteFeature<StorageObject> implements Write<StorageObject> {
    private static final Logger log = LogManager.getLogger(SwiftSession.class);

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    public SwiftWriteFeature(final SwiftSession session, final SwiftRegionService regionService) {
        super(new SwiftAttributesFinderFeature(session, regionService));
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public HttpResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        // Submit store run to background thread
        final DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>(file) {
            /**
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public StorageObject call(final HttpEntity entity) throws BackgroundException {
                try {
                    // Previous
                    final HashMap<String, String> headers = new HashMap<>(status.getMetadata());
                    if(status.isExists()) {
                        // Remove any large object header read from metadata of existing file
                        headers.remove(Constants.X_STATIC_LARGE_OBJECT);
                    }
                    final Checksum checksum = status.getChecksum();
                    final String etag = session.getClient().storeObject(
                            regionService.lookup(file),
                            containerService.getContainer(file).getName(), containerService.getKey(file),
                            entity, headers, checksum.algorithm == HashAlgorithm.md5 ? checksum.hash : null);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Saved object %s with checksum %s", file, etag));
                    }
                    final StorageObject stored = new StorageObject(containerService.getKey(file));
                    stored.setMd5sum(etag);
                    stored.setSize(status.getLength());
                    return stored;
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }
}
