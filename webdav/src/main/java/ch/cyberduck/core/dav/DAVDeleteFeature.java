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
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.sardine.impl.SardineException;

public class DAVDeleteFeature implements Delete {

    private final DAVSession session;

    public DAVDeleteFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> deleted = new ArrayList<Path>();
        for(Map.Entry<Path, TransferStatus> file : files.entrySet()) {
            boolean skip = false;
            for(Path d : deleted) {
                if(file.getKey().isChild(d)) {
                    skip = true;
                    break;
                }
            }
            if(skip) {
                continue;
            }
            deleted.add(file.getKey());
            callback.delete(file.getKey());
            try {
                if(file.getValue().getLockId() != null) {
                    // Indicate that the client has knowledge of that state token
                    session.getClient().delete(new DAVPathEncoder().encode(file.getKey()),
                        Collections.singletonMap(HttpHeaders.IF, String.format("(<%s>)", file.getValue().getLockId())));
                }
                else {
                    session.getClient().delete(new DAVPathEncoder().encode(file.getKey()));
                }
            }
            catch(SardineException e) {
                throw new DAVExceptionMappingService().map("Cannot delete {0}", e, file.getKey());
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e, file.getKey());
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
