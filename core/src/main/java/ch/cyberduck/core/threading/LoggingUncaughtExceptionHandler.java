package ch.cyberduck.core.threading;

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

import org.apache.log4j.Logger;

public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger log = Logger.getLogger(LoggingUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        // Swallow the exception
        log.error(String.format("Thread %s has thrown uncaught exception:%s",
                t.getName(), e.getMessage()), e);
    }
}
