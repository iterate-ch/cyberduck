package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.features.VersionIdProvider;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CteraBulkFeature extends DisabledBulkFeature {
    private static final Logger log = LogManager.getLogger(CteraBulkFeature.class);

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
                    final DirectIO metadata;
                    try {
                        metadata = this.getMetadata(file.getKey().remote);
                    }
                    catch(IOException e) {
                        log.warn("Ignore DirectIO download failure {} for {}", e, file.getKey().remote);
                        continue;
                    }
                    final TransferStatus status = file.getValue();
                    if(status.isSegmented()) {
                        final List<TransferStatus> segments = status.getSegments();
                        if(segments.size() <= metadata.chunks.size()) {
                            for(int i = 0; i < segments.size(); i++) {
                                final TransferStatus segment = segments.get(i);
                                if(i == 0) {
                                    if(segment.getOffset() > 0) {
                                        log.warn("DirectIO download for {} with an initial offset is not supported", file.getKey().remote);
                                        continue;
                                    }
                                }
                                segment.setUrl(metadata.chunks.get(i).url);
                                final Map<String, String> parameters = new HashMap<>(segment.getParameters());
                                parameters.put(CteraDirectIOReadFeature.CTERA_WRAPPEDKEY, metadata.wrapped_key);
                                segment.setParameters(parameters);
                            }
                        }
                        else {
                            throw new TransferCanceledException(String.format("Mismatch between number of segments and chunks for %s", file.getKey().remote));
                        }
                    }
                    else {
                        if(0L == status.getOffset()) {
                            status.setUrl(metadata.chunks.get(0).url);
                            final Map<String, String> parameters = new HashMap<>(status.getParameters());
                            parameters.put(CteraDirectIOReadFeature.CTERA_WRAPPEDKEY, metadata.wrapped_key);
                            status.setParameters(parameters);
                        }
                        else {
                            log.warn("DirectIO download for {} with an initial offset is not supported", file.getKey().remote);
                        }
                    }
                }
                break;
        }
        return files;
    }

    private DirectIO getMetadata(final Path file) throws IOException, BackgroundException {
        final HttpGet request = new HttpGet(String.format("%s%s%s", new HostUrlProvider().withUsername(false).withPath(false)
                .get(session.getHost()), CteraDirectIOInterceptor.DIRECTIO_PATH, versionid.getVersionId(file)));
        return session.getClient().getClient().execute(request, new AbstractResponseHandler<DirectIO>() {
            @Override
            public DirectIO handleEntity(final HttpEntity entity) throws IOException {
                final ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(entity.getContent(), DirectIO.class);
            }
        });
    }
}
