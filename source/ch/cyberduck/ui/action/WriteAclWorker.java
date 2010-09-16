package ch.cyberduck.ui.action;

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
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class WriteAclWorker extends Worker<Acl> {
    private static Logger log = Logger.getLogger(WriteAclWorker.class);

    /**
     * Selected files.
     */
    private List<Path> files;

    /**
     * Permissions to apply to files.
     */
    private Acl acl;

    /**
     * Descend into directories
     */
    private boolean recursive;

    public WriteAclWorker(List<Path> files, Acl acl, boolean recursive) {
        this.files = files;
        this.acl = acl;
        this.recursive = recursive;
    }

    @Override
    public Acl run() {
        for(Path next : files) {
            if(!next.getSession().isConnected()) {
                break;
            }
            if(acl.equals(next.attributes().getAcl())) {
                log.info("Skip writing equal ACL for " + next);
                return acl;
            }
            next.writeAcl(acl, recursive);
        }
        return acl;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                this.toString(files), acl);
    }
}
