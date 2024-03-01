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

import ch.cyberduck.core.CachingVersionIdProvider;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.VersionIdProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2VersionIdProvider extends CachingVersionIdProvider implements VersionIdProvider {
    private static final Logger log = LogManager.getLogger(B2VersionIdProvider.class);

    private final PathContainerService containerService = new B2PathContainerService();
    private final B2Session session;

    public B2VersionIdProvider(final B2Session session) {
        super(session.getCaseSensitivity());
        this.session = session;
    }

    @Override
    public String getVersionId(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return version %s from attributes for file %s", file.attributes().getVersionId(), file));
            }
            return file.attributes().getVersionId();
        }
        final String cached = super.getVersionId(file);
        if(cached != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached versionid %s for file %s", cached, file));
            }
            return cached;
        }
        try {
            if(containerService.isContainer(file)) {
                final B2BucketResponse info = session.getClient().listBucket(file.getName());
                if(null == info) {
                    throw new NotfoundException(file.getAbsolute());
                }
                // Cache in file attributes
                return this.cache(file, info.getBucketId());
            }
            // Files that have been hidden will not be returned
            final B2ListFilesResponse response = session.getClient().listFileNames(
                    this.getVersionId(containerService.getContainer(file)), containerService.getKey(file), 1,
                    new DirectoryDelimiterPathContainerService().getKey(file.getParent()),
                    String.valueOf(Path.DELIMITER));
            // Find for exact filename match (.bzEmpty file for directories)
            final Optional<B2FileInfoResponse> optional = response.getFiles().stream().filter(
                    info -> StringUtils.equals(containerService.getKey(file), info.getFileName())).findFirst();
            if(optional.isPresent()) {
                // Cache in file attributes
                return this.cache(file, optional.get().getFileId());
            }
            if(file.isDirectory()) {
                // Search for common prefix returned when no placeholder file was found
                if(response.getFiles().stream().anyMatch(
                        info -> StringUtils.startsWith(new DirectoryDelimiterPathContainerService().getKey(file), info.getFileName()))) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Common prefix found for %s but no placeholder file", file));
                    }
                    return null;
                }
                throw new NotfoundException(file.getAbsolute());
            }
            throw new NotfoundException(file.getAbsolute());
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(this).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
