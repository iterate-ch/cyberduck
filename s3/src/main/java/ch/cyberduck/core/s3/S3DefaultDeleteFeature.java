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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartUpload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class S3DefaultDeleteFeature implements Delete {
    private static final Logger log = LogManager.getLogger(S3DefaultDeleteFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3MultipartService multipartService;
    private final S3VersioningFeature versioningService;

    public S3DefaultDeleteFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this(session, new S3DefaultMultipartService(session), new S3VersioningFeature(session, acl));
    }

    public S3DefaultDeleteFeature(final S3Session session, final S3MultipartService multipartService, final S3VersioningFeature versioningService) {
        this.session = session;
        this.multipartService = multipartService;
        this.versioningService = versioningService;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> containers = new ArrayList<>();
        for(Path file : files.keySet()) {
            if(containerService.isContainer(file)) {
                containers.add(file);
            }
            else {
                callback.delete(file);
                final Path bucket = containerService.getContainer(file);
                if(file.getType().contains(Path.Type.upload)) {
                    // In-progress multipart upload
                    try {
                        multipartService.delete(new MultipartUpload(file.attributes().getVersionId(),
                                bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(file)));
                    }
                    catch(NotfoundException ignored) {
                        log.warn(String.format("Ignore failure deleting multipart upload %s", file));
                    }
                }
                else {
                    try {
                        if(versioningService.getConfiguration(bucket).isMultifactor()) {
                            final Credentials mfa = versioningService.getToken(prompt);
                            final String multiFactorSerialNumber = mfa.getUsername();
                            final String multiFactorAuthCode = mfa.getPassword();
                            session.getClient().deleteVersionedObjectWithMFA(file.attributes().getVersionId(),
                                    multiFactorSerialNumber, multiFactorAuthCode,
                                    bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(file));
                        }
                        else {
                            // Always returning 204 even if the key does not exist. Does not return 404 for non-existing keys
                            session.getClient().deleteVersionedObject(
                                    file.attributes().getVersionId(), bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(file));
                        }
                    }
                    catch(ServiceException e) {
                        throw new S3ExceptionMappingService().map("Cannot delete {0}", e, file);
                    }
                }
            }
        }
        for(Path file : containers) {
            callback.delete(file);
            try {
                final String bucket = containerService.getContainer(file).getName();
                session.getClient().deleteBucket(bucket);
                session.getClient().getRegionEndpointCache().removeRegionForBucketName(bucket);
            }
            catch(ServiceException e) {
                throw new S3ExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }
}
