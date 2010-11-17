package com.dropbox.client;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 *
 * Derived from Official Dropbox API client for Java.
 * http://bitbucket.org/dropboxapi/dropbox-client-java
 */

import java.util.Map;

/**
 * @version $Id$
 */
public abstract class AbstractResponse {

    protected String getString(Map map, String name) {
        Object val = map.get(name);
        if(val instanceof String) {
            return ((String) val);
        }
        return null;
    }

    protected long getLong(Map map, String name) {
        Object val = map.get(name);
        if(val instanceof Number) {
            return ((Number) val).longValue();
        }
        return -1;
    }

    protected boolean getBoolean(Map map, String name) {
        Object val = map.get(name);
        if(val instanceof Boolean) {
            return (Boolean) val;
        }
        return false;
    }
}
