package ch.cyberduck.core.idgard;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.idgard.io.swagger.client.ApiException;
import ch.cyberduck.core.idgard.io.swagger.client.api.BoxApiApi;
import ch.cyberduck.core.idgard.io.swagger.client.model.BoxMetaData;
import ch.cyberduck.core.idgard.io.swagger.client.model.Entry;
import ch.cyberduck.core.idgard.io.swagger.client.model.EntryList;
import ch.cyberduck.core.idgard.io.swagger.client.model.IdgardBox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.List;

public class IdgardListService implements ListService {
    private static final Logger log = LogManager.getLogger(IdgardListService.class);

    private final IdgardSession session;
    //private final DeepboxIdProvider fileid;

    public IdgardListService(final IdgardSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {

        if(directory.isRoot()) {
            return new BoxesListService().list(directory, listener);
        }

        if(directory.getParent().isDirectory()) {
            return new NodesInBoxesListService().list(directory, listener);
        }

        return AttributedList.EMPTY;
    }

    private final class BoxesListService implements ListService {
        @Override
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                final BoxApiApi rest = new BoxApiApi(session.getClient());
                final List<IdgardBox> boxes1 = rest.getBoxes();
                for(final IdgardBox box : boxes1) {
                    list.add(new Path(directory, box.getName(), EnumSet.of(Path.Type.directory, Path.Type.volume),
                            new DefaultPathAttributes().setFileId(box.getId()))
                    );
                }
                listener.chunk(directory, list);
                return list;
            }
            catch(ApiException e) {
                throw new BackgroundException("Listing directory failed", e);
            }
        }
    }

    private final class NodesInBoxesListService implements ListService {
        @Override
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                final BoxApiApi rest = new BoxApiApi(session.getClient());
                final BoxMetaData metadata = rest.getBox(directory.attributes().getFileId());

                final EntryList rootFolder = metadata.getRootFolder();
                rootFolder.getEntries().forEach(entry -> {
                    list.add(new Path(directory, entry.getName(), entry.getType() == Entry.TypeEnum.DIR ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), attributes(entry)));
                });
                listener.chunk(directory, list);
                return list;
            }
            catch(ApiException e) {
                throw new BackgroundException("Listing directory failed", e);
            }
        }

        private DefaultPathAttributes attributes(final Entry entry) {
            final DefaultPathAttributes attributes = new DefaultPathAttributes();
            attributes.setFileId(entry.getId());
            if(entry.getType() == Entry.TypeEnum.FILE) {
                attributes.setSize(Long.parseLong(entry.getSize()));
            }
            else {
                attributes.setSize(0L);
            }
            attributes.setModificationDate(entry.getDateCreated().getMillis()); // modified?
            return attributes;
        }
    }
}
