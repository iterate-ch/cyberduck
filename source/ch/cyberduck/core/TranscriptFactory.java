package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class TranscriptFactory {
    private static Logger log = Logger.getLogger(TranscriptFactory.class);

    private static Map transcripts = new HashMap();

    public static void addImpl(String host, Transcript impl) {
        transcripts.put(host, impl);
    }

    public static Transcript getImpl(String host) {
        Transcript impl = (Transcript) transcripts.get(host);
        if (null == impl) {
            return new DefaultTranscript();
        }
        return impl;
    }

    private static class DefaultTranscript implements Transcript {
        public void log(String message) {
            log.info(message);
        }
    }
}