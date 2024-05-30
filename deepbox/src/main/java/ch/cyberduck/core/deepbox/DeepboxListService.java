package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import java.util.EnumSet;

public class DeepboxListService implements ListService {

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;
    private final int chunksize;
//    private final DeepboxAttributesFinderFeature attributes;

    public DeepboxListService(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
//        this.attributes = new DeepboxAttributesFinderFeature(session, fileid);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        try {
            final DeepBoxes boxes = new BoxRestControllerApi(this.session.getClient()).listDeepBoxes(0, 50, "asc", null);
            for(DeepBox box : boxes.getDeepBoxes()) {
                list.add(new Path(directory, PathNormalizer.name(box.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                        new PathAttributes()));
            }
        }
        catch(ApiException e) {
            throw new BackgroundException(e);
        }
        return list;
    }
}
