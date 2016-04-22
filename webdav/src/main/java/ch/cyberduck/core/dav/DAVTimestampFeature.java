package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.HashMap;
import java.util.TimeZone;

import com.github.sardine.impl.SardineException;
import com.github.sardine.util.SardineUtil;

public class DAVTimestampFeature implements Timestamp {

    private final DAVSession session;

    public DAVTimestampFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final Long modified) throws BackgroundException {
        try {
            final HashMap<QName, String> props = new HashMap<>();
            props.put(new QName(SardineUtil.DEFAULT_NAMESPACE_URI, "lastmodified", SardineUtil.DEFAULT_NAMESPACE_PREFIX),
                    new RFC1123DateFormatter().format(modified, TimeZone.getTimeZone("UTC")));
            session.getClient().patch(new DAVPathEncoder().encode(file), props);
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }
}
