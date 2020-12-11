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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;

import java.util.Optional;

public class SharepointContainerService extends PathContainerService {

    @Override
    public boolean isContainer(final Path file) {
        return super.isContainer(file) || file.getType().contains(Path.Type.volume);
    }

    @Override
    public Path getContainer(final Path file) {
        if(file.isRoot()) {
            return file;
        }
        Path container = null;
        Path next = file;
        Path current = null, previous;
        while(null == container && null != next) {
            previous = current;
            current = next;
            next = !next.isRoot() ? next.getParent() : null;

            final String versionId = current.attributes().getVersionId();

            if(next != null && SharepointListService.GROUPS_ID.equals(next.attributes().getVersionId())) {
                // All Placeholders/Containers use format /Site-ID/<Name>/Drives-ID/<Name>
                // Groups however do not, they apply /Groups/<Group ID>/<Drive Name>
                // There is no common prefix directory for drives in groups
                // thus doing simple forward-check
                // i.e.
                // * current is /Groups/Group-Name
                // * Previous is /Groups/Group-Name/Documents
                // * next is /Groups
                // this will trigger Container detection for /Groups/Group-Name/Documents.

                // /Groups/Group Name/Drive
                container = previous;
            }
            else if(SharepointListService.DRIVES_ID.equals(versionId)) {
                // Drives/Drive Name
                container = previous;
            }
            else if(SharepointListService.GROUPS_ID.equals(versionId)) {
                // /Groups/Group Name
                container = previous;
            }
            else if(SharepointListService.SITES_ID.equals(versionId)) {
                // Sites/Site-Name
                container = previous;
            }
        }
        return Optional.ofNullable(container).orElse(current);
    }
}
