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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.VersionIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2VersionIdProvider extends CachingVersionIdProvider implements VersionIdProvider {
    private static final Logger log = LogManager.getLogger(B2VersionIdProvider.class);

    private final PathContainerService containerService = new B2PathContainerService();
    private final B2Session session;
    private final VersioningConfiguration versioning;

    public B2VersionIdProvider(final B2Session session) {
        this(session, new VersioningConfiguration(new HostPreferences(session.getHost()).getBoolean("b2.listing.versioning.enable")));
    }

    public B2VersionIdProvider(final B2Session session, final VersioningConfiguration versioning) {
        super(session.getCaseSensitivity());
        this.session = session;
        this.versioning = versioning;
    }

    @Override
    public String getVersionId(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                if(StringUtils.isNotBlank(file.attributes().getFileId())) {
                    return file.attributes().getFileId();
                }
                final B2BucketResponse info = session.getClient().listBucket(file.getName());
                if(null == info) {
                    throw new NotfoundException(file.getAbsolute());
                }
                // Cache in file attributes
                return info.getBucketId();
            }
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
            // Files that have been hidden will not be returned
            final B2ListFilesResponse response = session.getClient().listFileNames(
                    this.getVersionId(containerService.getContainer(file)), containerService.getKey(file), 1);
            for(B2FileInfoResponse info : response.getFiles()) {
                if(StringUtils.equals(containerService.getKey(file), info.getFileName())) {
                    // Cache in file attributes
                    return this.cache(file, info.getFileId());
                }
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

    @Override
    public String cache(final Path file, final String id) {
        if(versioning.isEnabled()) {
            return super.cache(file, id);
        }
        return id;
    }
}
