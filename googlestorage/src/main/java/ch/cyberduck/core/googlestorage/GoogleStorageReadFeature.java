package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import com.google.api.client.http.HttpHeaders;
import com.google.api.services.storage.Storage;

public class GoogleStorageReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(GoogleStorageReadFeature.class);

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    public GoogleStorageReadFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(0L == status.getLength()) {
                return new NullInputStream(0L);
            }
            final Storage.Objects.Get request = session.getClient().objects().get(
                    containerService.getContainer(file).getName(), containerService.getKey(file));
            if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                request.setUserProject(session.getHost().getCredentials().getUsername());
            }
            final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
                    containerService.getContainer(file)
            ) : VersioningConfiguration.empty();
            if(versioning.isEnabled()) {
                if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
                    request.setGeneration(Long.parseLong(file.attributes().getVersionId()));
                }
            }
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                final String header;
                if(TransferStatus.UNKNOWN_LENGTH == range.getEnd()) {
                    header = String.format("bytes=%d-", range.getStart());
                }
                else {
                    header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
                }
                if(log.isDebugEnabled()) {
                    log.debug("Add range header {} for file {}", header, file);
                }
                final HttpHeaders headers = request.getRequestHeaders();
                headers.setRange(header);
                // Disable compression
                headers.setAcceptEncoding("identity");
            }
            return request.executeMediaAsInputStream();
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
