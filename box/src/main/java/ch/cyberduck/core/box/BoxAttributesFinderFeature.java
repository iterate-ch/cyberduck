package ch.cyberduck.core.box;

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
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.box.io.swagger.client.api.FoldersApi;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class BoxAttributesFinderFeature implements AttributesFinder, AttributesAdapter<File> {

    public static final List<String> DEFAULT_FIELDS = Arrays.asList(
            "id", "etag", "name", "size", "content_modified_at", "content_created_at", "file_version", "file_id", "sha1");

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxAttributesFinderFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            if(file.isDirectory()) {
                return this.toAttributes(new FoldersApi(new BoxApiClient(session.getClient())).getFoldersId(fileid.getFileId(file),
                        DEFAULT_FIELDS, null, null));
            }
            return this.toAttributes(new FilesApi(new BoxApiClient(session.getClient())).getFilesId(fileid.getFileId(file),
                    StringUtils.EMPTY, DEFAULT_FIELDS, null, null));
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final File f) {
        final PathAttributes attrs = new PathAttributes();
        if(null != f.getContentModifiedAt()) {
            attrs.setModificationDate(f.getContentModifiedAt().getMillis());
        }
        if(null != f.getContentCreatedAt()) {
            attrs.setCreationDate(f.getContentCreatedAt().getMillis());
        }
        if(f.getSize() != null) {
            attrs.setSize(f.getSize());
        }
        attrs.setFileId(f.getId());
        attrs.setChecksum(new Checksum(HashAlgorithm.sha1, f.getSha1()));
        attrs.setETag(f.getEtag());
        return attrs;
    }

    protected PathAttributes toAttributes(final Folder f) {
        final PathAttributes attrs = new PathAttributes();
        if(null != f.getContentModifiedAt()) {
            attrs.setModificationDate(f.getContentModifiedAt().getMillis());
        }
        if(null != f.getContentCreatedAt()) {
            attrs.setCreationDate(f.getContentCreatedAt().getMillis());
        }
        if(f.getTrashedAt() != null) {
            attrs.setHidden(true);
        }
        attrs.setSize(f.getSize());
        attrs.setFileId(f.getId());
        return attrs;
    }
}
