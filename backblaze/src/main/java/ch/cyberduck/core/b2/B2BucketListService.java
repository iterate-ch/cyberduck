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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.EnumSet;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;

public class B2BucketListService implements RootListService {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2BucketListService(final B2Session session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> buckets = new AttributedList<Path>();
            for(B2BucketResponse bucket : session.getClient().listBuckets()) {
                final PathAttributes attributes = new PathAttributes();
                attributes.setVersionId(bucket.getBucketId());
                switch(bucket.getBucketType()) {
                    case allPublic:
                        attributes.setAcl(new Acl(new Acl.GroupUser(Acl.GroupUser.EVERYONE, false), new Acl.Role(Acl.Role.READ)));
                        break;
                }
                buckets.add(new Path(bucket.getBucketName(), EnumSet.of(Path.Type.directory, Path.Type.volume), attributes));
            }
            listener.chunk(directory, buckets);
            return buckets;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
