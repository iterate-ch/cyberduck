package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.CancellingListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;

import java.util.Collections;

import static ch.cyberduck.core.s3.S3VersionedObjectListService.KEY_DELETE_MARKER;

public class S3AttributesFinderFeature implements AttributesFinder {
    private static final Logger log = LogManager.getLogger(S3AttributesFinderFeature.class);

    private final S3Session session;
    private final S3AccessControlListFeature acl;
    private final PathContainerService containerService;

    public S3AttributesFinderFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.acl = acl;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        if(containerService.isContainer(file)) {
            final PathAttributes attributes = new PathAttributes();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Read location for bucket %s", file));
            }
            attributes.setRegion(new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getLocation(file).getIdentifier());
            return attributes;
        }
        if(file.getType().contains(Path.Type.upload)) {
            final Write.Append append = new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl).append(file, new TransferStatus());
            if(append.append) {
                return new PathAttributes().withSize(append.offset);
            }
            throw new NotfoundException(file.getAbsolute());
        }
        try {
            PathAttributes attr;
            final Path bucket = containerService.getContainer(file);
            try {
                attr = new S3AttributesAdapter().toAttributes(session.getClient().getVersionedObjectDetails(
                        file.attributes().getVersionId(), bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(file)));
            }
            catch(ServiceException e) {
                switch(e.getResponseCode()) {
                    case 405:
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Mark file %s as delete marker", file));
                        }
                        // Only DELETE method is allowed for delete markers
                        attr = new PathAttributes();
                        attr.setCustom(Collections.singletonMap(KEY_DELETE_MARKER, Boolean.TRUE.toString()));
                        attr.setDuplicate(true);
                        return attr;
                }
                throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            if(StringUtils.isNotBlank(attr.getVersionId())) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Determine if %s is latest version for %s", attr.getVersionId(), file));
                }
                // Determine if latest version
                try {
                    final String latest = new S3AttributesAdapter().toAttributes(session.getClient().getObjectDetails(
                            bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(file))).getVersionId();
                    if(null != latest) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Found later version %s for %s", latest, file));
                        }
                        // Duplicate if not latest version
                        attr.setDuplicate(!latest.equals(attr.getVersionId()));
                    }
                }
                catch(ServiceException e) {
                    final BackgroundException failure = new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                    if(failure instanceof NotfoundException) {
                        attr.setDuplicate(true);
                    }
                    else {
                        throw failure;
                    }
                }
            }
            return attr;
        }
        catch(NotfoundException e) {
            if(file.isDirectory()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Search for common prefix %s", file));
                }
                // File may be marked as placeholder but no placeholder file exists. Check for common prefix returned.
                try {
                    new S3ObjectListService(session, acl).list(file, new CancellingListProgressListener(), String.valueOf(Path.DELIMITER), 1);
                }
                catch(ListCanceledException l) {
                    // Found common prefix
                    return PathAttributes.EMPTY;
                }
                catch(NotfoundException n) {
                    throw e;
                }
                // Found common prefix
                return PathAttributes.EMPTY;
            }
            throw e;
        }
    }
}
