package ch.cyberduck.core.gstorage;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;

import org.apache.log4j.Logger;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.GroupGrantee;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id$
 */
public class GSPath extends S3Path {
    private static Logger log = Logger.getLogger(GSPath.class);

    private static class Factory extends PathFactory<GSSession> {
        @Override
        protected Path create(GSSession session, String path, int type) {
            return new GSPath(session, path, type);
        }

        @Override
        protected Path create(GSSession session, String parent, String name, int type) {
            return new GSPath(session, parent, name, type);
        }

        @Override
        protected Path create(GSSession session, String parent, Local file) {
            return new GSPath(session, parent, file);
        }

        @Override
        protected <T> Path create(GSSession session, T dict) {
            return new GSPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
    }

    protected GSPath(S3Session s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected GSPath(S3Session s, String path, int type) {
        super(s, path, type);
    }

    protected GSPath(S3Session s, String parent, Local file) {
        super(s, parent, file);
    }

    protected <T> GSPath(S3Session s, T dict) {
        super(s, dict);
    }

    /**
     * This creates an URL that uses Cookie-based Authentication. The ACLs for the given Google user account
     * has to be setup first.
     * <p/>
     * Google Storage lets you provide browser-based authenticated downloads to users who do not have
     * Google Storage accounts. To do this, you apply Google account-based ACLs to the object and then
     * you provide users with a URL that is scoped to the object.
     *
     * @return
     */
    @Override
    public DescriptiveUrl toAuthenticatedUrl() {
        // Authenticated browser download using cookie-based Google account authentication in conjunction with ACL
        return new DescriptiveUrl("https://sandbox.google.com/storage" + this.getAbsolute());
    }

    @Override
    public DescriptiveUrl toSignedUrl() {
        return new DescriptiveUrl(null, null);
    }

    /**
     * Torrent links are not supported.
     *
     * @return Always null.
     */
    @Override
    public DescriptiveUrl toTorrentUrl() {
        return new DescriptiveUrl(null, null);
    }

    @Override
    public List<DescriptiveUrl> getUrls() {
        List<DescriptiveUrl> urls = super.getUrls();
        urls.add(new DescriptiveUrl(this.toAuthenticatedUrl().getUrl(),
                MessageFormat.format(Locale.localizedString("{0} URL"), Locale.localizedString("Authenticated"))));
        return urls;

    }

    /**
     * Google Storage lets you assign the following permissions:
     * READ
     * When applied to an object, this permission lets a user download an object. When applied to
     * a bucket, this permission lets a user list a bucket's contents.
     * WRITE
     * When applied to a bucket, this permission lets a user create objects, overwrite
     * objects, and delete objects in a bucket. This permission also lets a user list the
     * contents of a bucket. You cannot apply this permission to objects because bucket ACLs
     * control who can upload, overwrite, and delete objects. Also, you must grant READ permission
     * if you grant WRITE permission.
     * FULL_CONTROL
     * When applied to a bucket, this permission gives a user READ and WRITE permissions on the
     * bucket. It also lets a user read and write bucket ACLs. When applied to an object, this
     * permission gives a user READ permission on the object. It also lets a user read and
     * write object ACLs.
     * <p/>
     * Note: You cannot grant discrete permissions for reading or writing ACLs. To let
     * someone read and write ACLs you must grant them FULL_CONTROL permission.
     *
     * @param permission The permissions to apply
     * @return The updated access control list.
     */
    @Override
    protected AccessControlList getAccessControlList(Permission permission) throws IOException {
        final AccessControlList acl = super.getAccessControlList(permission);
        // Owner always has FULL_CONTROL
        acl.revokeAllPermissions(new CanonicalGrantee(acl.getOwner().getId()));
        // Revoke standard S3 permissions
        acl.revokeAllPermissions(GroupGrantee.ALL_USERS);
        if(permission.getOtherPermissions()[Permission.READ]) {
            acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_READ);
        }
        if(permission.getOtherPermissions()[Permission.WRITE]) {
            if(this.isContainer()) {
                acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_READ);
                acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_WRITE);
            }
        }
        return acl;
    }

//    /**
//     * Convert ACL read from service.
//     *
//     * @param list
//     * @return
//     */
//    @Override
//    protected Acl convert(final AccessControlList list) {
//        if(log.isDebugEnabled()) {
//            try {
//                log.debug(list.toXml());
//            }
//            catch(S3ServiceException e) {
//                log.error(e.getMessage());
//            }
//        }
//        Acl acl = new Acl();
//        for(GrantAndPermission grant : list.getGrantAndPermissions()) {
//            Acl.Role role = new Acl.Role(grant.getPermission().toString());
//            if(grant.getGrantee() instanceof CanonicalGrantee) {
//                acl.addAll(new Acl.CanonicalUser(grant.getGrantee().getIdentifier(),
//                        ((CanonicalGrantee) grant.getGrantee()).getDisplayName(), false), role);
//            }
//            else if(grant.getGrantee() instanceof EmailAddressGrantee) {
//                acl.addAll(new Acl.EmailUser(grant.getGrantee().getIdentifier()), role);
//            }
//            else if(grant.getGrantee() instanceof UserByEmailAddressGrantee) {
//                acl.addAll(new Acl.EmailUser(grant.getGrantee().getIdentifier()), role);
//            }
//            else if(grant.getGrantee() instanceof GroupByEmailAddressGrantee) {
//                acl.addAll(new Acl.EmailGroupUser(grant.getGrantee().getIdentifier()), role);
//            }
//            else if(grant.getGrantee() instanceof GroupByDomainGrantee) {
//                acl.addAll(new Acl.DomainUser(grant.getGrantee().getIdentifier()), role);
//            }
//            else if(grant.getGrantee() instanceof GroupGrantee) {
//                acl.addAll(new Acl.GroupUser(grant.getGrantee().getIdentifier()), role);
//            }
//        }
//        return acl;
//    }

//    @Override
//    protected AccessControlList convert(Acl acl) throws IOException {
//        AccessControlList list = new AccessControlList();
//        final S3Owner owner = this.getSession().getBucket(this.getContainerName()).getOwner();
//        list.setOwner(owner);
//        for(Acl.UserAndRole userAndRole : acl.asList()) {
//            if(!userAndRole.isValid()) {
//                continue;
//            }
//            if(userAndRole.getUser() instanceof Acl.EmailUser) {
//                list.grantPermission(new UserByEmailAddressGrantee(userAndRole.getUser().getIdentifier()),
//                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
//            }
//            else if(userAndRole.getUser() instanceof Acl.GroupUser) {
//                list.grantPermission(new GroupByIdGrantee(userAndRole.getUser().getIdentifier()),
//                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
//            }
//            else if(userAndRole.getUser() instanceof Acl.EmailGroupUser) {
//                list.grantPermission(new GroupByEmailAddressGrantee(userAndRole.getUser().getIdentifier()),
//                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
//            }
//            else if(userAndRole.getUser() instanceof Acl.DomainUser) {
//                list.grantPermission(new GroupByDomainGrantee(userAndRole.getUser().getIdentifier()),
//                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
//            }
//            else {
//                list.grantPermission(new UserByIdGrantee(userAndRole.getUser().getIdentifier()),
//                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
//            }
//        }
//        if(log.isDebugEnabled()) {
//            try {
//                log.debug(list.toXml());
//            }
//            catch(S3ServiceException e) {
//                log.error(e.getMessage());
//            }
//        }
//        return list;
//    }
}
