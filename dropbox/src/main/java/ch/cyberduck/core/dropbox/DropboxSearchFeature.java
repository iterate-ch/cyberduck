package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchMatchV2;
import com.dropbox.core.v2.files.SearchOptions;
import com.dropbox.core.v2.files.SearchV2Result;

public class DropboxSearchFeature implements Search {
    private static final Logger log = LogManager.getLogger(DropboxSearchFeature.class);

    private final DropboxSession session;
    private final DropboxAttributesFinderFeature attributes;
    private final PathContainerService containerService;

    public DropboxSearchFeature(final DropboxSession session) {
        this.session = session;
        this.attributes = new DropboxAttributesFinderFeature(session);
        this.containerService = new DropboxPathContainerService(session);
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> regex, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> list = new AttributedList<>();
            long start = 0;
            SearchV2Result result = new DbxUserFilesRequests(session.getClient(workdir)).searchV2Builder(regex.toString())
                    .withOptions(SearchOptions.newBuilder().withPath(containerService.getKey(workdir)).build()).start();
            this.parse(workdir, listener, list, result);
            while(result.getHasMore()) {
                this.parse(workdir, listener, list, result = new DbxUserFilesRequests(session.getClient(workdir)).searchContinueV2(result.getCursor()));
                if(this.parse(workdir, listener, list, result)) {
                    return null;
                }
            }
            return list;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Failure to read attributes of {0}", e, workdir);
        }
    }

    protected boolean parse(final Path workdir, final ListProgressListener listener, final AttributedList<Path> list, final SearchV2Result result) throws ConnectionCanceledException {
        final List<SearchMatchV2> matches = result.getMatches();
        for(SearchMatchV2 match : matches) {
            final Metadata metadata = match.getMetadata().getMetadataValue();
            final EnumSet<Path.Type> type;
            if(metadata instanceof FileMetadata) {
                type = EnumSet.of(Path.Type.file);
            }
            else if(metadata instanceof FolderMetadata) {
                type = EnumSet.of(Path.Type.directory);
            }
            else {
                log.warn(String.format("Skip file %s", metadata));
                return true;
            }
            list.add(new Path(metadata.getPathDisplay(), type, attributes.toAttributes(metadata)));
            listener.chunk(workdir, list);
        }
        return false;
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }

}
