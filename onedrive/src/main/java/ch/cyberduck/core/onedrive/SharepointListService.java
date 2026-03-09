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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.onedrive.features.sharepoint.GroupDrivesListService;
import ch.cyberduck.core.onedrive.features.sharepoint.GroupListService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;
import org.nuxeo.onedrive.client.types.Site;
import java.io.IOException;
import java.util.EnumSet;

public class SharepointListService extends AbstractSharepointListService {
    static final Logger log = LogManager.getLogger(SharepointListService.class);

    public static final String DRIVES_CONTAINER = "Drives";
    public static final String GROUPS_CONTAINER = "Groups";
    public static final String SITES_CONTAINER = "Sites";

    public static final Path DRIVES_NAME = new Path(DRIVES_CONTAINER, EnumSet.of(Path.Type.placeholder, Path.Type.directory));
    public static final Path GROUPS_NAME = new Path(GROUPS_CONTAINER, EnumSet.of(Path.Type.placeholder, Path.Type.directory));
    public static final Path SITES_NAME = new Path(SITES_CONTAINER, EnumSet.of(Path.Type.placeholder, Path.Type.directory));

    private final SharepointSession session;
    private final GraphFileIdProvider fileid;

    public SharepointListService(final SharepointSession session, final GraphFileIdProvider fileid) {
        super(session, fileid);
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    protected AttributedList<Path> getRoot(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        list.add(GROUPS_NAME);
        list.add(SITES_NAME);
        listener.chunk(directory, list);
        return list;
    }

    @Override
    protected AttributedList<Path> processList(Path directory, final ListProgressListener listener) throws BackgroundException {
        final GraphSession.ContainerItem container = session.getContainer(directory);
        if(container.isDrive()) {
            return AttributedList.emptyList();
        }

        if(container.getCollectionPath().map(p -> GROUPS_CONTAINER.equals(p.getName())).orElse(false)) {
            if(!container.isDefined()) {
                return new GroupListService(session, fileid).list(directory, listener);
            }
            else {
                return new GroupDrivesListService(session, fileid).list(directory, listener);
            }
        }
        return AttributedList.emptyList();
    }

    public Path getDefaultSite() throws BackgroundException {
        try {
            final Site.Metadata metadata = Site.byId(session.getClient(), "root").getMetadata(null);
            return list(SITES_NAME).find(path -> metadata.getId().equals(path.attributes().getFileId()));
        }
        catch(OneDriveRuntimeException e) {
            throw new GraphExceptionMappingService(fileid).map(e.getCause());
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
