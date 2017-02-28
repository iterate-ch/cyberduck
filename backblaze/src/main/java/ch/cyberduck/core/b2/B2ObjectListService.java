package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2ObjectListService implements ListService {
    private static final Logger log = Logger.getLogger(B2ObjectListService.class);

    private final PathContainerService containerService
            = new PathContainerService();

    private final B2Session session;

    private final int chunksize;

    public B2ObjectListService(final B2Session session) {
        this(session, PreferencesFactory.get().getInteger("b2.listing.chunksize"));
    }

    public B2ObjectListService(final B2Session session, final int chunksize) {
        this.session = session;
        this.chunksize = chunksize;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> objects = new AttributedList<Path>();
            Marker marker;
            if(containerService.isContainer(directory)) {
                marker = new Marker(null, null);
            }
            else {
                marker = new Marker(String.format("%s%s", containerService.getKey(directory), Path.DELIMITER), null);
            }
            // Seen placeholders
            final Map<String, Integer> revisions = new HashMap<String, Integer>();
            do {
                // In alphabetical order by file name, and by reverse of date/time uploaded for
                // versions of files with the same name.
                final B2ListFilesResponse response = session.getClient().listFileVersions(
                        new B2FileidProvider(session).getFileid(containerService.getContainer(directory)),
                        marker.nextFilename, marker.nextFileId, chunksize,
                        containerService.isContainer(directory) ? null : String.format("%s%s", containerService.getKey(directory), String.valueOf(Path.DELIMITER)),
                        String.valueOf(Path.DELIMITER));
                marker = this.parse(directory, objects, response, revisions);
                listener.chunk(directory, objects);
            }
            while(marker.hasNext());
            return objects;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    protected Marker parse(final Path directory, final AttributedList<Path> objects,
                           final B2ListFilesResponse response, final Map<String, Integer> revisions) throws BackgroundException {
        for(B2FileInfoResponse file : response.getFiles()) {
            if(StringUtils.isBlank(file.getFileId())) {
                // Common prefix
                final Path placeholder = new Path(directory, PathNormalizer.name(file.getFileName()), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
                objects.add(placeholder);
                continue;
            }
            final PathAttributes attributes = this.parse(file);
            final Integer revision;
            if(revisions.keySet().contains(file.getFileName())) {
                // Later version already found
                attributes.setDuplicate(true);
                revision = revisions.get(file.getFileName()) + 1;
            }
            else {
                revision = 1;
            }
            attributes.setSize(file.getSize());
            revisions.put(file.getFileName(), revision);
            attributes.setRevision(revision);
            objects.add(new Path(directory, PathNormalizer.name(file.getFileName()), EnumSet.of(Path.Type.file), attributes));
        }
        if(null == response.getNextFileName()) {
            return new Marker(response.getNextFileName(), response.getNextFileId());
        }
        return new Marker(response.getNextFileName(), response.getNextFileId());
    }

    /**
     * @param response List filenames response from server
     * @return Null when respone filename is not child of working directory directory
     */
    protected PathAttributes parse(final B2FileInfoResponse response) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setChecksum(Checksum.parse(StringUtils.lowerCase(response.getContentSha1(), Locale.ROOT)));
        final long timestamp = response.getUploadTimestamp();
        attributes.setCreationDate(timestamp);
        attributes.setModificationDate(timestamp);
        attributes.setVersionId(response.getFileId());
        switch(response.getAction()) {
            case hide:
            case start:
                attributes.setDuplicate(true);
                attributes.setSize(-1L);
                break;
        }
        return attributes;
    }

    private static final class Marker {
        public final String nextFilename;
        public final String nextFileId;

        public Marker(final String nextFilename, final String nextFileId) {
            this.nextFilename = nextFilename;
            this.nextFileId = nextFileId;
        }

        public boolean hasNext() {
            return nextFilename != null;
        }
    }
}
