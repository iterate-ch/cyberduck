package ch.cyberduck.core.gmxcloud;

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
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceAliasApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Children;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class GmxcloudIdProvider implements FileIdProvider {
    private static final Logger log = Logger.getLogger(GmxcloudIdProvider.class);

    private final GmxcloudSession session;
    private final LRUCache<SimplePathPredicate, String> cache = LRUCache.build(PreferencesFactory.get().getLong("fileid.cache.size"));

    public GmxcloudIdProvider(final GmxcloudSession session) {
        this.session = session;
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
            final String[] paths = file.getAbsolute().split("/");
            UiFsModel response = new ListResourceAliasApi(new GmxcloudApiClient(session)).resourceAliasAliasGet("ROOT",
                null, null, null, null, null, null, null, null);
            String id = getResourceId(paths[1], response);
            if(id != null) {
                for(int i = 2; i < paths.length; i++) {
                    id = this.getResourceId(id, paths[i]);
                }
                this.cache(file, id);
            }
            return id;
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Failure to read attributes of {0}", e, file);
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

    private String getResourceId(String resourceId, String path) throws ApiException {
        final UiFsModel uiFsModel = new ListResourceApi(new GmxcloudApiClient(session)).resourceResourceIdGet(resourceId,
            null, null, null, null, null, null, null, null);
        return getResourceId(path, uiFsModel);

    }

    private String getResourceId(final String path, final UiFsModel uiFsModel) {
        for(Children child : uiFsModel.getUifs().getChildren()) {
            if(child.getUifs().getName().equalsIgnoreCase(path)) {
                return Util.getResourceIdFromResourceUri(child.getUifs().getResourceURI());
            }
        }
        return null;
    }

}
