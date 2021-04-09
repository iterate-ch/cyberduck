package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.AbstractHomeFeature;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class LocalHomeFinderFeature extends AbstractHomeFeature {

    @Override
    public Path find() throws BackgroundException {
        return this.toPath(LocalFactory.get().getAbsolute());
    }

    protected Path toPath(final String home) {
        return new Path(StringUtils.replace(home, "\\", "/"), EnumSet.of(Path.Type.directory));
    }
}
