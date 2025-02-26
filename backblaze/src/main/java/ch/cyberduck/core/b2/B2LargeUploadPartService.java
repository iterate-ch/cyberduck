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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;
import synapticloop.b2.response.B2ListPartsResponse;
import synapticloop.b2.response.B2UploadPartResponse;

public class B2LargeUploadPartService {
    private static final Logger log = LogManager.getLogger(B2LargeUploadPartService.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2LargeUploadPartService(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    /**
     * @param file File reference
     * @return File id of unfinished large upload
     */
    public List<B2FileInfoResponse> find(final Path file) throws BackgroundException {
        log.debug("Finding multipart uploads for {}", file);
        try {
            final List<B2FileInfoResponse> uploads = new ArrayList<B2FileInfoResponse>();
            // This operation lists in-progress multipart uploads. An in-progress multipart upload is a
            // multipart upload that has been initiated, using the Initiate Multipart Upload request, but has
            // not yet been completed or aborted.
            String startFileId = null;
            do {
                final B2ListFilesResponse chunk;
                chunk = session.getClient().listUnfinishedLargeFiles(
                    fileid.getVersionId(containerService.getContainer(file)), startFileId, null);
                for(B2FileInfoResponse upload : chunk.getFiles()) {
                    if(file.isDirectory()) {
                        final Path parent = new Path(containerService.getContainer(file), upload.getFileName(), EnumSet.of(Path.Type.file)).getParent();
                        if(new SimplePathPredicate(parent).test(file)) {
                            uploads.add(upload);
                        }
                    }
                    else {
                        if(StringUtils.equals(upload.getFileName(), containerService.getKey(file))) {
                            uploads.add(upload);
                        }
                    }
                }
                log.info("Found {} previous multipart uploads for {}", uploads.size(), file);
                startFileId = chunk.getNextFileId();
            }
            while(startFileId != null);
            if(log.isInfoEnabled()) {
                for(B2FileInfoResponse upload : uploads) {
                    log.info("Found multipart upload {} for {}", upload, file);
                }
            }
            // Uploads are listed in the order they were started, with the oldest one first
            uploads.sort(new Comparator<B2FileInfoResponse>() {
                @Override
                public int compare(final B2FileInfoResponse o1, final B2FileInfoResponse o2) {
                    return o1.getUploadTimestamp().compareTo(o2.getUploadTimestamp());
                }
            });
            Collections.reverse(uploads);
            return uploads;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    /**
     * @param fileid File id of unfinished large upload
     * @return List of parts
     */
    public List<B2UploadPartResponse> list(final String fileid) throws BackgroundException {
        log.info("List completed parts of file {}", fileid);
        // This operation lists the parts that have been uploaded for a specific multipart upload.
        try {
            // Completed parts
            final List<B2UploadPartResponse> completed = new ArrayList<B2UploadPartResponse>();
            Integer startPartNumber = null;
            do {
                final B2ListPartsResponse response = session.getClient().listParts(
                        fileid, startPartNumber, HostPreferencesFactory.get(session.getHost()).getInteger("b2.listing.chunksize"));
                completed.addAll(response.getFiles());
                startPartNumber = response.getNextPartNumber();
            }
            while(startPartNumber != null);
            return completed;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(this.fileid).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    /**
     * Cancel large file upload with id
     */
    public void delete(final String id) throws BackgroundException {
        log.info("Delete multipart upload for fileid {}", id);
        try {
            session.getClient().cancelLargeFileUpload(id);
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
