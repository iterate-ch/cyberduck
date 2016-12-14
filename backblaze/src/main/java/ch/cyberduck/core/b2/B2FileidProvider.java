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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2FileidProvider implements IdProvider {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2FileidProvider(final B2Session session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        try {
            if(containerService.isContainer(file)) {
                final List<B2BucketResponse> buckets = session.getClient().listBuckets();
                for(B2BucketResponse bucket : buckets) {
                    if(StringUtils.equals(containerService.getContainer(file).getName(), bucket.getBucketName())) {
                        return bucket.getBucketId();
                    }
                }
                throw new NotfoundException(file.getAbsolute());
            }
            else if(file.isPlaceholder()) {
                return null;
            }
            else {
                final B2ListFilesResponse response = session.getClient().listFileNames(
                        this.getFileid(containerService.getContainer(file)), containerService.getKey(file), 2);
                for(B2FileInfoResponse info : response.getFiles()) {
                    if(file.isFile()) {
                        if(StringUtils.equals(containerService.getKey(file), info.getFileName())) {
                            switch(info.getAction()) {
                                default:
                                    return info.getFileId();
                            }
                        }
                    }
                    else if(file.isPlaceholder()) {
                        if(StringUtils.endsWith(info.getFileName(), "/.bzEmpty")) {
                            if(StringUtils.equals(containerService.getKey(file), StringUtils.removeEnd(info.getFileName(), "/.bzEmpty"))) {
                                return info.getFileId();
                            }
                        }
                    }
                }
                throw new NotfoundException(file.getAbsolute());
            }
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
