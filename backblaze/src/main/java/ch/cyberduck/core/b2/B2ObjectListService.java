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
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import synapticloop.b2.Action;
import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2ObjectListService implements ListService {
    private static final Logger log = LogManager.getLogger(B2ObjectListService.class);

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    private final B2Session session;

    private final int chunksize;
    private final B2VersionIdProvider fileid;

    public B2ObjectListService(final B2Session session, final B2VersionIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getInteger("b2.listing.chunksize"));
    }

    public B2ObjectListService(final B2Session session, final B2VersionIdProvider fileid, final int chunksize) {
        this.session = session;
        this.fileid = fileid;
        this.chunksize = chunksize;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> objects = new AttributedList<>();
            Marker marker = new Marker(this.createPrefix(directory), null);
            final String containerId = fileid.getVersionId(containerService.getContainer(directory));
            // Seen placeholders
            final Map<String, Long> revisions = new HashMap<>();
            boolean hasDirectoryPlaceholder = containerService.isContainer(directory);
            do {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("List directory %s with marker %s", directory, marker));
                }
                // In alphabetical order by file name, and by reverse of date/time uploaded for
                // versions of files with the same name.
                final B2ListFilesResponse response = session.getClient().listFileVersions(
                        containerId,
                        marker.nextFilename, marker.nextFileId, chunksize,
                        this.createPrefix(directory),
                        String.valueOf(Path.DELIMITER));
                marker = this.parse(directory, objects, response, revisions);
                if(null == marker.nextFileId) {
                    if(!response.getFiles().isEmpty()) {
                        hasDirectoryPlaceholder = true;
                    }
                }
                listener.chunk(directory, objects);
            }
            while(marker.hasNext());
            if(!hasDirectoryPlaceholder && objects.isEmpty()) {
                if(log.isWarnEnabled()) {
                    log.warn(String.format("No placeholder found for directory %s", directory));
                }
                throw new NotfoundException(directory.getAbsolute());
            }
            return objects;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private String createPrefix(final Path directory) {
        return containerService.isContainer(directory) ? StringUtils.EMPTY :
                directory.isDirectory() ? String.format("%s%s", containerService.getKey(directory), Path.DELIMITER) : containerService.getKey(directory);
    }

    private Marker parse(final Path directory, final AttributedList<Path> objects,
                         final B2ListFilesResponse response, final Map<String, Long> revisions) {
        final B2AttributesFinderFeature attr = new B2AttributesFinderFeature(session, fileid);
        for(B2FileInfoResponse info : response.getFiles()) {
            if(StringUtils.equals(PathNormalizer.name(info.getFileName()), B2PathContainerService.PLACEHOLDER)) {
                continue;
            }
            if(directory.isFile()) {
                if(!StringUtils.equals(directory.getName(), PathNormalizer.name(info.getFileName()))) {
                    log.warn(String.format("Skip %s not matching %s", info, directory.getName()));
                    continue;
                }
            }
            if(StringUtils.isBlank(info.getFileId())) {
                // Common prefix
                final Path placeholder = new Path(directory.isDirectory() ? directory : directory.getParent(),
                        PathNormalizer.name(StringUtils.chomp(info.getFileName(), String.valueOf(Path.DELIMITER))),
                        EnumSet.of(Path.Type.directory, Path.Type.placeholder));
                objects.add(placeholder);
                continue;
            }
            final PathAttributes attributes = attr.toAttributes(info);
            long revision = 0;
            if(revisions.containsKey(info.getFileName())) {
                // Later version already found
                attributes.setDuplicate(true);
                revision = revisions.get(info.getFileName()) + 1L;
                attributes.setRevision(revision);
            }
            revisions.put(info.getFileName(), revision);
            final Path f = new Path(directory.isDirectory() ? directory : directory.getParent(), PathNormalizer.name(info.getFileName()),
                    info.getAction() == Action.start ? EnumSet.of(Path.Type.file, Path.Type.upload) : EnumSet.of(Path.Type.file), attributes);
            fileid.cache(f, info.getFileId());
            objects.add(f);
        }
        if(null == response.getNextFileName()) {
            return new Marker(response.getNextFileName(), response.getNextFileId());
        }
        return new Marker(response.getNextFileName(), response.getNextFileId());
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

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Marker{");
            sb.append("nextFilename='").append(nextFilename).append('\'');
            sb.append(", nextFileId='").append(nextFileId).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

}
