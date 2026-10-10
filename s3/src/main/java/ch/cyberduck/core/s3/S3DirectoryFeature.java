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
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.utils.ServiceUtils;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Optional;

public class S3DirectoryFeature implements Directory<StorageObject> {
    private static final Logger log = LogManager.getLogger(S3DirectoryFeature.class);

    private static final String MIMETYPE = "application/x-directory";

    private final S3Session session;
    private final PathContainerService containerService;

    public S3DirectoryFeature(final S3Session session) {
        this.session = session;
        this.containerService = new S3PathContainerService(session.getHost());
    }

    @Override
    public Path mkdir(final Write<StorageObject> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(folder)) {
            final String region = StringUtils.isBlank(status.getRegion()) ?
                    new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getDefault(folder).getIdentifier() : status.getRegion();
            log.debug("Create bucket {} in region {}", folder, region);
            if(!HostPreferencesFactory.get(session.getHost()).getBoolean("s3.bucket.virtualhost.disable")) {
                if(!ServiceUtils.isBucketNameValidDNSName(folder.getName())) {
                    throw new InteroperabilityException(LocaleFactory.localizedString("Bucket name is not DNS compatible", "S3"));
                }
            }
            AccessControlList acl;
            if(HostPreferencesFactory.get(session.getHost()).getProperty("s3.acl.default").equals("public-read")) {
                acl = AccessControlList.REST_CANNED_PUBLIC_READ;
            }
            else {
                acl = AccessControlList.REST_CANNED_PRIVATE;
            }
            try {
                if(StringUtils.isNotBlank(region)) {
                    if(S3Session.isAwsHostname(session.getHost().getHostname())) {
                        // Adjust default region to be used when searching for existing bucket will return 404
                        HostPreferencesFactory.get(session.getHost()).setProperty("s3.location", region);
                    }
                }
                else {
                    log.warn("Missing region for bucket location");
                }
                // Create bucket
                session.getClient().createBucket(URIEncoder.encode(containerService.getContainer(folder).getName()),
                        S3LocationFeature.DEFAULT_REGION.getIdentifier().equals(region) ? "US" : region, acl);
            }
            catch(ServiceException e) {
                throw new S3ExceptionMappingService().map("Cannot create folder {0}", e, folder);
            }
            return folder;
        }
        else {
            final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
            type.add(Path.Type.placeholder);
            return new S3TouchFeature(session).touch(writer, folder
                    .withType(type), status
                    // Add placeholder object
                    .setMime(MIMETYPE)
                    .setChecksum(writer.checksum(folder, status).compute(new NullInputStream(0L), status)));
        }
    }


    @Override
    public void preflight(final Path workdir, final Optional<String> filename) throws BackgroundException {
        if(StringUtils.isEmpty(RequestEntityRestStorageService.findBucketInHostname(session.getHost()))) {
            if(workdir.isRoot()) {
                if(filename.isPresent()) {
                    if(!ServiceUtils.isBucketNameValidDNSName(filename.get())) {
                        throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
                    }
                }
            }
        }
    }

}
