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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.github.sardine.DavResource;

public class MicrosoftIISDAVAttributesFinderFeature extends DAVAttributesFinderFeature {
    private static final Logger log = LogManager.getLogger(MicrosoftIISDAVAttributesFinderFeature.class);

    private final DAVSession session;

    private final RFC1123DateFormatter rfc1123
        = new RFC1123DateFormatter();

    public MicrosoftIISDAVAttributesFinderFeature(DAVSession session) {
        super(session);
        this.session = session;
    }

    protected List<DavResource> list(final Path file) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(file), 0, true);
    }

    @Override
    public PathAttributes toAttributes(final DavResource resource) {
        final PathAttributes attributes = super.toAttributes(resource);
        final Map<QName, String> properties = resource.getCustomPropsNS();
        if(null != properties && properties.containsKey(MicrosoftIISDAVTimestampFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE)) {
            final String value = properties.get(MicrosoftIISDAVTimestampFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE);
            if(StringUtils.isNotBlank(value)) {
                try {
                    attributes.setModificationDate(rfc1123.parse(value).getTime());
                }
                catch(InvalidDateException e) {
                    log.warn("Failure parsing property {} with value {}", MicrosoftIISDAVTimestampFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE, value);
                    if(resource.getModified() != null) {
                        attributes.setModificationDate(resource.getModified().getTime());
                    }
                }
            }
            else {
                log.debug("Missing value for property {}", MicrosoftIISDAVTimestampFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE);
                if(resource.getModified() != null) {
                    attributes.setModificationDate(resource.getModified().getTime());
                }
            }
        }
        return attributes;
    }
}
