package ch.cyberduck.core.brick;

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

import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.dav.DAVTimestampFeature;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;

import com.github.sardine.DavResource;

public class BrickTimestampFeature extends DAVTimestampFeature {

    private static final String MS_NAMESPACE_URI = "http://brickftp.com/ns";
    private static final String MS_NAMESPACE_PREFIX = "s";
    private static final String MS_NAMESPACE_LASTMODIFIED = "provided_mtime";

    public static final QName LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE = new QName(MS_NAMESPACE_URI, MS_NAMESPACE_LASTMODIFIED, MS_NAMESPACE_PREFIX);

    public BrickTimestampFeature(final BrickSession session) {
        super(session);
    }

    protected Map<QName, String> getCustomProperties(final DavResource resource, final Long modified) {
        return Collections.singletonMap(LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE, new RFC1123DateFormatter().format(modified, TimeZone.getTimeZone("GMT")));
    }
}
