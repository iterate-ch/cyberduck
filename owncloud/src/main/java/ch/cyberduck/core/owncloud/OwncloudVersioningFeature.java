package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.dav.DAVExceptionMappingService;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.nextcloud.NextcloudHomeFeature;
import ch.cyberduck.core.nextcloud.NextcloudVersioningFeature;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.model.Propfind;

public class OwncloudVersioningFeature extends NextcloudVersioningFeature {

    private final DAVSession session;

    public OwncloudVersioningFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        try {
            session.getClient().copy(String.format("%s/%s/v/%s",
                            new OwncloudHomeFeature(session.getHost()).find(NextcloudHomeFeature.Context.versions).getAbsolute(),
                            file.attributes().getFileId(), file.attributes().getVersionId()),
                    new DAVPathEncoder().encode(file));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Cannot revert file", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }

    @Override
    protected boolean filter(final Path file, final DavResource resource) {
        if(StringUtils.equals("v", PathNormalizer.name(resource.getHref().getPath()))) {
            return false;
        }
        return super.filter(file, resource);
    }

    @Override
    protected List<DavResource> propfind(final Path file, final Propfind body) throws IOException, BackgroundException {
        return session.getClient().propfind(URIEncoder.encode(String.format("%s/%s/v",
                new OwncloudHomeFeature(session.getHost()).find(NextcloudHomeFeature.Context.versions).getAbsolute(),
                file.attributes().getFileId())), 1, body);
    }
}
