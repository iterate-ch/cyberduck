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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

import java.io.IOException;

import synapticloop.b2.BucketType;
import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;

public class B2DirectoryFeature implements Directory {

    protected static final String MIMETYPE = "application/octet-stream";
    protected static final String PLACEHOLDER = "/.bzEmpty";

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;
    private final Write write;

    public B2DirectoryFeature(final B2Session session) {
        this(session, session.getFeature(Write.class, new B2WriteFeature(session)));
    }

    public B2DirectoryFeature(final B2Session session, final Write write) {
        this.session = session;
        this.write = write;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, new TransferStatus());
    }

    @Override
    public void mkdir(final Path file, final String type, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final B2BucketResponse response = session.getClient().createBucket(containerService.getContainer(file).getName(),
                        null == type ? BucketType.valueOf(PreferencesFactory.get().getProperty("b2.bucket.acl.default")) : BucketType.valueOf(type));
                switch(response.getBucketType()) {
                    case allPublic:
                        file.attributes().setAcl(new Acl(new Acl.GroupUser(Acl.GroupUser.EVERYONE, false), new Acl.Role(Acl.Role.READ)));
                }
            }
            else {
                status.setChecksum(session.getFeature(ChecksumCompute.class, ChecksumComputeFactory.get(HashAlgorithm.sha1)).compute(new NullInputStream(0L), status.length(0L)));
                status.setMime(MIMETYPE);
                new DefaultStreamCloser().close(write.write(file, status));
            }
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}

