package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.utils.ServiceUtils;

import java.text.MessageFormat;
import java.util.EnumSet;

public class S3DirectoryFeature implements Directory<StorageObject> {

    private static final String MIMETYPE = "application/x-directory";

    private final S3Session session;
    private final S3AccessControlListFeature acl;
    private final PathContainerService containerService;

    public S3DirectoryFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.acl = acl;
    }

    @Override
    public Path mkdir(final Write<StorageObject> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(folder)) {
            final S3BucketCreateService service = new S3BucketCreateService(session);
            service.create(folder, StringUtils.isBlank(status.getRegion()) ?
                    new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getDefault(folder).getIdentifier() : status.getRegion());
            return folder;
        }
        else {
            final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
            type.add(Path.Type.placeholder);
            return new S3TouchFeature(session, acl).touch(writer, folder
                    .withType(type), status
                    // Add placeholder object
                    .setMime(MIMETYPE)
                    .setChecksum(writer.checksum(folder, status).compute(new NullInputStream(0L), status)));
        }
    }


    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(StringUtils.isEmpty(RequestEntityRestStorageService.findBucketInHostname(session.getHost()))) {
            if(workdir.isRoot()) {
                if(StringUtils.isNotBlank(filename)) {
                    if(!ServiceUtils.isBucketNameValidDNSName(filename)) {
                        throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
                    }
                }
            }
        }
    }

}
