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

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObject;

public class MantaObjectAttributeAdapter {

    private static final Logger log = Logger.getLogger(MantaAttributesFinderFeature.class);

    private final MantaSession session;

    private static final String HEADER_KEY_STORAGE_CLASS = "Durability-Level";

    public MantaObjectAttributeAdapter(MantaSession session) {
        this.session = session;
    }

    public PathAttributes from(final MantaObject mantaObject) {
        final PathAttributes attributes = new PathAttributes();

        populateGenericAttributes(mantaObject, attributes);

        if(mantaObject.isDirectory()) {
            return attributes;
        }

        if(session.isWorldReadable(mantaObject)) {
            populateLinkAttribute(attributes, mantaObject);
        }

        attributes.setSize(mantaObject.getContentLength());
        attributes.setETag(mantaObject.getEtag());

        final byte[] md5Bytes = mantaObject.getMd5Bytes();

        if(md5Bytes != null) {
            attributes.setChecksum(new Checksum(HashAlgorithm.md5, new String(md5Bytes)));
        }

        attributes.setStorageClass(mantaObject.getHeaderAsString(HEADER_KEY_STORAGE_CLASS));

        return attributes;
    }

    private void populateGenericAttributes(final MantaObject mantaObject, final PathAttributes attributes) {
        final String[] pathSegments = mantaObject.getPath().split(MantaClient.SEPARATOR);

        attributes.setDisplayname(pathSegments[pathSegments.length - 1]);
        attributes.setOwner(session.getAccountOwner());

        populatePermissionsAttribute(mantaObject, attributes);

        if (mantaObject.getLastModifiedTime() != null) {
            attributes.setModificationDate(mantaObject.getLastModifiedTime().getTime());
            attributes.setCreationDate(attributes.getModificationDate());
        }
    }

    private void populatePermissionsAttribute(final MantaObject mantaObject, final PathAttributes attributes) {
        final Permission.Action userPermissions =
                session.isUserWritable(mantaObject)
                        ? Permission.Action.all
                        : Permission.Action.read;
        final Permission.Action otherPermissions =
                session.isWorldReadable(mantaObject)
                        ? Permission.Action.read
                        : Permission.Action.none;

        attributes.setPermission(new Permission(userPermissions, Permission.Action.none, otherPermissions));
    }

    private void populateLinkAttribute(final PathAttributes attributes, final MantaObject mantaObject) {
        // mantaObject.getPath() starts with /
        final String joinedPath = session.getHost().getDefaultWebURL() + mantaObject.getPath();

        try {
            final URI link = new URI(joinedPath);
            attributes.setLink(new DescriptiveUrl(link, DescriptiveUrl.Type.http));
        }
        catch(URISyntaxException e) {
            log.warn(String.format("Cannot set link. Web URL returned %s", joinedPath), e);
        }
    }
}
