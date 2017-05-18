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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObject;
import com.joyent.manta.exception.MantaException;

public class MantaAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(MantaAttributesFinderFeature.class);

    private final MantaSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    public MantaAttributesFinderFeature(final MantaSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            return convert(session.client.head(file.getAbsolute()));

        }
        catch(MantaException e) {
            throw new MantaExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    private PathAttributes convert(final MantaObject mantaObject) {
        final PathAttributes attributes = new PathAttributes();

        attributes.setSize(mantaObject.getContentLength());
        attributes.setETag(mantaObject.getEtag());

        attributes.setChecksum(
                new Checksum(HashAlgorithm.md5, new String(mantaObject.getMd5Bytes())));

        attributes.setModificationDate(mantaObject.getLastModifiedTime().getTime());

        if(!mantaObject.isDirectory()) {
            populateLinkAttributeFromPath(attributes, mantaObject.getPath());
        }

        String[] pathSegments = mantaObject.getPath().split(MantaClient.SEPARATOR);

        attributes.setDisplayname(pathSegments[pathSegments.length - 1]);
        attributes.setOwner(pathSegments[0]);

        attributes.setStorageClass(mantaObject.getHeaderAsString(MantaSession.HEADER_KEY_STORAGE_CLASS));

        attributes.setPermission(
                new Permission(
                        Permission.Action.all,
                        Permission.Action.none,
                        session.pathIsProtected()
                                ? Permission.Action.none
                                : Permission.Action.read
                )
        );

        // TODO: directoryId for cryptomator?
        // TODO: region?
        // TODO: metadata extras?

        return attributes;
    }

    private void populateLinkAttributeFromPath(final PathAttributes attributes, final String objectPath) {
        final String joinedPath = session.getBaseWebURL() + MantaClient.SEPARATOR + objectPath;

        // TODO: verify this urlType switch is actually what we want to do
        final DescriptiveUrl.Type urlType = session.pathIsProtected()
                ? DescriptiveUrl.Type.authenticated
                : DescriptiveUrl.Type.http;

        try {
            final URI link = new URI(joinedPath);

            attributes.setLink(new DescriptiveUrl(link, urlType));
        }
        catch(URISyntaxException e) {
            log.warn(String.format("Cannot set link. Web URL returned %s", joinedPath), e);
        }
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        // TODO: do we care about attributes caching? protocols that seem to: b2, dav, vault, default

        return this;
    }
}
