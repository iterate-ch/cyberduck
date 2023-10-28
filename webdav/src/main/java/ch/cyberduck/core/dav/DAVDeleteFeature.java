package ch.cyberduck.core.dav;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.VoidResponseHandler;

public class DAVDeleteFeature implements Delete {

    private final DAVSession session;

    public DAVDeleteFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> deleted = new ArrayList<Path>();
        for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
            boolean skip = false;
            final Path file = entry.getKey();
            for(Path d : deleted) {
                if(file.isChild(d)) {
                    skip = true;
                    break;
                }
            }
            if(skip) {
                continue;
            }
            deleted.add(file);
            callback.delete(file);
            try {
                final TransferStatus status = entry.getValue();
                session.getClient().execute(this.toRequest(file, status), new VoidResponseHandler());
            }
            catch(SardineException e) {
                throw new DAVExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e, file);
            }
        }
    }

    protected HttpRequestBase toRequest(final Path file, final TransferStatus status) {
        final HttpDelete request = new HttpDelete(new DAVPathEncoder().encode(file));
        if(session.getFeature(Lock.class) != null && status.getLockId() != null) {
            // Indicate that the client has knowledge of that state token
            request.setHeader(HttpHeaders.IF, String.format("(<%s>)", status.getLockId()));
        }
        return request;
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
