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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import synapticloop.b2.exception.B2Exception;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2FileidProvider {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2FileidProvider(final B2Session session) {
        this.session = session;
    }

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
            else {
                final B2ListFilesResponse response = session.getClient().listFileNames(
                        containerService.getContainer(file).getName(), containerService.getKey(file), 1);
                if(1 == response.getFiles().size()) {
                    return response.getFiles().iterator().next().getFileId();
                }
                throw new NotfoundException(file.getAbsolute());
            }
        }
        catch(B2Exception e) {
            throw new B2ExceptionMappingService().map(e);
        }
    }
}
