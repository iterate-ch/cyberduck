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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.MultipartUploadChunk;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class S3DefaultMultipartService implements S3MultipartService {
    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;

    private static final Logger log = LogManager.getLogger(S3DefaultMultipartService.class);

    private final S3Session session;
    private final PathContainerService containerService;

    public S3DefaultMultipartService(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public List<MultipartUpload> find(final Path file) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Finding multipart uploads for %s", file));
        }
        final List<MultipartUpload> uploads = new ArrayList<>();
        // This operation lists in-progress multipart uploads. An in-progress multipart upload is a
        // multipart upload that has been initiated, using the Initiate Multipart Upload request, but has
        // not yet been completed or aborted.
        String nextUploadIdMarker = null;
        String nextKeyMarker = null;
        boolean isTruncated;
        do {
            final Path bucket = containerService.getContainer(file);
            final MultipartUploadChunk chunk;
            try {
                chunk = session.getClient().multipartListUploadsChunked(
                        bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(file),
                        String.valueOf(Path.DELIMITER), nextKeyMarker, nextUploadIdMarker, null, false);
            }
            catch(S3ServiceException e) {
                final BackgroundException failure = new S3ExceptionMappingService().map("Upload {0} failed", e, file);
                if(failure instanceof NotfoundException) {
                    return Collections.emptyList();
                }
                if(failure instanceof InteroperabilityException) {
                    return Collections.emptyList();
                }
                throw failure;
            }
            uploads.addAll(Arrays.asList(chunk.getUploads()));
            if(log.isInfoEnabled()) {
                log.info(String.format("Found %d previous multipart uploads for %s", uploads.size(), file));
            }
            // Sort with newest upload first in list
            uploads.sort(new Comparator<MultipartUpload>() {
                @Override
                public int compare(final MultipartUpload o1, final MultipartUpload o2) {
                    return -o1.getInitiatedDate().compareTo(o2.getInitiatedDate());
                }
            });
            nextKeyMarker = chunk.getPriorLastKey();
            nextUploadIdMarker = chunk.getPriorLastIdMarker();
            isTruncated = !chunk.isListingComplete();
        }
        while(isTruncated && nextUploadIdMarker != null);
        if(log.isInfoEnabled()) {
            for(MultipartUpload upload : uploads) {
                log.info(String.format("Found multipart upload %s for %s", upload, file));
            }
        }
        return uploads;
    }

    @Override
    public List<MultipartPart> list(final MultipartUpload upload) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("List completed parts of %s", upload.getUploadId()));
        }
        // This operation lists the parts that have been uploaded for a specific multipart upload.
        try {
            return session.getClient().multipartListParts(upload);
        }
        catch(S3ServiceException e) {
            throw new S3ExceptionMappingService().map(MessageFormat.format("Upload {0} failed", upload.getObjectKey()), e);
        }
    }

    @Override
    public void delete(final MultipartUpload upload) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Delete multipart upload %s", upload.getUploadId()));
        }
        try {
            session.getClient().multipartAbortUpload(upload);
        }
        catch(S3ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot delete {0}", e,
                new Path(new Path(PathNormalizer.normalize(upload.getBucketName()), EnumSet.of(Path.Type.directory)),
                    upload.getObjectKey(), EnumSet.of(Path.Type.file)));
        }
    }
}
