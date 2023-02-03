package ch.cyberduck.core;

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

import java.util.EnumSet;

public class ContainerPathKindDetector implements PathKindDetector {

    private final PathContainerService service;

    public ContainerPathKindDetector(final PathContainerService service) {
        this.service = service;
    }

    @Override
    public Path.Type detect(final String file) {
        if(service.isContainer(new Path(file, EnumSet.of(Path.Type.directory)))) {
            return Path.Type.directory;
        }
        return Path.Type.file;
    }
}
