package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.ListResourceAliasApi;
import ch.cyberduck.core.eue.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.model.Children;
import ch.cyberduck.core.eue.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class EueResourceIdProvider implements FileIdProvider {

    public static final String ROOT = "ROOT";
    public static final String TRASH = "TRASH";

    private static final Logger log = Logger.getLogger(EueResourceIdProvider.class);

    private final EueSession session;
    private final LRUCache<SimplePathPredicate, String> cache = LRUCache.build(PreferencesFactory.get().getLong("fileid.cache.size"));

    public EueResourceIdProvider(final EueSession session) {
        this.session = session;
    }

    public static String getResourceIdFromResourceUri(final String uri) {
        if(StringUtils.contains(uri, Path.DELIMITER)) {
            return StringUtils.substringAfterLast(uri, Path.DELIMITER);
        }
        return uri;
    }

    @Override
    public String getFileId(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            if(StringUtils.isNotBlank(file.attributes().getFileId())) {
                return file.attributes().getFileId();
            }
            if(cache.contains(new SimplePathPredicate(file))) {
                final String cached = cache.get(new SimplePathPredicate(file));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Return cached fileid %s for file %s", cached, file));
                }
                return cached;
            }
            if(file.isRoot()) {
                return ROOT;
            }
            int offset = 0;
            UiFsModel fsModel;
            final int chunksize = new HostPreferences(session.getHost()).getInteger("eue.listing.chunksize");
            do {
                final String parentResourceId = this.getFileId(file.getParent(), listener);
                switch(parentResourceId) {
                    case EueResourceIdProvider.ROOT:
                    case EueResourceIdProvider.TRASH:
                        fsModel = new ListResourceAliasApi(new EueApiClient(session)).resourceAliasAliasGet(parentResourceId,
                                null, null, null, null, chunksize, offset, null, null);
                        break;
                    default:
                        fsModel = new ListResourceApi(new EueApiClient(session)).resourceResourceIdGet(parentResourceId,
                                null, null, null, null, chunksize, offset, null, null);
                }
                for(Children child : fsModel.getUifs().getChildren()) {
                    if(child.getUifs().getName().equalsIgnoreCase(file.getName())) {
                        return getResourceIdFromResourceUri(child.getUifs().getResourceURI());
                    }
                }
                offset += chunksize;
            }
            while(fsModel.getUifs().getChildren().size() == chunksize);
            throw new NotfoundException(file.getAbsolute());
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public String cache(final Path file, final String id) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cache %s for file %s", id, file));
        }
        if(null == id) {
            cache.remove(new SimplePathPredicate(file));
            file.attributes().setFileId(null);
        }
        else {
            cache.put(new SimplePathPredicate(file), id);
            file.attributes().setFileId(id);
        }
        return id;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
