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
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;
import synapticloop.b2.response.B2ListPartsResponse;
import synapticloop.b2.response.B2UploadPartResponse;

public class B2LargeUploadPartService {
    private static final Logger log = Logger.getLogger(B2LargeUploadPartService.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2LargeUploadPartService(final B2Session session) {
        this.session = session;
    }

    /**
     * @param file File reference
     * @return File id of unfinished large upload
     */
    public List<B2FileInfoResponse> find(final Path file) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Finding multipart uploads for %s", file));
        }
        try {
            final List<B2FileInfoResponse> uploads = new ArrayList<B2FileInfoResponse>();
            // This operation lists in-progress multipart uploads. An in-progress multipart upload is a
            // multipart upload that has been initiated, using the Initiate Multipart Upload request, but has
            // not yet been completed or aborted.
            String startFileId = null;
            do {
                final B2ListFilesResponse chunk;
                chunk = session.getClient().listUnfinishedLargeFiles(
                        new B2FileidProvider(session).getFileid(containerService.getContainer(file)), startFileId, null);
                for(B2FileInfoResponse upload : chunk.getFiles()) {
                    if(file.isDirectory()) {
                        final Path parent = new Path(containerService.getContainer(file), upload.getFileName(),
                                EnumSet.of(Path.Type.file, Path.Type.upload)).getParent();
                        if(parent.equals(file)) {
                            uploads.add(upload);
                        }
                    }
                    else {
                        if(StringUtils.equals(upload.getFileName(), containerService.getKey(file))) {
                            uploads.add(upload);
                        }
                    }
                }
                if(log.isInfoEnabled()) {
                    log.info(String.format("Found %d previous multipart uploads for %s", uploads.size(), file));
                }
                startFileId = chunk.getNextFileId();
            }
            while(startFileId != null);
            if(log.isInfoEnabled()) {
                for(B2FileInfoResponse upload : uploads) {
                    log.info(String.format("Found multipart upload %s for %s", upload, file));
                }
            }
            // Uploads are listed in the order they were started, with the oldest one first
            Collections.reverse(uploads);
            return uploads;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, file);
        }
    }

    /**
     * @param fileid File id of unfinished large upload
     * @return List of parts
     */
    public List<B2UploadPartResponse> list(final String fileid) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("List completed parts of file %s", fileid));
        }
        // This operation lists the parts that have been uploaded for a specific multipart upload.
        try {
            // Completed parts
            final List<B2UploadPartResponse> completed = new ArrayList<B2UploadPartResponse>();
            Integer startPartNumber = null;
            do {
                final B2ListPartsResponse response = session.getClient().listParts(
                        fileid, startPartNumber, null);
                completed.addAll(response.getFiles());
                startPartNumber = response.getNextPartNumber();
            }
            while(startPartNumber != null);
            return completed;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public void delete(final String id) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Delete multipart upload for fileid %s", id));
        }
        try {
            session.getClient().cancelLargeFileUpload(id);
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
