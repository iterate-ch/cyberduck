package ch.cyberduck.core.dav.microsoft;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.exception.NotfoundException;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import com.github.sardine.DavResource;
import com.github.sardine.util.SardineUtil;

public class MicrosoftIISDAVTimestampFeature extends DAVTimestampFeature {

    private static final String MS_NAMESPACE_URI = "urn:schemas-microsoft-com:";
    private static final String MS_NAMESPACE_PREFIX = "Z";
    private static final String MS_NAMESPACE_LASTMODIFIED = "Win32LastModifiedTime";

    public static final QName LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE = new QName(MS_NAMESPACE_URI, MS_NAMESPACE_LASTMODIFIED, MS_NAMESPACE_PREFIX);

    private final DAVSession session;

    public MicrosoftIISDAVTimestampFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    @Override
    protected DavResource getResource(final Path file) throws NotfoundException, IOException {
        final Optional<DavResource> optional = new MicrosoftIISDAVAttributesFinderFeature(session).list(file).stream().findFirst();
        if(!optional.isPresent()) {
            throw new NotfoundException(file.getAbsolute());
        }
        return optional.get();
    }

    protected List<Element> getCustomProperties(final DavResource resource, final Long modified) {
        final List<Element> props = new ArrayList<>();
        final Element element = SardineUtil.createElement(LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE);
        element.setTextContent(new RFC1123DateFormatter().format(modified, TimeZone.getTimeZone("GMT")));
        props.add(element);
        return props;
    }
}
