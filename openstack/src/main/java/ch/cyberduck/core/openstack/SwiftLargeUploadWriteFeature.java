/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.threading.RetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftLargeUploadWriteFeature implements Write {
    private static final Logger log = Logger.getLogger(SwiftLargeUploadWriteFeature.class);

    private final SwiftSession session;

    private final Find finder;

    private final AttributesFinder attributes;

    private final PathContainerService containerService
            = new SwiftPathContainerService();

    private final SwiftSegmentService segmentService;

    private final SwiftRegionService regionService;

    public SwiftLargeUploadWriteFeature(final SwiftSession session) {
        this(session, session.getFeature(Find.class, new DefaultFindFeature(session)), session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session)));
    }

    public SwiftLargeUploadWriteFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, regionService, new SwiftSegmentService(session, regionService), session.getFeature(Find.class, new DefaultFindFeature(session)), session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session)));
    }

    public SwiftLargeUploadWriteFeature(final SwiftSession session, final SwiftRegionService regionService, final SwiftSegmentService segmentService) {
        this(session, regionService, segmentService, session.getFeature(Find.class, new DefaultFindFeature(session)), session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session)));
    }

    public SwiftLargeUploadWriteFeature(final SwiftSession session, final Find finder, final AttributesFinder attributes) {
        this(session, new SwiftRegionService(session), new SwiftSegmentService(session), finder, attributes);
    }

    public SwiftLargeUploadWriteFeature(final SwiftSession session, final SwiftRegionService regionService, final SwiftSegmentService segmentService,
                                        final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.regionService = regionService;
        this.segmentService = segmentService;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        final LargeUploadOutputStream stream = new LargeUploadOutputStream(file, status);
        return new ResponseOutputStream<List<StorageObject>>(new BufferedOutputStream(stream,
                PreferencesFactory.get().getInteger("openstack.upload.largeobject.size.minimum"))) {
            @Override
            public List<StorageObject> getResponse() throws BackgroundException {
                return stream.getCompleted();
            }
        };
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    private final class LargeUploadOutputStream extends OutputStream {
        final List<StorageObject> completed = new ArrayList<StorageObject>();
        private final Path file;
        private final TransferStatus status;
        private int segmentNumber;

        public LargeUploadOutputStream(final Path file, final TransferStatus status) {
            this.file = file;
            this.status = status;
        }

        @Override
        public void write(final int value) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            try {
                completed.add(new RetryCallable<StorageObject>() {
                    @Override
                    public StorageObject call() throws BackgroundException {
                        try {
                            final TransferStatus status = new TransferStatus().length(len);
                            // Segment name with left padded segment number
                            final Path segment = new Path(containerService.getContainer(file),
                                    segmentService.name(file, status.getLength(), ++segmentNumber), EnumSet.of(Path.Type.file));
                            final ByteArrayEntity entity = new ByteArrayEntity(b, off, len);
                            final HashMap<String, String> headers = new HashMap<>();
                            final String checksum;
                            try {
                                checksum = session.getClient().storeObject(
                                        regionService.lookup(file),
                                        containerService.getContainer(segment).getName(), containerService.getKey(segment),
                                        entity, headers, null);
                            }
                            catch(GenericException e) {
                                throw new SwiftExceptionMappingService().map("Upload {0} failed", e, file);
                            }
                            catch(IOException e) {
                                throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                            }
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Saved segment %s with checksum %s", segment, checksum));
                            }
                            final StorageObject stored = new StorageObject(containerService.getKey(segment));
                            stored.setMd5sum(checksum);
                            stored.setSize(status.getLength());
                            return stored;
                        }
                        catch(BackgroundException e) {
                            if(this.retry(e, new DisabledProgressListener(), status)) {
                                return this.call();
                            }
                            else {
                                throw e;
                            }
                        }
                    }
                }.call());
            }
            catch(Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        @Override
        public void close() throws IOException {
            // Create and upload the large object manifest. It is best to upload all the segments first and
            // then create or update the manifest.
            try {
                // Static Large Object
                final String manifest = segmentService.manifest(containerService.getContainer(file).getName(), completed);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Creating SLO manifest %s for %s", manifest, file));
                }
                session.getClient().createSLOManifestObject(regionService.lookup(
                        containerService.getContainer(file)),
                        containerService.getContainer(file).getName(),
                        status.getMime(),
                        containerService.getKey(file), manifest, Collections.emptyMap());
            }
            catch(BackgroundException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        public List<StorageObject> getCompleted() {
            return completed;
        }
    }
}
