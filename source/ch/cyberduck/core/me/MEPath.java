package ch.cyberduck.core.me;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.davs.DAVSPath;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

/**
 * @version $Id$
 */
public class MEPath extends DAVSPath {

    static {
        PathFactory.addFactory(Protocol.IDISK, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new MEPath((MESession) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new MEPath((MESession) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new MEPath((MESession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new MEPath((MESession) session, dict);
        }
    }

    protected MEPath(MESession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected MEPath(MESession s, String path, int type) {
        super(s, path, type);
    }

    protected MEPath(MESession s, String parent, Local file) {
        super(s, parent, file);
    }

    protected MEPath(MESession s, NSDictionary dict) {
        super(s, dict);
    }

    /**
     * The "Sites" folder of a MobileMe iDisk contains the files created by HomePage, the MobileMe online web authoring tool.
     * The "Web" folder of a MobileMe iDisk contains the files created by iWeb, part of the iLife suite.
     * 
     * Custom Web URL handling
     * @return A URL to either <code>homepage.mac.com</code> or <code>web.me.com</code>
     */
    public String toHttpURL() {
        final String member = this.getHost().getCredentials().getUsername();

        // Maps to http://homepage.mac.com/<member>
        final String homepage = Path.DELIMITER + member + Path.DELIMITER + "Sites";
        // Maps to http://web.mac.com/<member>
        final String sites = Path.DELIMITER + member + Path.DELIMITER + "Web/Sites";

        final String gallery = Path.DELIMITER + member + Path.DELIMITER + "Web/Sites/_gallery";

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