package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.CaseSensitivePathPredicate;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ListFilteringFeature {
    private static final Logger log = LogManager.getLogger(ListFilteringFeature.class);

    private final Session<?> session;

    public ListFilteringFeature(final Session<?> session) {
        this.session = session;
    }

    /**
     * @param file Query
     * @return Null if not found
     */
    protected Path search(final Path file, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = session._getFeature(ListService.class).list(file.getParent(), listener);
        // Try to match path only as the version might have changed in the meantime
        final Path found = list.find(new ListFilteringPredicate(session.getCaseSensitivity(), file));
        if(null == found) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("File %s not found in directory listing", file));
            }
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return attributes %s for file %s", found.attributes(), file));
            }
        }
        return found;
    }

    public static final class ListFilteringPredicate extends DefaultPathPredicate {
        private final Protocol.Case sensitivity;
        private final Path file;

        public ListFilteringPredicate(final Protocol.Case sensitivity, final Path file) {
            super(file);
            this.sensitivity = sensitivity;
            this.file = file;
        }

        @Override
        public boolean test(final Path f) {
            if(StringUtils.isNotBlank(file.attributes().getVersionId())
                    || StringUtils.isNotBlank(file.attributes().getFileId())) {
                // Search with specific version and region
                return super.test(f);
            }
            if(f.attributes().isDuplicate() || f.attributes().isHidden()) {
                // Filter previous versions and delete markers when searching for no specific version
                return false;
            }
            switch(sensitivity) {
                case sensitive:
                    return new CaseSensitivePathPredicate(file).test(f);
                case insensitive:
                    return new CaseInsensitivePathPredicate(file).test(f);
            }
            return false;
        }
    }
}
