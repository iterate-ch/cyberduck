package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.features.AccessControlList;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.EmailAddressGrantee;
import org.jets3t.service.acl.GrantAndPermission;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.model.S3Owner;

/**
 * @version $Id$
 */
public class S3AccessControlListFeature implements AccessControlList {
    private static final Logger log = Logger.getLogger(S3AccessControlListFeature.class);

    private S3Session session;

    private PathContainerService containerService = new PathContainerService();

    public S3AccessControlListFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public Acl read(final Path file) throws BackgroundException {
        final Credentials credentials = session.getHost().getCredentials();
        if(credentials.isAnonymousLogin()) {
            return Acl.EMPTY;
        }
        try {
            if(containerService.isContainer(file)) {
                // This method can be performed by anonymous services, but can only succeed if the
                // bucket's existing ACL already allows write access by the anonymous user.
                // In general, you can only access the ACL of a bucket if the ACL already in place
                // for that bucket (in S3) allows you to do so.
                return this.convert(session.getClient().getBucketAcl(containerService.getContainer(file).getName()));
            }
            else if(file.attributes().isFile() || file.attributes().isPlaceholder()) {
                org.jets3t.service.acl.AccessControlList list;
                if(new S3VersioningFeature(session).getConfiguration(containerService.getContainer(file)).isEnabled()) {
                    list = session.getClient().getVersionedObjectAcl(file.attributes().getVersionId(),
                            containerService.getContainer(file).getName(), containerService.getKey(file));
                }
                else {
                    // This method can be performed by anonymous services, but can only succeed if the
                    // object's existing ACL already allows read access by the anonymous user.
                    list = session.getClient().getObjectAcl(containerService.getContainer(file).getName(), containerService.getKey(file));
                }
                return this.convert(list);
            }
            return Acl.EMPTY;
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read file attributes", e, file);
        }
    }

    @Override
    public void write(final Path file, final Acl acl, final boolean recursive) throws BackgroundException {
        try {
            if(null == acl.getOwner()) {
                // Owner is lost in controller
                acl.setOwner(file.attributes().getAcl().getOwner());
            }
            if(containerService.isContainer(file)) {
                session.getClient().putBucketAcl(containerService.getContainer(file).getName(), this.convert(acl));
            }
            else {
                if(file.attributes().isFile() || file.attributes().isPlaceholder()) {
                    session.getClient().putObjectAcl(containerService.getContainer(file).getName(), containerService.getKey(file), this.convert(acl));
                }
                if(file.attributes().isDirectory()) {
                    if(recursive) {
                        for(Path child : session.list(file)) {
                            this.write(child, acl, recursive);
                        }
                    }
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot change permissions", e, file);
        }
    }

    /**
     * Convert ACL for writing to service.
     *
     * @param acl Edited ACL
     * @return ACL to write to server
     */
    protected org.jets3t.service.acl.AccessControlList convert(Acl acl) {
        if(null == acl) {
            return null;
        }
        org.jets3t.service.acl.AccessControlList list = new org.jets3t.service.acl.AccessControlList();
        list.setOwner(new S3Owner(acl.getOwner().getIdentifier(), acl.getOwner().getDisplayName()));
        for(Acl.UserAndRole userAndRole : acl.asList()) {
            if(!userAndRole.isValid()) {
                continue;
            }
            if(userAndRole.getUser() instanceof Acl.EmailUser) {
                list.grantPermission(new EmailAddressGrantee(userAndRole.getUser().getIdentifier()),
                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else if(userAndRole.getUser() instanceof Acl.GroupUser) {
                list.grantPermission(new GroupGrantee(userAndRole.getUser().getIdentifier()),
                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else if(userAndRole.getUser() instanceof Acl.CanonicalUser) {
                list.grantPermission(new CanonicalGrantee(userAndRole.getUser().getIdentifier()),
                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else {
                log.warn("Unsupported user:" + userAndRole.getUser());
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
    protected Acl convert(final org.jets3t.service.acl.AccessControlList list) {
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
}
