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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.s3.S3Path;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.gs.AllAuthenticatedUsersGrantee;
import org.jets3t.service.acl.gs.AllUsersGrantee;
import org.jets3t.service.acl.gs.GSAccessControlList;
import org.jets3t.service.acl.gs.GroupByDomainGrantee;
import org.jets3t.service.acl.gs.GroupByEmailAddressGrantee;
import org.jets3t.service.acl.gs.GroupByIdGrantee;
import org.jets3t.service.acl.gs.UserByEmailAddressGrantee;
import org.jets3t.service.acl.gs.UserByIdGrantee;

/**
 * @version $Id$
 */
public class GSPath extends S3Path {
    private static Logger log = Logger.getLogger(GSPath.class);

    public GSPath(GSSession s, Path parent, String name, int type) {
        super(s, parent, name, type);
    }

    public GSPath(GSSession s, String path, int type) {
        super(s, path, type);
    }

    public GSPath(GSSession s, Path parent, Local file) {
        super(s, parent, file);
    }

    public <T> GSPath(GSSession s, T dict) {
        super(s, dict);
    }

    @Override
    protected AccessControlList convert(Acl acl) {
        GSAccessControlList list = new GSAccessControlList();
        // Do not set owner for ACL which is set automatically
        for(Acl.UserAndRole userAndRole : acl.asList()) {
            if(!userAndRole.isValid()) {
                continue;
            }
            if(userAndRole.getUser() instanceof Acl.EmailUser) {
                list.grantPermission(new UserByEmailAddressGrantee(userAndRole.getUser().getIdentifier()),
                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else if(userAndRole.getUser() instanceof Acl.GroupUser) {
                if(userAndRole.getUser().getIdentifier().equals("AllUsers")) {
                    list.grantPermission(new AllUsersGrantee(),
                            org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
                }
                else if(userAndRole.getUser().getIdentifier().equals("AllAuthenticatedUsers")) {
                    list.grantPermission(new AllAuthenticatedUsersGrantee(),
                            org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
                }
                else {
                    list.grantPermission(new GroupByIdGrantee(userAndRole.getUser().getIdentifier()),
                            org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
                }
            }
            else if(userAndRole.getUser() instanceof Acl.DomainUser) {
                list.grantPermission(new GroupByDomainGrantee(userAndRole.getUser().getIdentifier()),
                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else if(userAndRole.getUser() instanceof Acl.CanonicalUser) {
                list.grantPermission(new UserByIdGrantee(userAndRole.getUser().getIdentifier()),
                        org.jets3t.service.acl.Permission.parsePermission(userAndRole.getRole().getName()));
            }
            else if(userAndRole.getUser() instanceof Acl.EmailGroupUser) {
                list.grantPermission(new GroupByEmailAddressGrantee(userAndRole.getUser().getIdentifier()),
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
}