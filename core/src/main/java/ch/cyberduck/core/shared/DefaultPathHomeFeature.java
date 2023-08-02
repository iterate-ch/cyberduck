package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TildePathExpander;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class DefaultPathHomeFeature extends AbstractHomeFeature {
    private static final Logger log = LogManager.getLogger(DefaultPathHomeFeature.class);

    private final Session<?> session;

    public DefaultPathHomeFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        if(StringUtils.isNotBlank(session.getHost().getDefaultPath())) {
            if(StringUtils.startsWith(session.getHost().getDefaultPath(), Path.HOME)) {
                final Home feature = session.getFeature(Home.class);
                if(null == feature) {
                    // No native implementation
                }
                else {
                    return new Path(new TildePathExpander(feature.find()).expand(session.getHost().getDefaultPath(), Path.HOME + Path.DELIMITER), EnumSet.of(Path.Type.directory));
                }
            }
            return PathNormalizer.compose(ROOT, session.getHost().getDefaultPath());
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("No default path set for bookmark %s", session));
        }
        // No default path configured
        return null;
    }
}
