package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

import com.joyent.manta.client.MantaObject;

public final class MantaObjectAttributeAdapter {
    private static final Logger log = Logger.getLogger(MantaObjectAttributeAdapter.class);

    private final MantaSession session;

    private static final String HEADER_KEY_STORAGE_CLASS = "Durability-Level";

    public MantaObjectAttributeAdapter(MantaSession session) {
        this.session = session;
    }

    public PathAttributes convert(final MantaObject object) {
        final PathAttributes attributes = new PathAttributes();
        populateGenericAttributes(object, attributes);

        if(object.isDirectory()) {
            return attributes;
        }

        if(session.isWorldReadable(object)) {
            populateLinkAttribute(attributes, object);
        }

        attributes.setSize(object.getContentLength());
        attributes.setETag(object.getEtag());

        final byte[] md5Bytes = object.getMd5Bytes();

        if(md5Bytes != null) {
            attributes.setChecksum(new Checksum(HashAlgorithm.md5, new String(md5Bytes)));
        }

        attributes.setStorageClass(object.getHeaderAsString(HEADER_KEY_STORAGE_CLASS));

        return attributes;
    }

    private void populateGenericAttributes(final MantaObject object, final PathAttributes attributes) {
        final Permission.Action userPermissions =
            session.isUserWritable(object)
                ? Permission.Action.all
                : Permission.Action.read;
        final Permission.Action otherPermissions =
            session.isWorldReadable(object)
                ? Permission.Action.read
                : Permission.Action.none;
        attributes.setPermission(new Permission(userPermissions, Permission.Action.none, otherPermissions));
        if(object.getLastModifiedTime() != null) {
            attributes.setModificationDate(object.getLastModifiedTime().getTime());
            attributes.setCreationDate(attributes.getModificationDate());
        }
    }


    private void populateLinkAttribute(final PathAttributes attributes, final MantaObject object) {
        // mantaObject.getPath() starts with /
        final String joinedPath = session.getHost().getDefaultWebURL() + object.getPath();

        try {
            final URI link = new URI(joinedPath);
            attributes.setLink(new DescriptiveUrl(link, DescriptiveUrl.Type.http));
        }
        catch(URISyntaxException e) {
            log.warn(String.format("Cannot set link. Web URL returned %s", joinedPath), e);
        }
    }
}
