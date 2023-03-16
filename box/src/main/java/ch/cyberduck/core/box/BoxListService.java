package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.api.FoldersApi;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;
import ch.cyberduck.core.box.io.swagger.client.model.Items;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BoxListService implements ListService {
    private static final Logger log = LogManager.getLogger(BoxListService.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;
    private final BoxAttributesFinderFeature attributes;

    public BoxListService(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.attributes = new BoxAttributesFinderFeature(session, fileid);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return this.list(directory, listener, new HostPreferences(session.getHost()).getInteger("box.listing.chunksize"));
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final int chunksize) throws BackgroundException {
        try {
            final AttributedList<Path> list = new AttributedList<>();
            int offset = 0;
            Items items;
            do {
                items = new FoldersApi(new BoxApiClient(session.getClient())).getFoldersIdItems(directory.isRoot() ? "0" :
                                fileid.getFileId(directory),
                        BoxAttributesFinderFeature.DEFAULT_FIELDS, false, null, (long) offset, (long) chunksize,
                        StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
                for(Object entry : items.getEntries()) {
                    if(!(entry instanceof Map)) {
                        log.error(String.format("Unexpected entry %s", entry));
                        continue;
                    }
                    final Object type = ((Map) entry).get("type");
                    if(!(type instanceof String)) {
                        log.error(String.format("Missing type %s", type));
                        continue;
                    }
                    switch(type.toString()) {
                        case "file":
                            final File file = new JSON().getContext(null).readValue(new JSON().getContext(null)
                                    .writeValueAsString(entry), File.class);
                            list.add(new Path(directory, file.getName(), EnumSet.of(Path.Type.file),
                                    attributes.toAttributes(file)));
                            break;
                        case "folder":
                            final Folder folder = new JSON().getContext(null).readValue(new JSON().getContext(null)
                                    .writeValueAsString(entry), Folder.class);
                            list.add(new Path(directory, folder.getName(), EnumSet.of(Path.Type.directory),
                                    attributes.toAttributes(folder)));
                            break;
                    }
                }
                offset += chunksize;
                listener.chunk(directory, list);
            }
            while(items.getEntries().size() == chunksize);
            return list;
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map("Listing directory {0} failed", e, directory);
        }
        catch(JsonProcessingException e) {
            throw new DefaultIOExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
