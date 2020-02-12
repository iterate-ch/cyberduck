package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVExceptionMappingService;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.util.SardineUtil;

public class NextcloudTimestampFeature extends DAVTimestampFeature {

    private final NextcloudSession session;

    public NextcloudTimestampFeature(final NextcloudSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final List<DavResource> resources = session.getClient().propfind(new DAVPathEncoder().encode(file), 0,
                Collections.singleton(SardineUtil.createQNameWithDefaultNamespace("getlastmodified")));
            final Map<String, String> headers = new HashMap<>();
            if(null != status.getTimestamp()) {
                headers.put("X-OC-Mtime", String.valueOf(status.getTimestamp()));
            }
            for(DavResource resource : resources) {
                if(status.getLockId() != null) {
                    headers.put(HttpHeaders.IF, String.format("(<%s>)", status.getLockId()));
                }
                session.getClient().patch(new DAVPathEncoder().encode(file),
                    this.getCustomProperties(resource, status.getTimestamp()), Collections.emptyList(), headers);
                break;
            }
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }
}
