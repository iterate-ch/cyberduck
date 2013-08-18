package ch.cyberduck.core.openstack;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * @version $Id$
 */
public class SwiftWriteFeature implements Write {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private PathContainerService containerService = new PathContainerService();

    private SwiftSession session;

    public SwiftWriteFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        final HashMap<String, String> metadata = new HashMap<String, String>();
        // Default metadata for new files
        for(String m : Preferences.instance().getList("openstack.metadata.default")) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String name = m.substring(0, split);
            if(StringUtils.isBlank(name)) {
                log.warn(String.format("Missing key in %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in %s", m));
                continue;
            }
            metadata.put(name, value);
        }
        // Submit store run to background thread
        final DelayedHttpEntityCallable<String> command = new DelayedHttpEntityCallable<String>() {
            /**
             *
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public String call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    return session.getClient().storeObject(
                            session.getRegion(containerService.getContainer(file)), containerService.getContainer(file).getName(),
                            containerService.getKey(file), entity,
                            metadata, null);
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Upload failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength() - status.getCurrent();
            }
        };
        return session.write(file, command);
    }

    /**
     * @param file
     * @return No Content-Range support
     */
    @Override
    public boolean append(final Path file) {
        return false;
    }
}
