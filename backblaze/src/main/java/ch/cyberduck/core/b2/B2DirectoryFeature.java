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
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.EnumSet;

import synapticloop.b2.BucketType;
import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.BaseB2Response;

public class B2DirectoryFeature implements Directory<BaseB2Response> {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;
    private Write<BaseB2Response> writer;

    public B2DirectoryFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this(session, fileid, new B2WriteFeature(session, fileid));
    }

    public B2DirectoryFeature(final B2Session session, final B2VersionIdProvider fileid, final B2WriteFeature writer) {
        this.session = session;
        this.fileid = fileid;
        this.writer = writer;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                final B2BucketResponse response = session.getClient().createBucket(containerService.getContainer(folder).getName(),
                        null == status.getRegion() ? BucketType.valueOf(new B2BucketTypeFeature(session, fileid).getDefault().getIdentifier()) : BucketType.valueOf(status.getRegion()));
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.volume);
                return folder.withType(type).withAttributes(new B2AttributesFinderFeature(session, fileid).toAttributes(response));
            }
            else {
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.placeholder);
                return new B2TouchFeature(session, fileid).touch(folder.withType(type), status
                        .withMime(MimeTypeService.DEFAULT_CONTENT_TYPE)
                        .withChecksum(writer.checksum(folder, status).compute(new NullInputStream(0L), status)));
            }
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map("Cannot create folder {0}", e, folder);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        if(workdir.isRoot()) {
            // Empty argument if not known in validation
            if(StringUtils.isNotBlank(name)) {
                // Bucket names must be a minimum of 6 and a maximum of 50 characters long, and must be globally unique;
                // two different B2 accounts cannot have buckets with the name name. Bucket names can consist of: letters,
                // digits, and "-". Bucket names cannot start with "b2-"; these are reserved for internal Backblaze use.
                if(StringUtils.startsWith(name, "b2-")) {
                    return false;
                }
                if(StringUtils.length(name) > 50) {
                    return false;
                }
                if(StringUtils.length(name) < 6) {
                    return false;
                }
                return StringUtils.isAlphanumeric(RegExUtils.removeAll(name, "-"));
            }
        }
        return true;
    }

    @Override
    public B2DirectoryFeature withWriter(final Write<BaseB2Response> writer) {
        this.writer = writer;
        return this;
    }
}

