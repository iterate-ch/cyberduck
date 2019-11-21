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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;

import java.util.HashMap;
import java.util.Map;

public class StoregateIdProvider implements IdProvider {

    public static final String KEY_NODE_ID = "node_id";

    private final StoregateSession session;

    public StoregateIdProvider(final StoregateSession session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            if(file.attributes().getCustom().containsKey(KEY_NODE_ID)) {
                return file.attributes().getCustom().get(KEY_NODE_ID);
            }
            final String id = new FilesApi(session.getClient()).filesGet_1(URIEncoder.encode(this.getPrefixedPath(file))).getId();
            final Map<String, String> custom = new HashMap<>(file.attributes().getCustom());
            custom.put(KEY_NODE_ID, id);
            file.attributes().setCustom(custom);
            return id;
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public StoregateIdProvider withCache(final Cache<Path> cache) {
        return this;
    }

    /**
     * Mapping of path "/Home/mduck" to "My files"
     * Mapping of path "/Common" to "Common files"
     */
    protected String getPrefixedPath(final Path file) {
        final PathContainerService service = new PathContainerService();
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
