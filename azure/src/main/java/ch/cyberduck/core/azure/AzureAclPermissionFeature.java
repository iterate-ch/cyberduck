package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.shared.DefaultAclFeature;

import org.jets3t.service.acl.Permission;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

/**
 * By default, a container and any blobs within it may be accessed only by the owner of the storage account.
 * If you want to give anonymous users read permissions to a container and its blobs, you can set the container
 * permissions to allow public access. Anonymous users can read blobs within a publicly accessible container without authenticating the request.
 * Containers provide the following options for managing container access:
 * <p/>
 * Full public read access: Container and blob data can be read via anonymous request. Clients can enumerate
 * blobs within the container via anonymous request, but cannot enumerate containers within the storage account.
 * <p/>
 * Public read access for blobs only: Blob data within this container can be read via anonymous request, but
 * container data is not available. Clients cannot enumerate blobs within the container via anonymous request.
 * <p/>
 * No public read access: Container and blob data can be read by the account owner only.
 */
public class AzureAclPermissionFeature extends DefaultAclFeature implements AclPermission {

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    public AzureAclPermissionFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Collections.singletonList(
                new Acl.Role(Permission.PERMISSION_READ.toString()));
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return new ArrayList<Acl.User>(Collections.singletonList(
                new Acl.GroupUser(Acl.GroupUser.EVERYONE, false))
        );
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final CloudBlobContainer container = session.getClient()
                        .getContainerReference(containerService.getContainer(file).getName());
                final BlobContainerPermissions permissions = container.downloadPermissions(null, null, context);
                final Acl acl = new Acl();
                if(permissions.getPublicAccess().equals(BlobContainerPublicAccessType.BLOB)
                        || permissions.getPublicAccess().equals(BlobContainerPublicAccessType.CONTAINER)) {
                    acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE, false), new Acl.Role(Acl.Role.READ));
                }
                return acl;
            }
            return Acl.EMPTY;
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final CloudBlobContainer container = session.getClient()
                        .getContainerReference(containerService.getContainer(file).getName());
                final BlobContainerPermissions permissions = container.downloadPermissions(null, null, context);
                for(Acl.UserAndRole userAndRole : acl.asList()) {
                    if(userAndRole.getUser() instanceof Acl.GroupUser) {
                        if(userAndRole.getUser().getIdentifier().equals(Acl.GroupUser.EVERYONE)) {
                            permissions.setPublicAccess(BlobContainerPublicAccessType.BLOB);
                        }
                    }
                }
                container.uploadPermissions(permissions, null, null, context);
            }
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot change permissions of {0}", e, file);
        }
    }
}
