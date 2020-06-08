package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.onedrive.features.sharepoint.GroupListService;
import ch.cyberduck.core.onedrive.features.sharepoint.GroupDrivesListService;
import ch.cyberduck.core.onedrive.features.sharepoint.SiteDrivesListService;
import ch.cyberduck.core.onedrive.features.sharepoint.SitesListService;

import java.util.EnumSet;

public class SharepointListService extends AbstractSharepointListService {

    public static final String DEFAULT_ID = "DEFAULT_NAME";
    public static final String DRIVES_ID = "DRIVES_NAME";
    public static final String GROUPS_ID = "GROUPS_NAME";
    public static final String SITES_ID = "SITES_NAME";

    public static final Path DEFAULT_NAME = new Path("/Default", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(DEFAULT_ID));
    public static final Path DRIVES_NAME = new Path("/Drives", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(DRIVES_ID));
    public static final Path GROUPS_NAME = new Path("/Groups", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(GROUPS_ID));
    public static final Path SITES_NAME = new Path("/Sites", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(SITES_ID));

    private final SharepointSession session;

    public SharepointListService(final SharepointSession session, final IdProvider idProvider) {
        super(session, idProvider);
        this.session = session;
    }

    @Override
    AttributedList<Path> getRoot(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return addDefaultItems(directory, listener);
    }

    @Override
    boolean processList(final Path directory, final ListProgressListener listener, final ProcessListResult result) throws BackgroundException {
        if(DEFAULT_ID.equals(directory.attributes().getVersionId())) {
            // TODO: Create Symlink to /Sites/<Sitename>/<Drives>/<Drive ID>
            return result.withChildren(new GraphDrivesListService(session).list(directory, listener)).success();
        }
        else if(GROUPS_ID.equals(directory.attributes().getVersionId())) {
            return result.withChildren(new GroupListService(session).list(directory, listener)).success();
        }
        else if(GROUPS_ID.equals(directory.getParent().attributes().getVersionId())) {
            return result.withChildren(new GroupDrivesListService(session).list(directory, listener)).success();
        }

        return false;
    }
}
