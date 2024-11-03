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

import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoregateIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(StoregateIdProvider.class);

    private final StoregateSession session;

    public StoregateIdProvider(final StoregateSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        try {
            if(StringUtils.isNotBlank(file.attributes().getFileId())) {
                return file.attributes().getFileId();
            }
            final String cached = super.getFileId(file);
            if(cached != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Return cached fileid {} for file {}", cached, file);
                }
                return cached;
            }
            final String id = new FilesApi(session.getClient()).filesGet_0(URIEncoder.encode(this.getPrefixedPath(file))).getId();
            this.cache(file, id);
            return id;
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(this).map("Failure to read attributes of {0}", e, file);
        }
    }

    /**
     * Mapping of path "/Home/mduck" to "My files"
     * Mapping of path "/Common" to "Common files"
     */
    protected String getPrefixedPath(final Path file) {
        final PathContainerService service = new DefaultPathContainerService();
        final String name = new DefaultPathContainerService().getContainer(file).getName();
        for(RootFolder r : session.roots()) {
            if(StringUtils.equalsIgnoreCase(name, PathNormalizer.name(r.getPath()))
                    || StringUtils.equalsIgnoreCase(name, PathNormalizer.name(r.getName()))) {
                if(service.isContainer(file)) {
                    return r.getPath();
                }
                return String.format("%s/%s", r.getPath(), PathRelativizer.relativize(name, file.getAbsolute()));
            }
        }
        return file.getAbsolute();
    }
}
