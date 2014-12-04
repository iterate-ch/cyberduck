package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

/**
 * @version $Id$
 */
public class SwiftWriteFeature extends AbstractHttpWriteFeature<StorageObject> implements Write {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private SwiftSession session;

    private SwiftSegmentService segmentService;

    private SwiftObjectListService listService;

    private Find finder;

    private Preferences preferences
            = PreferencesFactory.get();

    public SwiftWriteFeature(final SwiftSession session) {
        this(session, new SwiftObjectListService(session), new SwiftSegmentService(session), new DefaultFindFeature(session));
    }

    public SwiftWriteFeature(final SwiftSession session, final SwiftObjectListService listService,
                             final SwiftSegmentService segmentService) {
        this(session, listService, segmentService, new DefaultFindFeature(session));
    }

    public SwiftWriteFeature(final SwiftSession session, final SwiftObjectListService listService,
                             final SwiftSegmentService segmentService, final Find finder) {
        super(session);
        this.session = session;
        this.listService = listService;
        this.segmentService = segmentService;
        this.finder = finder;
    }

    @Override
    public ResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status) throws BackgroundException {
        final Map<String, String> metadata = new HashMap<String, String>();
        // Default metadata for new files
        for(String m : preferences.getList("openstack.metadata.default")) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String key = m.substring(0, split);
            if(StringUtils.isBlank(key)) {
                log.warn(String.format("Missing key in %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in %s", m));
                continue;
            }
            metadata.put(key, value);
        }
        // Submit store run to background thread
        final DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>() {
            /**
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public StorageObject call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final String checksum = session.getClient().storeObject(new SwiftRegionService(session).lookup(containerService.getContainer(file)),
                            containerService.getContainer(file).getName(), containerService.getKey(file),
                            entity, metadata, null);
                    final StorageObject stored = new StorageObject(containerService.getKey(file));
                    stored.setMd5sum(checksum);
                    stored.setSize(status.getLength());
                    return stored;
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    @Override
    public Append append(final Path file, final Long length, final Cache cache) throws BackgroundException {
        if(length >= preferences.getLong("openstack.upload.largeobject.threshold")) {
            if(preferences.getBoolean("openstack.upload.largeobject")) {
                Long size = 0L;
                final List<Path> segments = listService.list(
                        new Path(containerService.getContainer(file), segmentService.basename(file, length), EnumSet.of(Path.Type.directory)),
                        new DisabledListProgressListener());
                if(segments.isEmpty()) {
                    return Write.notfound;
                }
                for(Path segment : segments) {
                    size += segment.attributes().getSize();
                }
                return new Append(size);
            }
        }
        if(finder.withCache(cache).find(file)) {
            return Write.override;
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }
}
