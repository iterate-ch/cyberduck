package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RandomFileIdProvider extends CachingFileIdProvider {
    private static final Logger log = LogManager.getLogger(RandomFileIdProvider.class);

    private final RandomStringService random;

    public RandomFileIdProvider(final Protocol.Case sensitivity) {
        this(sensitivity, new AlphanumericRandomStringService(12));
    }

    public RandomFileIdProvider(final Protocol.Case sensitivity, final RandomStringService random) {
        super(sensitivity);
        this.random = random;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        final String cached = super.getFileId(file);
        if(cached != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached fileid %s for file %s", cached, file));
            }
            return cached;
        }
        return random.random();
    }
}
