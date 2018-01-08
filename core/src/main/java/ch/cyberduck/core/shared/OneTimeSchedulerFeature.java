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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;

public abstract class OneTimeSchedulerFeature<R> extends AbstractSchedulerFeature<R> {

    private static final Logger log = Logger.getLogger(OneTimeSchedulerFeature.class);

    private final Path file;

    public OneTimeSchedulerFeature(final Path file) {
        super(Long.MAX_VALUE);
        this.file = file;
    }

    @Override
    public R repeat(final PasswordCallback callback) {
        try {
            return this.operate(callback, file);
        }
        catch(BackgroundException e) {
            log.warn(String.format("Failure processing missing file keys. %s", e.getDetail()));
        }
        return null;
    }
}
