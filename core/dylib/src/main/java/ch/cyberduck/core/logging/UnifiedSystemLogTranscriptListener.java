package ch.cyberduck.core.logging;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.TranscriptListener;

public class UnifiedSystemLogTranscriptListener implements TranscriptListener {

    private final UnifiedSystemLogAppender appender = new UnifiedSystemLogAppender();

    @Override
    public void log(final Type type, final String message) {
        switch(type) {
            case request:
            case response:
                appender.log(UnifiedSystemLogAppender.OS_LOG_TYPE_INFO, "transcript", message);
                break;
        }
    }
}
