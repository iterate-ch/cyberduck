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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.DefaultAclFeature;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.EmailAddressGrantee;
import org.jets3t.service.acl.GrantAndPermission;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.model.StorageOwner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class S3AccessControlListFeature extends DefaultAclFeature implements AclPermission {
    private static final Logger log = Logger.getLogger(S3AccessControlListFeature.class);

    public static final Set<? extends Acl> CANNED_LIST = new LinkedHashSet<>(Arrays.asList(
        Acl.CANNED_PRIVATE,
        Acl.CANNED_PUBLIC_READ,
        Acl.CANNED_PUBLIC_READ_WRITE,
        Acl.CANNED_BUCKET_OWNER_READ,
        Acl.CANNED_BUCKET_OWNER_FULLCONTROL,
        Acl.CANNED_AUTHENTICATED_READ)
    );

    private final S3Session session;
    private final PathContainerService containerService;

    public S3AccessControlListFeature(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public Acl getDefault(final Path file, final Local local) {
        return Acl.toAcl(new HostPreferences(session.getHost()).getProperty("s3.acl.default"));
    }

    @Override
    public Acl getDefault(final EnumSet<Path.Type> type) {
        return Acl.toAcl(new HostPreferences(session.getHost()).getProperty("s3.acl.default"));
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        try {
            if(file.getType().contains(Path.Type.upload)) {
                return Acl.EMPTY;
            }
            if(containerService.isContainer(file)) {
                // This method can be performed by anonymous services, but can only succeed if the
                // bucket's existing ACL already allows write access by the anonymous user.
                // In general, you can only access the ACL of a bucket if the ACL already in place
                // for that bucket (in S3) allows you to do so.
                return this.toAcl(session.getClient().getBucketAcl(containerService.getContainer(file).getName()));
            }
            else if(file.isFile() || file.isPlaceholder()) {
                return this.toAcl(session.getClient().getVersionedObjectAcl(file.attributes().getVersionId(),
                    containerService.getContainer(file).getName(), containerService.getKey(file)));
            }
            return Acl.EMPTY;
        }
        catch(ServiceException e) {
            final BackgroundException failure = new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            if(file.isPlaceholder()) {
                if(failure instanceof NotfoundException) {
                    // No placeholder file may exist but we just have a common prefix
                    return Acl.EMPTY;
                }
            }
            if(failure instanceof InteroperabilityException) {
                // The specified method is not allowed against this resource. The case for delete markers in versioned buckets.
                return Acl.EMPTY;
            }
            throw failure;
        }
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        try {
            // Read owner from bucket
            final AccessControlList list = this.toAcl(file, acl);
            if(containerService.isContainer(file)) {
                session.getClient().putBucketAcl(containerService.getContainer(file).getName(), list);
            }
            else {
                if(file.isFile() || file.isPlaceholder()) {
                    session.getClient().putObjectAcl(containerService.getContainer(file).getName(), containerService.getKey(file), list);
                }
            }
        }
        catch(ServiceException e) {
            final BackgroundException failure = new S3ExceptionMappingService().map("Cannot change permissions of {0}", e, file);
            if(file.isPlaceholder()) {
                if(failure instanceof NotfoundException) {
                    // No placeholder file may exist but we just have a common prefix
                    return;
                }
            }
            throw failure;
        }
    }

    /**
     * Convert ACL for writing to service.
     *
     * @param file
     * @param acl  Edited ACL
     * @return ACL to write to server
     */
    protected AccessControlList toAcl(final Path file, final Acl acl) {
        if(Acl.EMPTY.equals(acl)) {
            return null;
        }
        if(Acl.CANNED_PRIVATE.equals(acl)) {
            return AccessControlList.REST_CANNED_PRIVATE;
        }
        if(Acl.CANNED_BUCKET_OWNER_FULLCONTROL.equals(acl)) {
            return new AccessControlList() {
                @Override
                public String getValueForRESTHeaderACL() {
                    return "bucket-owner-full-control";
                }
            };
        }
        if(Acl.CANNED_BUCKET_OWNER_READ.equals(acl)) {
            return new AccessControlList() {
                @Override
                public String getValueForRESTHeaderACL() {
                    return "bucket-owner-read";
                }
            };
        }
        if(Acl.CANNED_AUTHENTICATED_READ.equals(acl)) {
            return AccessControlList.REST_CANNED_AUTHENTICATED_READ;
        }
        if(Acl.CANNED_PUBLIC_READ.equals(acl)) {
            return AccessControlList.REST_CANNED_PUBLIC_READ;
        }
        if(Acl.CANNED_PUBLIC_READ_WRITE.equals(acl)) {
            return AccessControlList.REST_CANNED_PUBLIC_READ_WRITE;
        }
        final AccessControlList list = new AccessControlList();
        for(Acl.UserAndRole userAndRole : acl.asList()) {
            if(!userAndRole.isValid()) {
                continue;
            }
            if(userAndRole.getUser() instanceof Acl.Owner) {
                list.setOwner(new StorageOwner(userAndRole.getUser().getIdentifier(),
                    userAndRole.getUser().getDisplayName()));
            }
            else if(userAndRole.getUser() instanceof Acl.EmailUser) {
                list.grantPermission(new EmailAddressGrantee(userAndRole.getUser().getIdentifier()),
                    Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else if(userAndRole.getUser() instanceof Acl.GroupUser) {
                // Handle special cases
                if(userAndRole.getUser().getIdentifier().equals(Acl.GroupUser.EVERYONE)) {
                    list.grantPermission(GroupGrantee.ALL_USERS,
                        Permission.parsePermission(userAndRole.getRole().getName()));
                }
                else if(userAndRole.getUser().getIdentifier().equals(Acl.GroupUser.AUTHENTICATED)) {
                    list.grantPermission(GroupGrantee.AUTHENTICATED_USERS,
                        Permission.parsePermission(userAndRole.getRole().getName()));
                }
                else {
                    // Generic mappings
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
        return list;
    }

    /**
     * @param list ACL from server
     * @return Editable ACL
     */
    protected Acl toAcl(final AccessControlList list) {
        if(AccessControlList.REST_CANNED_PRIVATE == list) {
            return Acl.CANNED_PRIVATE;
        }
        if(AccessControlList.REST_CANNED_PUBLIC_READ == list) {
            return Acl.CANNED_PUBLIC_READ;
        }
        if(AccessControlList.REST_CANNED_PUBLIC_READ_WRITE == list) {
            return Acl.CANNED_PUBLIC_READ_WRITE;
        }
        if(AccessControlList.REST_CANNED_AUTHENTICATED_READ == list) {
            return Acl.CANNED_AUTHENTICATED_READ;
        }
        final Acl acl = new Acl(new Acl.Owner(list.getOwner().getId(), list.getOwner().getDisplayName()),
            new Acl.Role(Permission.PERMISSION_FULL_CONTROL.toString()));
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
                // Handle special cases
                if(grant.getGrantee().getIdentifier().equals(GroupGrantee.ALL_USERS.getIdentifier())) {
                    acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), role);
                }
                else if(grant.getGrantee().getIdentifier().equals(GroupGrantee.AUTHENTICATED_USERS.getIdentifier())) {
                    acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), role);
                }
                else {
                    // Generic mappings
                    acl.addAll(new Acl.GroupUser(grant.getGrantee().getIdentifier()), role);
                }
            }
        }
        return acl;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Arrays.asList(
            new Acl.Role(Permission.PERMISSION_FULL_CONTROL.toString()),
            new Acl.Role(Permission.PERMISSION_READ.toString()),
            new Acl.Role(Permission.PERMISSION_WRITE.toString()),
            new Acl.Role(Permission.PERMISSION_READ_ACP.toString()),
            new Acl.Role(Permission.PERMISSION_WRITE_ACP.toString())
        );
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return new ArrayList<>(Arrays.asList(
            new Acl.CanonicalUser(),
            new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED, false) {
                @Override
                public String getPlaceholder() {
                    return LocaleFactory.localizedString("http://acs.amazonaws.com/groups/global/AuthenticatedUsers", "S3");
                }
            },
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
