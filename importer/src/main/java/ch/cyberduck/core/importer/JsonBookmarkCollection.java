package ch.cyberduck.core.importer;

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

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public abstract class JsonBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = Logger.getLogger(JsonBookmarkCollection.class);

    protected String readNext(final String name, final JsonReader reader) throws IOException {
        if(reader.peek() != JsonToken.NULL) {
            return reader.nextString();
        }
        else {
            reader.skipValue();
            log.warn(String.format("No value for key %s", name));
            return null;
        }
    }
}
