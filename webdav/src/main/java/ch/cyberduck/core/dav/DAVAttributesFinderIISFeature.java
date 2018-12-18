package ch.cyberduck.core.dav;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.github.sardine.DavResource;

public class DAVAttributesFinderIISFeature extends DAVAttributesFinderFeature {
    private static final Logger log = Logger.getLogger(DAVAttributesFinderIISFeature.class);

    private final DAVSession session;

    public DAVAttributesFinderIISFeature(DAVSession session) {
        super(session);
        this.session = session;
    }

    protected List<DavResource> list(final Path file) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(file), 1, true);
    }

    protected PathAttributes toAttributes(final DavResource resource) {
        final PathAttributes attributes = super.toAttributes(resource);
        final Map<QName, String> properties = resource.getCustomPropsNS();
        if(null != properties && properties.containsKey(DAVTimestampIISFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE)) {
            final String value = properties.get(DAVTimestampIISFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE);
            if(StringUtils.isNotBlank(value)) {
                try {
                    attributes.setModificationDate(new RFC1123DateFormatter().parse(value).getTime());
                }
                catch(InvalidDateException e) {
                    log.warn(String.format("Failure parsing property %s with value %s", DAVTimestampIISFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE, value));
                    if(resource.getModified() != null) {
                        attributes.setModificationDate(resource.getModified().getTime());
                    }
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Missing value for property %s", DAVTimestampIISFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE));
                }
                if(resource.getModified() != null) {
                    attributes.setModificationDate(resource.getModified().getTime());
                }
            }
        }
        return attributes;
    }
}
