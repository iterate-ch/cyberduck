package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.FileIdProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CteraFileIdProvider extends CachingFileIdProvider implements FileIdProvider {

    private static final Logger log = LogManager.getLogger(CteraFileIdProvider.class);

    private final CteraSession session;

    public CteraFileIdProvider(final CteraSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getFileId())) {
            return file.attributes().getFileId();
        }
        final String cached = super.getFileId(file);
        if(cached != null) {
            log.debug("Return cached fileid {} for file {}", cached, file);
            return cached;
        }
        final PathAttributes attr = session._getFeature(AttributesFinder.class).find(file);
        final String id = attr.getCustom().get(CteraAttributesFinderFeature.CTERA_FILEID);
        if(StringUtils.isBlank(id)) {
            throw new NotfoundException(file.getAbsolute());
        }
        this.cache(file, id);
        return id;
    }
}
