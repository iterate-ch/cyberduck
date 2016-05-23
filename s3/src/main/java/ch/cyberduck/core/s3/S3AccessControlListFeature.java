package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Versioning;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.EmailAddressGrantee;
import org.jets3t.service.acl.GrantAndPermission;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.model.S3Owner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class S3AccessControlListFeature implements AclPermission {
    private static final Logger log = Logger.getLogger(S3AccessControlListFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    public S3AccessControlListFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                // This method can be performed by anonymous services, but can only succeed if the
                // bucket's existing ACL already allows write access by the anonymous user.
                // In general, you can only access the ACL of a bucket if the ACL already in place
                // for that bucket (in S3) allows you to do so.
                return this.convert(session.getClient().getBucketAcl(containerService.getContainer(file).getName()));
            }
            else if(file.isFile() || file.isPlaceholder()) {
                org.jets3t.service.acl.AccessControlList list;
                final Versioning feature = session.getFeature(Versioning.class);
                if(feature != null && feature.getConfiguration(containerService.getContainer(file)).isEnabled()) {
                    return this.convert(session.getClient().getVersionedObjectAcl(file.attributes().getVersionId(),
                            containerService.getContainer(file).getName(), containerService.getKey(file)));
                }
                else {
                    // This method can be performed by anonymous services, but can only succeed if the
                    // object's existing ACL already allows read access by the anonymous user.
                    return this.convert(session.getClient().getObjectAcl(
                            containerService.getContainer(file).getName(), containerService.getKey(file)));
                }
            }
            return Acl.EMPTY;
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            catch(AccessDeniedException l) {
                log.warn(String.format("Missing permission to read ACL for %s %s", file, e.getMessage()));
                return Acl.EMPTY;
            }
        }
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        try {
            final Path container = containerService.getContainer(file);
            if(null == acl.getOwner()) {
                // Read owner from cache
                acl.setOwner(file.attributes().getAcl().getOwner());
            }
            if(null == acl.getOwner()) {
                // Read owner from bucket
                acl.setOwner(this.getPermission(container).getOwner());
            }
            if(containerService.isContainer(file)) {
                session.getClient().putBucketAcl(container.getName(), this.convert(acl));
            }
            else {
                if(file.isFile() || file.isPlaceholder()) {
                    session.getClient().putObjectAcl(container.getName(), containerService.getKey(file), this.convert(acl));
                }
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot change permissions of {0}", e, file);
        }
    }

    /**
     * Convert ACL for writing to service.
     *
     * @param acl Edited ACL
     * @return ACL to write to server
     */
    protected AccessControlList convert(final Acl acl) {
        final AccessControlList list = new AccessControlList();
        list.setOwner(new S3Owner(acl.getOwner().getIdentifier(), acl.getOwner().getDisplayName()));
        list.grantPermission(new CanonicalGrantee(acl.getOwner().getIdentifier()), Permission.PERMISSION_FULL_CONTROL);
        for(Acl.UserAndRole userAndRole : acl.asList()) {
            if(!userAndRole.isValid()) {
                continue;
            }
            if(userAndRole.getUser() instanceof Acl.EmailUser) {
                list.grantPermission(new EmailAddressGrantee(userAndRole.getUser().getIdentifier()),
                        Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else if(userAndRole.getUser() instanceof Acl.GroupUser) {
                if(userAndRole.getUser().getIdentifier().equals(GroupGrantee.ALL_USERS.getIdentifier())
                        || userAndRole.getUser().getIdentifier().equals(Acl.GroupUser.EVERYONE)) {
                    list.grantPermission(GroupGrantee.ALL_USERS,
                            Permission.parsePermission(userAndRole.getRole().getName()));
                }
                else if(userAndRole.getUser().getIdentifier().equals(Acl.GroupUser.AUTHENTICATED)) {
                    list.grantPermission(GroupGrantee.AUTHENTICATED_USERS,
                            Permission.parsePermission(userAndRole.getRole().getName()));
                }
                else {
                    list.grantPermission(new GroupGrantee(userAndRole.getUser().getIdentifier()),
                            Permission.parsePermission(userAndRole.getRole().getName()));
                }
            }
            else if(userAndRole.getUser() instanceof Acl.CanonicalUser) {
                list.grantPermission(new CanonicalGrantee(userAndRole.getUser().getIdentifier()),
                        Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else {
                log.warn(String.format("Unsupported user %s", userAndRole.getUser()));
            }
        }
        if(log.isDebugEnabled()) {
            try {
                log.debug(list.toXml());
            }
            catch(ServiceException e) {
                log.error(e.getMessage());
            }
        }
        return list;
    }

    /**
     * @param list ACL from server
     * @return Editable ACL
     */
    protected Acl convert(final AccessControlList list) {
        if(log.isDebugEnabled()) {
            try {
                log.debug(list.toXml());
            }
            catch(ServiceException e) {
                log.error(e.getMessage());
            }
        }
        Acl acl = new Acl();
        acl.setOwner(new Acl.CanonicalUser(list.getOwner().getId(), list.getOwner().getDisplayName()));
        for(GrantAndPermission grant : list.getGrantAndPermissions()) {
            Acl.Role role = new Acl.Role(grant.getPermission().toString());
            if(grant.getGrantee() instanceof CanonicalGrantee) {
                acl.addAll(new Acl.CanonicalUser(grant.getGrantee().getIdentifier(),
                        ((CanonicalGrantee) grant.getGrantee()).getDisplayName(), false), role);
            }
            else if(grant.getGrantee() instanceof EmailAddressGrantee) {
                acl.addAll(new Acl.EmailUser(grant.getGrantee().getIdentifier()), role);
            }
            else if(grant.getGrantee() instanceof GroupGrantee) {
                acl.addAll(new Acl.GroupUser(grant.getGrantee().getIdentifier()), role);
            }
        }
        return acl;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Arrays.asList(new Acl.Role(Permission.PERMISSION_FULL_CONTROL.toString()),
                new Acl.Role(Permission.PERMISSION_READ.toString()),
                new Acl.Role(Permission.PERMISSION_WRITE.toString()),
                new Acl.Role(Permission.PERMISSION_READ_ACP.toString()),
                new Acl.Role(Permission.PERMISSION_WRITE_ACP.toString()));
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return new ArrayList<Acl.User>(Arrays.asList(
                new Acl.CanonicalUser(),
                new Acl.GroupUser(Acl.GroupUser.EVERYONE, false),
                new Acl.EmailUser() {
                    @Override
                    public String getPlaceholder() {
                        return LocaleFactory.localizedString("Amazon Customer Email Address", "S3");
                    }
                })
        );
    }
}
