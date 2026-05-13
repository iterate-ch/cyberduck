package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ctera.model.DirectIO;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.VersionIdProvider;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CteraBulkFeature extends DisabledBulkFeature {
    private static final Logger log = LogManager.getLogger(CteraBulkFeature.class);

    public static final String DIRECTIO_PARAMETER = "directio";

    private final CteraSession session;
    private final VersionIdProvider versionid;

    public CteraBulkFeature(final CteraSession session, final VersionIdProvider versionid) {
        this.session = session;
        this.versionid = versionid;
    }

    @Override
    public Map<TransferItem, TransferStatus> pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case upload:
                break;
            case download:
                for(Map.Entry<TransferItem, TransferStatus> file : files.entrySet()) {
                    final DirectIO metadata = this.getMetadata(file.getKey().remote);
                    log.debug("DirectIO metadata {} retrieved for {}", metadata, file.getKey().remote);
                    final TransferStatus status = file.getValue();
                    for(TransferStatus segment : status.getSegments()) {
                        segment.setParameters(Collections.singletonMap(DIRECTIO_PARAMETER, metadata));
                    }
                }
                break;
        }
        return files;
    }

    private DirectIO getMetadata(final Path file) throws BackgroundException {
        final HttpGet request = new HttpGet(String.format("%s%s%s", new HostUrlProvider().withUsername(false).withPath(false)
                .get(session.getHost()), CteraDirectIOInterceptor.DIRECTIO_PATH, versionid.getVersionId(file)));
        try {
            return session.getClient().getClient().execute(request, new AbstractResponseHandler<DirectIO>() {
                @Override
                public DirectIO handleEntity(final HttpEntity entity) throws IOException {
                    final ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), DirectIO.class);
                }
            });
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
