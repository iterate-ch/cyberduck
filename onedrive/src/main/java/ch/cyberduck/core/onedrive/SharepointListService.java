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
import ch.cyberduck.core.CaseInsensitivePathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.onedrive.features.sharepoint.GroupDrivesListService;
import ch.cyberduck.core.onedrive.features.sharepoint.GroupListService;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.types.Site;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;

public class SharepointListService extends AbstractSharepointListService {
    static final Logger log = Logger.getLogger(SharepointListService.class);

    public static final String DEFAULT_ID = "DEFAULT_NAME";
    public static final String DRIVES_ID = "DRIVES_NAME";
    public static final String GROUPS_ID = "GROUPS_NAME";
    public static final String SITES_ID = "SITES_NAME";

    public static final Path DEFAULT_NAME = new Path("/Default", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(DEFAULT_ID));
    public static final Path DRIVES_NAME = new Path("/Drives", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(DRIVES_ID));
    public static final Path GROUPS_NAME = new Path("/Groups", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(GROUPS_ID));
    public static final Path SITES_NAME = new Path("/Sites", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(SITES_ID));

    public static final Predicate<Path> DEFAULT_PREDICATE = new CaseInsensitivePathPredicate(DEFAULT_NAME);

    private final SharepointSession session;

    public SharepointListService(final SharepointSession session, final IdProvider idProvider) {
        super(session, idProvider);
        this.session = session;
    }

    private Optional<Path> getDefault(final Path directory) {
        try {
            final Site site = Site.byId(session.getClient(), "root");
            final Site.Metadata metadata = site.getMetadata();
            final EnumSet<Path.Type> type = DEFAULT_NAME.getType().clone();
            type.add(Path.Type.symboliclink);
            final Path path = new Path(directory, DEFAULT_NAME.getName(), type, new PathAttributes(DEFAULT_NAME.attributes()));
            path.setSymlinkTarget(
                new Path(SITES_NAME, metadata.getSiteCollection().getHostname(), SITES_NAME.getType(),
                    new PathAttributes().withVersionId(metadata.getId())));
            return Optional.of(path);
        }
        catch(IOException ex) {
            log.error("Cannot get default site. Skipping.", ex);
        }
        return Optional.empty();
    }

    @Override
    AttributedList<Path> getRoot(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        getDefault(directory).ifPresent(list::add);
        addDefaultItems(list);
        listener.chunk(directory, list);
        return list;
    }

    static void addDefaultItems(final AttributedList<Path> list) throws BackgroundException {
        list.add(GROUPS_NAME);
        list.add(SITES_NAME);
    }

    static boolean isDefaultPath(final Path directory) {
        return DEFAULT_PREDICATE.test(directory);
    }

    Path findDefaultPath(final Path directory) throws BackgroundException {
        if(isDefaultPath(directory)) {
            return getDefault(directory.getParent()).orElseThrow(() -> new NotfoundException(String.format("%s not found.", directory.getAbsolute())));
        }
        return directory;
    }

    Path getDefaultSymlinkTarget(final Path directory) throws BackgroundException {
        if (directory.getSymlinkTarget() != null) {
            return directory.getSymlinkTarget();
        }
        return getDefault(directory.getParent()).orElseThrow(() -> new NotfoundException(String.format("%s not found.", directory.getAbsolute()))).getSymlinkTarget();
    }

    @Override
    boolean processList(Path directory, final ListProgressListener listener, final ProcessListResult result) throws BackgroundException {
        // check whether this has been passed by bookmark defaultpath
        directory = findDefaultPath(directory);

        final String versionId = getIdProvider().getFileid(directory, new DisabledListProgressListener());
        if(DEFAULT_ID.equals(versionId)) {
            final Path symlinkTarget = getDefaultSymlinkTarget(directory);
            return result.withChildren(new GraphDrivesListService(session).list(symlinkTarget, listener)).success();
        }
        else if(GROUPS_ID.equals(versionId)) {
            return result.withChildren(new GroupListService(session).list(directory, listener)).success();
        }
        else {
            final String parentId = getIdProvider().getFileid(directory.getParent(), new DisabledListProgressListener());
            if(GROUPS_ID.equals(parentId)) {
                return result.withChildren(new GroupDrivesListService(session).list(directory, listener)).success();
            }
        }

        return false;
    }
}
