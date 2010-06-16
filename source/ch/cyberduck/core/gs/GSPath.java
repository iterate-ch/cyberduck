package ch.cyberduck.core.gs;

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
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;

import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.GroupGrantee;

/**
 * @version $Id$
 */
public class GSPath extends S3Path {

    static {
        PathFactory.addFactory(Protocol.GOOGLESTORAGE_SSL, new Factory());
    }

    public static class Factory extends PathFactory<GSSession> {
        @Override
        protected Path create(GSSession session, String path, int type) {
            return new GSPath(session, path, type);
        }

        @Override
        protected Path create(GSSession session, String parent, String name, int type) {
            return new GSPath(session, parent, name, type);
        }

        @Override
        protected Path create(GSSession session, Path path, Local file) {
            return new GSPath(session, path, file);
        }

        @Override
        protected <T> Path create(GSSession session, T dict) {
            return new GSPath(session, dict);
        }
    }

    protected GSPath(S3Session s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected GSPath(S3Session s, String path, int type) {
        super(s, path, type);
    }

    protected GSPath(S3Session s, Path parent, Local file) {
        super(s, parent, file);
    }

    protected <T> GSPath(S3Session s, T dict) {
        super(s, dict);
    }

    /**
     * This creates an URL that uses Cookie-based Authentication. The ACLs for the given Google user account
     * has to be setup first before.
     * <p/>
     * Google Storage lets you provide browser-based authenticated downloads to users who do not have
     * Google Storage accounts. To do this, you apply Google account-based ACLs to the object and then
     * you provide users with a URL that is scoped to the object.
     *
     * @return
     */
    @Override
    public String createSignedUrl() {
        return "https://sandbox.google.com/storage" + this.getAbsolute();
    }

    /**
     * Torrent links are not supported.
     *
     * @return Always null.
     */
    @Override
    public String createTorrentUrl() {
        return null;
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
     * @param perm The permissions to apply
     * @param acl  The ACL to update
     */
    protected void updateAccessControlList(Permission perm, AccessControlList acl) {
        acl.revokeAllPermissions(GroupGrantee.ALL_USERS);
        if(perm.getOtherPermissions()[Permission.READ]) {
            acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_READ);
        }
        if(perm.getOtherPermissions()[Permission.WRITE]) {
            if(this.isContainer()) {
                acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_READ);
                acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_WRITE);
            }
        }
    }
}
