package ch.cyberduck.core.dav;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.sardine.impl.SardineException;

public class DAVLockFeature implements Lock<String> {

    public static final String LOCK_TOKEN = "Lock-Token";

    private final DAVSession session;

    public DAVLockFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public String lock(final Path file) throws BackgroundException {
        try {
            return session.getClient().lock(new DAVPathEncoder(session).encode(file));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public void unlock(final Path file, final String token) throws BackgroundException {
        try {
            session.getClient().unlock(new DAVPathEncoder(session).encode(file), token);
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    /**
     * Headers indicating that the client has knowledge of a lock token.
     *
     * @return Empty map if the file is not locked
     */
    public static Map<String, String> toHeaders(final DAVSession session, final Path file, final TransferStatus status) {
        if(null == status.getLockId()) {
            return Collections.emptyMap();
        }
        final Map<String, String> headers = new LinkedHashMap<>();
        // Indicate that the client has knowledge of that state token
        headers.put(HttpHeaders.IF, String.format("<%s> (<%s>)", new DAVPathEncoder(session).encode(file), status.getLockId()));
        // Infinite Scale and OpenCloud read the lock token from the Lock-Token header only. Ignored by servers following RFC 4918
        headers.put(LOCK_TOKEN, String.format("<%s>", status.getLockId()));
        return headers;
    }
}
