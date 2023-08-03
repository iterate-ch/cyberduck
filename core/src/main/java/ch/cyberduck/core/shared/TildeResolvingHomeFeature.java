package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.TildePathExpander;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class TildeResolvingHomeFeature implements Home {

    private final Host host;
    private final Home proxy;

    public TildeResolvingHomeFeature(final Host host, final Home proxy) {
        this.host = host;
        this.proxy = proxy;
    }

    @Override
    public Path find() throws BackgroundException {
        if(StringUtils.isNotBlank(host.getDefaultPath())) {
            if(StringUtils.startsWith(host.getDefaultPath(), Path.HOME)) {
                return new Path(new TildePathExpander(proxy.find()).expand(host.getDefaultPath(), Path.HOME + Path.DELIMITER), EnumSet.of(Path.Type.directory));
            }
            return PathNormalizer.compose(ROOT, host.getDefaultPath());
        }
        return proxy.find();
    }
}
