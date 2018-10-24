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
        Path previous = file;
        Path parent = file.getParent();
        while(!parent.isRoot()) {
            if(SharepointListService.DEFAULT_NAME.equals(parent.getParent())) {
                return parent;
            }
            else if(SharepointListService.GROUPS_NAME.equals(parent.getParent())) {
                return previous;
            }
            previous = parent;
            parent = parent.getParent();
        }
        return file;
    }
}
