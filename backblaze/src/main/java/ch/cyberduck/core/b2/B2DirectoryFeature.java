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
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

import java.io.IOException;

import synapticloop.b2.BucketType;
import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.BaseB2Response;

public class B2DirectoryFeature implements Directory<BaseB2Response> {

    protected static final String MIMETYPE = "application/octet-stream";
    protected static final String PLACEHOLDER = "/.bzEmpty";

    private final PathContainerService containerService
            = new PathContainerService();

    private final B2Session session;
    private Write<BaseB2Response> writer;

    public B2DirectoryFeature(final B2Session session) {
        this(session, new B2WriteFeature(session));
    }

    public B2DirectoryFeature(final B2Session session, final B2WriteFeature writer) {
        this.session = session;
        this.writer = writer;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, new TransferStatus());
    }

    @Override
    public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final B2BucketResponse response = session.getClient().createBucket(containerService.getContainer(file).getName(),
                        null == region ? BucketType.valueOf(PreferencesFactory.get().getProperty("b2.bucket.acl.default")) : BucketType.valueOf(region));
                switch(response.getBucketType()) {
                    case allPublic:
                        file.attributes().setAcl(new Acl(new Acl.GroupUser(Acl.GroupUser.EVERYONE, false), new Acl.Role(Acl.Role.READ)));
                }
            }
            else {
                status.setChecksum(writer.checksum().compute(new NullInputStream(0L), status.length(0L)));
                status.setMime(MIMETYPE);
                new DefaultStreamCloser().close(writer.write(file, status));
            }
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public B2DirectoryFeature withWriter(final Write<BaseB2Response> writer) {
        this.writer = writer;
        return this;
    }
}

