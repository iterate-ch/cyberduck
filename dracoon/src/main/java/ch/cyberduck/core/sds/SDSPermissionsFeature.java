package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.shared.DefaultAclFeature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SDSPermissionsFeature extends DefaultAclFeature {

    public static final Acl.Role MANAGE_ROLE = new Acl.Role("MANAGE_ROLE");
    public static final Acl.Role READ_ROLE = new Acl.Role("READ");
    public static final Acl.Role CREATE_ROLE = new Acl.Role("CREATE");
    public static final Acl.Role CHANGE_ROLE = new Acl.Role("CHANGE");
    public static final Acl.Role DELETE_ROLE = new Acl.Role("DELETE");
    public static final Acl.Role DOWNLOAD_SHARE_ROLE = new Acl.Role("DOWNLOAD_SHARE");
    public static final Acl.Role UPLOAD_SHARE_ROLE = new Acl.Role("UPLOAD_SHARE");

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSPermissionsFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        if(Acl.EMPTY.equals(file.attributes().getAcl())) {
            return new SDSAttributesFinderFeature(session, nodeid).find(file, new DisabledListProgressListener()).getAcl();
        }
        return file.attributes().getAcl();
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        try {
            return Collections.singletonList(new Acl.CanonicalUser(String.valueOf(session.userAccount().getId())));
        }
        catch(BackgroundException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Arrays.asList(
            MANAGE_ROLE,
            READ_ROLE,
            CREATE_ROLE,
            CHANGE_ROLE,
            DELETE_ROLE,
            DOWNLOAD_SHARE_ROLE,
            UPLOAD_SHARE_ROLE
        );
    }
}
