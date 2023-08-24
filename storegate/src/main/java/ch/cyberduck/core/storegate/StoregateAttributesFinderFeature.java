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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;

import org.apache.commons.lang3.StringUtils;

public class StoregateAttributesFinderFeature implements AttributesFinder, AttributesAdapter<File> {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateAttributesFinderFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final PathContainerService service = new DefaultPathContainerService();
            if(service.isContainer(file)) {
                for(RootFolder r : session.roots()) {
                    if(StringUtils.equalsIgnoreCase(file.getName(), PathNormalizer.name(r.getPath()))
                            || StringUtils.equalsIgnoreCase(file.getName(), PathNormalizer.name(r.getName()))) {
                        return this.toAttributes(r);
                    }
                }
                throw new NotfoundException(file.getAbsolute());
            }
            final FilesApi files = new FilesApi(session.getClient());
            return this.toAttributes(files.filesGet_0(URIEncoder.encode(fileid.getPrefixedPath(file))));
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final File f) {
        final PathAttributes attrs = new PathAttributes();
        if(0 != f.getModified().getMillis()) {
            attrs.setModificationDate(f.getModified().getMillis());
        }
        else {
            attrs.setModificationDate(f.getUploaded().getMillis());
        }
        if(0 != f.getCreated().getMillis()) {
            attrs.setCreationDate(f.getCreated().getMillis());
        }
        else {
            attrs.setCreationDate(f.getUploaded().getMillis());
        }
        if(f.getSize() != null) {
            attrs.setSize(f.getSize());
        }
        if(f.getFlags() != null) {
            if((f.getFlags() & 4) == 4) {
                // This item is locked by some user
                attrs.setLockId(Boolean.TRUE.toString());
            }
            if((f.getFlags() & 512) == 512) {
                // This item is hidden
                attrs.setHidden(true);
            }
        }
        if(f.getPermission() != null) {
            // NoAccess	0
            // ReadOnly	 1
            // ReadWrite 2
            // Synchronize	4	Read, write access and permission to syncronize using desktop client.
            // FullControl 99
            final Permission permission;
            if((f.getPermission() & 2) == 2 || (f.getPermission() & 4) == 4) {
                permission = new Permission(Permission.Action.read_write, Permission.Action.none, Permission.Action.none);
            }
            else {
                permission = new Permission(Permission.Action.read, Permission.Action.none, Permission.Action.none);
            }
            if((f.getFlags() & 1) == 1) {
                // This item is a folder
                permission.setUser(permission.getUser().or(Permission.Action.execute));
            }
            attrs.setPermission(permission);
        }
        attrs.setFileId(f.getId());
        return attrs;
    }

    public PathAttributes toAttributes(final RootFolder f) {
        final PathAttributes attrs = new PathAttributes();
        if(0 != f.getModified().getMillis()) {
            attrs.setModificationDate(f.getModified().getMillis());
        }
        else {
            attrs.setModificationDate(f.getUploaded().getMillis());
        }
        if(0 != f.getCreated().getMillis()) {
            attrs.setCreationDate(f.getCreated().getMillis());
        }
        else {
            attrs.setCreationDate(f.getUploaded().getMillis());
        }
        attrs.setSize(f.getSize());
        if((f.getFlags() & 4) == 4) {
            // This item is locked by some user
            attrs.setLockId(Boolean.TRUE.toString());
        }
        if((f.getFlags() & 512) == 512) {
            // This item is hidden
            attrs.setHidden(true);
        }
        attrs.setFileId(f.getId());
        return attrs;
    }
}
