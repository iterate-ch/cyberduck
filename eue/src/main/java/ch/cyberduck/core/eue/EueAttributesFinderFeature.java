package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.ListResourceAliasApi;
import ch.cyberduck.core.eue.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.eue.io.swagger.client.model.UiWin32;
import ch.cyberduck.core.eue.io.swagger.client.model.Uifs;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

public class EueAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = LogManager.getLogger(EueAttributesFinderFeature.class);

    protected static final String OPTION_WIN_32_PROPS = "win32props";
    protected static final String OPTION_SHARES = "shares";
    protected static final String OPTION_DOWNLOAD = "download";

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueAttributesFinderFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final EueApiClient client = new EueApiClient(session);
            final UiFsModel response;
            final String resourceId = fileid.getFileId(file);
            switch(resourceId) {
                case EueResourceIdProvider.ROOT:
                case EueResourceIdProvider.TRASH:
                    response = new ListResourceAliasApi(client).resourceAliasAliasGet(resourceId,
                            null, file.attributes().getETag(), null, null, null, null,
                            Collections.singletonList(OPTION_WIN_32_PROPS), null);
                    break;
                default:
                    response = new ListResourceApi(client).resourceResourceIdGet(resourceId,
                            null, file.attributes().getETag(), null, null, null, null,
                            Collections.singletonList(OPTION_WIN_32_PROPS), null);
                    break;
            }
            switch(response.getUifs().getResourceType()) {
                case "aliascontainer":
                case "container":
                    if(file.isFile()) {
                        throw new NotfoundException(file.getAbsolute());
                    }
                    break;
                default:
                    if(file.isDirectory()) {
                        throw new NotfoundException(file.getAbsolute());
                    }
                    break;
            }
            final PathAttributes attr = this.toAttributes(response.getUifs(), response.getUiwin32(),
                    EueShareFeature.findShareForResource(session.userShares(), resourceId));
            if(client.getResponseHeaders().containsKey(HttpHeaders.ETAG)) {
                attr.setETag(StringUtils.remove(client.getResponseHeaders().get(HttpHeaders.ETAG).stream().findFirst().orElse(null), '"'));
            }
            return attr;
        }
        catch(ApiException e) {
            switch(e.getCode()) {
                case HttpStatus.SC_NOT_MODIFIED:
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("No changes for file %s with ETag %s", file, file.attributes().getETag()));
                    }
                    return file.attributes();
            }
            throw new EueExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    protected PathAttributes toAttributes(final Uifs entity, final UiWin32 uiwin32,
                                          final ShareCreationResponseEntity share) {
        final PathAttributes attr = new PathAttributes();
        attr.setDisplayname(entity.getName());
        // Matches ETag response header
        attr.setETag(StringUtils.remove(entity.getMetaETag(), '"'));
        switch(entity.getResourceType()) {
            case "aliascontainer":
            case "container":
                break;
            default:
                if(entity.getVersion() != null) {
                    attr.setRevision(Long.valueOf(entity.getVersion()));
                }
                break;
        }
        attr.setSize(entity.getSize());
        final String resourceId = EueResourceIdProvider.getResourceIdFromResourceUri(entity.getResourceURI());
        attr.setFileId(resourceId);
        if(null == uiwin32) {
            log.warn("Missing extended properties");
            return attr;
        }
        else {
            attr.setCreationDate(uiwin32.getCreationMillis());
            attr.setModificationDate(uiwin32.getLastModificationMillis());
            attr.setAccessedDate(uiwin32.getLastAccessMillis());
            attr.setHidden(uiwin32.isHidden());
        }
        attr.setLink(EueShareUrlProvider.toUrl(session.getHost(), share));
        return attr;
    }
}
