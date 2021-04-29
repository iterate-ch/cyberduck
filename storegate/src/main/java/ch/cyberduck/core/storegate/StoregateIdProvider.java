package ch.cyberduck.core.storegate;

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

import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class StoregateIdProvider implements FileIdProvider {
    private static final Logger log = Logger.getLogger(StoregateIdProvider.class);

    private final StoregateSession session;
    private final LRUCache<SimplePathPredicate, String> cache = LRUCache.build(PreferencesFactory.get().getLong("fileid.cache.size"));

    public StoregateIdProvider(final StoregateSession session) {
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
            final String id = new FilesApi(session.getClient()).filesGet_1(URIEncoder.encode(this.getPrefixedPath(file))).getId();
            this.cache(file, id);
            return id;
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map("Failure to read attributes of {0}", e, file);
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

    /**
     * Mapping of path "/Home/mduck" to "My files" Mapping of path "/Common" to "Common files"
     */
    protected String getPrefixedPath(final Path file) {
        final PathContainerService service = new DefaultPathContainerService();
        final String root = service.getContainer(file).getAbsolute();
        for(RootFolder r : session.roots()) {
            if(root.endsWith(r.getName())) {
                if(service.isContainer(file)) {
                    return r.getPath();
                }
                return String.format("%s/%s", r.getPath(), PathRelativizer.relativize(root, file.getAbsolute()));
            }
        }
        return file.getAbsolute();
    }
}
