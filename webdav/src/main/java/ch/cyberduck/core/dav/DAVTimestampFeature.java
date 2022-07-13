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
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.util.SardineUtil;

public class DAVTimestampFeature extends DefaultTimestampFeature implements Timestamp {

    private final DAVSession session;

    public static final QName LAST_MODIFIED_DEFAULT_NAMESPACE =
        SardineUtil.createQNameWithDefaultNamespace("lastmodified");

    /**
     * Modified timestamp we want to preserve - set by Cyberduck
     */
    public static final QName LAST_MODIFIED_CUSTOM_NAMESPACE =
        SardineUtil.createQNameWithCustomNamespace("lastmodified");

    /**
     * Contains the server side timestamp at the time we have set our custom lastmodified. If this value differs from
     * the modification date on the server the resource has been modified by another user or application.
     */
    public static final QName LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE =
        SardineUtil.createQNameWithCustomNamespace("lastmodified_server");

    public DAVTimestampFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final List<DavResource> resources = session.getClient().propfind(new DAVPathEncoder().encode(file), 0,
                Collections.singleton(SardineUtil.createQNameWithDefaultNamespace("getlastmodified")));
            for(DavResource resource : resources) {
                session.getClient().patch(new DAVPathEncoder().encode(file), this.getCustomProperties(resource, status.getTimestamp()), Collections.emptyList(),
                    this.getCustomHeaders(file, status));
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

    protected List<Element> getCustomProperties(final DavResource resource, final Long modified) {
        final List<Element> props = new ArrayList<>();
        if(resource.getModified() != null) {
            Element element = SardineUtil.createElement(LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE);
            element.setTextContent(new RFC1123DateFormatter().format(resource.getModified(), TimeZone.getTimeZone("UTC")));
            props.add(element);
        }
        Element element = SardineUtil.createElement(LAST_MODIFIED_CUSTOM_NAMESPACE);
        element.setTextContent(new RFC1123DateFormatter().format(modified, TimeZone.getTimeZone("UTC")));
        props.add(element);
        return props;
    }

    protected Map<String, String> getCustomHeaders(final Path file, final TransferStatus status) {
        final Map<String, String> headers = new HashMap<>();
        if(session.getFeature(Lock.class) != null && status.getLockId() != null &&
                !new HostPreferences(session.getHost()).getBoolean("fs.lock.implementation.pseudo")) {
            headers.put(HttpHeaders.IF, String.format("(<%s>)", status.getLockId()));
        }
        return headers;
    }
}
