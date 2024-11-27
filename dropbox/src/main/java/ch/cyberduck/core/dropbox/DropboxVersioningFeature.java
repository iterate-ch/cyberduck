package ch.cyberduck.core.dropbox;

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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Versioning;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListRevisionsResult;

public class DropboxVersioningFeature implements Versioning {
    private static final Logger log = LogManager.getLogger(DropboxVersioningFeature.class);

    private final DropboxSession session;
    private final PathContainerService containerService;

    public DropboxVersioningFeature(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService();
    }


    @Override
    public VersioningConfiguration getConfiguration(final Path container) {
        return VersioningConfiguration.empty();
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        try {
            new DbxUserFilesRequests(session.getClient(file)).restore(containerService.getKey(file), file.attributes().getVersionId());
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isDirectory()) {
            return AttributedList.emptyList();
        }
        try {
            final AttributedList<Path> versions = new AttributedList<>();
            final ListRevisionsResult result = new DbxUserFilesRequests(session.getClient(file)).listRevisions(containerService.getKey(file));
            final List<FileMetadata> entries = result.getEntries();
            final DropboxAttributesFinderFeature attr = new DropboxAttributesFinderFeature(session);
            for(FileMetadata revision : entries) {
                if(StringUtils.equals(revision.getRev(), file.attributes().getVersionId())) {
                    continue;
                }
                log.debug("Found revision {}", revision);
                final PathAttributes attributes = attr.toAttributes(revision);
                attributes.setDuplicate(true);
                versions.add(new Path(file.getParent(), PathNormalizer.name(revision.getName()), file.getType(), attributes));
                listener.chunk(file.getParent(), versions);
            }
            return versions;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.revert, Flags.list);
    }
}
