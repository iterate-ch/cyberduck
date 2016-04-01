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
import java.util.List;
import java.util.Map;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2ObjectListService implements ListService {
    private static final Logger log = Logger.getLogger(B2ObjectListService.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

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
            String nextFileid = null;
            String nextFilename = containerService.getKey(directory);
            do {
                // In alphabetical order by file name, and by reverse of date/time uploaded for
                // versions of files with the same name.
                final B2ListFilesResponse response = session.getClient().listFileVersions(
                        new B2FileidProvider(session).getFileid(containerService.getContainer(directory)),
                        nextFilename, nextFileid, chunksize);
                final List<B2FileInfoResponse> files = response.getFiles();
                final Map<String, Integer> revisions = new HashMap<String, Integer>();
                for(B2FileInfoResponse file : files) {
                    final PathAttributes attributes = this.parse(directory, revisions, file);
                    if(attributes == null) {
                        continue;
                    }
                    if(StringUtils.endsWith(file.getFileName(), "/.bzEmpty")) {
                        objects.add(new Path(directory, PathNormalizer.name(StringUtils.removeEnd(file.getFileName(), "/.bzEmpty")),
                                EnumSet.of(Path.Type.directory, Path.Type.placeholder), attributes));
                    }
                    else {
                        attributes.setSize(file.getSize());
                        objects.add(new Path(directory, PathNormalizer.name(file.getFileName()), EnumSet.of(Path.Type.file), attributes));
                    }
                }
                nextFilename = response.getNextFileName();
                nextFileid = response.getNextFileId();
                listener.chunk(directory, objects);
            }
            while(nextFileid != null);
            return objects;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    protected PathAttributes parse(final Path directory, final Map<String, Integer> revisions, final B2FileInfoResponse file) {
        if(!StringUtils.equals(PathNormalizer.parent(
                StringUtils.removeEnd(file.getFileName(), ".bzEmpty"), Path.DELIMITER),
                containerService.isContainer(directory) ? String.valueOf(Path.DELIMITER) : containerService.getKey(directory))) {
            log.warn(String.format("Skip file %s", file));
            return null;
        }
        final PathAttributes attributes = new PathAttributes();
        attributes.setChecksum(Checksum.parse(file.getContentSha1()));
        final long timestamp = file.getUploadTimestamp();
        attributes.setCreationDate(timestamp);
        attributes.setModificationDate(timestamp);
        attributes.setVersionId(file.getFileId());
        switch(file.getAction()) {
            case hide:
            case start:
                attributes.setDuplicate(true);
                break;
        }
        final Integer revision;
        if(revisions.keySet().contains(file.getFileName())) {
            // Later version already found
            attributes.setDuplicate(true);
            revision = revisions.get(file.getFileName()) + 1;
        }
        else {
            revision = 1;
        }
        revisions.put(file.getFileName(), revision);
        attributes.setRevision(revision);
        return attributes;
    }
}
