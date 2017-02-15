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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;

public class B2FindFeature implements Find {

    private final PathContainerService containerService
            = new PathContainerService();

    private final B2Session session;

    public B2FindFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final List<B2BucketResponse> buckets = session.getClient().listBuckets();
                for(B2BucketResponse bucket : buckets) {
                    if(StringUtils.equals(containerService.getContainer(file).getName(), bucket.getBucketName())) {
                        return true;
                    }
                }
            }
            else {
                try {
                    return null != new B2FileidProvider(session).getFileid(file);
                }
                catch(NotfoundException e) {
                    return false;
                }
            }
            return false;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        return this;
    }
}
