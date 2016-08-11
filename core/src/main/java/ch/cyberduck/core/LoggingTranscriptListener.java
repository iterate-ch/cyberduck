package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

public class LoggingTranscriptListener implements TranscriptListener {

    private static final Logger request = Logger.getLogger("Request");
    private static final Logger response = Logger.getLogger("Response");

    @Override
    public void log(final Type type, final String message) {
        switch(type) {
            case request:
                if(request.isInfoEnabled()) {
                    request.info(message);
                }
                break;
            case response:
                if(response.isInfoEnabled()) {
                    response.info(message);
                }
                break;
        }
    }
}
