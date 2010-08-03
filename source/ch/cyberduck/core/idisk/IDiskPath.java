package ch.cyberduck.core.idisk;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.dav.DAVPath;

/**
 * @version $Id$
 */
public class IDiskPath extends DAVPath {

    private static class Factory extends PathFactory<IDiskSession> {
        @Override
        protected Path create(IDiskSession session, String path, int type) {
            return new IDiskPath(session, path, type);
        }

        @Override
        protected Path create(IDiskSession session, String parent, String name, int type) {
            return new IDiskPath(session, parent, name, type);
        }

        @Override
        protected Path create(IDiskSession session, Path path, Local file) {
            return new IDiskPath(session, path, file);
        }

        @Override
        protected <T> Path create(IDiskSession session, T dict) {
            return new IDiskPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
    }

    protected IDiskPath(IDiskSession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected IDiskPath(IDiskSession s, String path, int type) {
        super(s, path, type);
    }

    protected IDiskPath(IDiskSession s, Path parent, Local file) {
        super(s, parent, file);
    }

    protected <T> IDiskPath(IDiskSession s, T dict) {
        super(s, dict);
    }

    /**
     * The "Sites" folder of a MobileMe iDisk contains the files created by HomePage, the MobileMe online web authoring tool.
     * The "Web" folder of a MobileMe iDisk contains the files created by iWeb, part of the iLife suite.
     * <p/>
     * Custom Web URL handling
     *
     * @return A URL to either <code>homepage.mac.com</code> or <code>web.me.com</code>
     */
    @Override
    public String toHttpURL() {
        final String member = this.getHost().getCredentials().getUsername();

        // Maps to http://homepage.mac.com/<member>
        final String homepage = String.valueOf(Path.DELIMITER) + member + String.valueOf(Path.DELIMITER) + "Sites";
        // Maps to http://web.mac.com/<member>
        final String sites = String.valueOf(Path.DELIMITER) + member + String.valueOf(Path.DELIMITER) + "Web/Sites";

        final String gallery = String.valueOf(Path.DELIMITER) + member + String.valueOf(Path.DELIMITER) + "Web/Sites/_gallery";

        String absolute = this.getAbsolute();
        if(absolute.startsWith(homepage)) {
            absolute = absolute.substring(homepage.length());
            return "http://homepage.mac.com/" + member + absolute;
        }
        if(absolute.startsWith(gallery)) {
            absolute = absolute.substring(gallery.length());
            return "http://gallery.me.com/" + member + absolute;
        }
        if(absolute.startsWith(sites)) {
            absolute = absolute.substring(sites.length());
            return "http://web.me.com/" + member + absolute;
        }
        return super.toHttpURL();
    }
}