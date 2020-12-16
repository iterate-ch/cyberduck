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

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

public class SharepointContainerService extends PathContainerService {
    @Override
    public boolean isContainer(final Path file) {
        return file.equals(getContainer(file));
    }

    @Override
    public Path getContainer(final Path file) {
        if(file.isRoot()) {
            return file;
        }
        final Deque<Map.Entry<String, Path>> tree = new ArrayDeque<>();
        Path next = file;
        Path current = null, previous;
        while(current != next) {
            previous = current;
            current = next;
            next = next.getParent();

            if(previous != null) {
                if(SharepointListService.GROUPS_CONTAINER.equals(next.getName())) {
                    // All Placeholders/Containers use format /Site-ID/<Name>/Drives-ID/<Name>
                    // Groups however do not, they apply /Groups/<Group ID>/<Drive Name>
                    // There is no common prefix directory for drives in groups
                    // thus doing simple forward-check
                    // i.e.
                    // * current is /Groups/Group-Name
                    // * Previous is /Groups/Group-Name/Documents
                    // * next is /Groups
                    // this will trigger Container detection for /Groups/Group-Name/Documents
                    // using DRIVES_CONTAINER as placeholder here. Will trigger early exit later.
                    tree.push(new AbstractMap.SimpleEntry<>(SharepointListService.DRIVES_CONTAINER, previous));
                }
                else {
                    switch(current.getName()) {
                        case SharepointListService.SITES_CONTAINER:
                        case SharepointListService.GROUPS_CONTAINER:
                        case SharepointListService.DRIVES_CONTAINER:
                            tree.push(new AbstractMap.SimpleEntry<>(current.getName(), previous));
                            break;
                    }
                }
            }
        }

        // walk tree, in order to find first matching Drives/Drive-Name/ or /Groups/Name/Drive-Name
        Path container = null;
        while(tree.size() > 0) {
            final Map.Entry<String, Path> element = tree.pop();
            container = element.getValue();
            if(SharepointListService.DRIVES_CONTAINER.equals(element.getKey())) {
                break;
            }
        }

        return Optional.ofNullable(container).orElse(current);
    }
}
