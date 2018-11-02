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
import ch.cyberduck.core.shared.DefaultTimestampFeature;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.util.SardineUtil;

public class DAVTimestampFeature extends DefaultTimestampFeature implements Timestamp {

    private final DAVSession session;

    public static final QName LAST_MODIFIED_DEFAULT_NAMESPACE =
        SardineUtil.createQNameWithDefaultNamespace("lastmodified");

    /*
     * Modified timestamp we want to preserve - set by Cyberduck
     */
    public static final QName LAST_MODIFIED_CUSTOM_NAMESPACE =
        SardineUtil.createQNameWithCustomNamespace("lastmodified");

    /*
     * Contains the server side timestamp at the time we have set our custom lastmodified. If this value differs
     * from the modification date on the server the resource has been modified by another user or application.
     */
    public static final QName LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE =
        SardineUtil.createQNameWithCustomNamespace("lastmodified_server");

    public DAVTimestampFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final Long modified) throws BackgroundException {
        try {
            final List<DavResource> resources = session.getClient().propfind(new DAVPathEncoder().encode(file), 1,
                Collections.singleton(SardineUtil.createQNameWithDefaultNamespace("getlastmodified")));
            for(DavResource resource : resources) {
                final HashMap<QName, String> props = new HashMap<>();
                if(resource.getModified() != null) {
                    props.put(LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE,
                        new RFC1123DateFormatter().format(resource.getModified(), TimeZone.getTimeZone("UTC")));
                }
                props.put(LAST_MODIFIED_CUSTOM_NAMESPACE,
                    new RFC1123DateFormatter().format(modified, TimeZone.getTimeZone("UTC")));

                session.getClient().patch(new DAVPathEncoder().encode(file), props);
                break;
            }
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }
}
