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
import ch.cyberduck.core.collections.Partition;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.MultipleDeleteResult;
import org.jets3t.service.model.container.ObjectKeyAndVersion;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3MultipleDeleteFeature implements Delete {
    private static final Logger log = LogManager.getLogger(S3MultipleDeleteFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3MultipartService multipartService;
    private final S3VersioningFeature versioningService;

    public S3MultipleDeleteFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this(session, new S3DefaultMultipartService(session), new S3VersioningFeature(session, acl));
    }

    public S3MultipleDeleteFeature(final S3Session session, final S3MultipartService multipartService, final S3VersioningFeature versioningService) {
        this.session = session;
        this.multipartService = multipartService;
        this.versioningService = versioningService;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final Map<Path, List<ObjectKeyAndVersion>> map = new HashMap<>();
        final List<Path> containers = new ArrayList<>();
        for(Path file : files.keySet()) {
            if(containerService.isContainer(file)) {
                containers.add(file);
                continue;
            }
            callback.delete(file);
            final Path bucket = containerService.getContainer(file);
            if(file.getType().contains(Path.Type.upload)) {
                // In-progress multipart upload
                try {
                    multipartService.delete(new MultipartUpload(file.attributes().getVersionId(),
                            bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(file)));
                }
                catch(NotfoundException ignored) {
                    log.warn("Ignore failure deleting multipart upload {}", file);
                }
            }
            else {
                final List<ObjectKeyAndVersion> keys = new ArrayList<>();
                // Always returning 204 even if the key does not exist. Does not return 404 for non-existing keys
                keys.add(new ObjectKeyAndVersion(containerService.getKey(file), file.attributes().getVersionId()));
                if(map.containsKey(bucket)) {
                    map.get(bucket).addAll(keys);
                }
                else {
                    map.put(bucket, keys);
                }
            }
        }
        // Iterate over all containers and delete list of keys
        for(Map.Entry<Path, List<ObjectKeyAndVersion>> entry : map.entrySet()) {
            final Path container = entry.getKey();
            final List<ObjectKeyAndVersion> keys = entry.getValue();
            this.delete(container, keys, prompt);
        }
        for(Path file : containers) {
            callback.delete(file);
            // Finally delete bucket itself
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

    /**
     * @param bucket Bucket
     * @param keys   Key and version ID for versioned object or null
     * @param prompt Password input
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException Authentication canceled for MFA delete
     */
    protected void delete(final Path bucket, final List<ObjectKeyAndVersion> keys, final PasswordCallback prompt)
            throws BackgroundException {
        try {
            if(versioningService.getConfiguration(bucket).isMultifactor()) {
                final Credentials mfa = versioningService.getToken(prompt);
                final String multiFactorSerialNumber = mfa.getUsername();
                final String multiFactorAuthCode = mfa.getPassword();
                final MultipleDeleteResult result = session.getClient().deleteMultipleObjectsWithMFA(bucket.getName(),
                        keys.toArray(new ObjectKeyAndVersion[keys.size()]),
                        mfa.getUsername(),
                        mfa.getPassword(),
                        // Only include errors in response
                        true);
                if(result.hasErrors()) {
                    for(MultipleDeleteResult.ErrorResult error : result.getErrorResults()) {
                        if(StringUtils.equals("ObjectNotFound", error.getErrorCode())) {
                            // Ignore failure deleting placeholder
                            continue;
                        }
                        final ServiceException failure = new ServiceException();
                        failure.setErrorCode(error.getErrorCode());
                        failure.setErrorMessage(error.getMessage());
                        throw new S3ExceptionMappingService().map("Cannot delete {0}", failure,
                                new Path(bucket, error.getKey(), EnumSet.of(Path.Type.file)));
                    }
                }
            }
            else {
                // Request contains a list of up to 1000 keys that you want to delete
                for(List<ObjectKeyAndVersion> partition : new Partition<>(keys,
                    new HostPreferences(session.getHost()).getInteger("s3.delete.multiple.partition"))) {
                    final MultipleDeleteResult result = session.getClient().deleteMultipleObjects(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(),
                            partition.toArray(new ObjectKeyAndVersion[partition.size()]),
                            // Only include errors in response
                            true);
                    if(result.hasErrors()) {
                        for(MultipleDeleteResult.ErrorResult error : result.getErrorResults()) {
                            if(StringUtils.equals("ObjectNotFound", error.getErrorCode())) {
                                // Ignore failure deleting placeholder
                                continue;
                            }
                            final ServiceException failure = new ServiceException();
                            failure.setErrorCode(error.getErrorCode());
                            failure.setErrorMessage(error.getMessage());
                            throw new S3ExceptionMappingService().map("Cannot delete {0}", failure,
                                    new Path(bucket, error.getKey(), EnumSet.of(Path.Type.file)));
                        }
                    }
                }
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot delete {0}", e, bucket);
        }
    }
}
