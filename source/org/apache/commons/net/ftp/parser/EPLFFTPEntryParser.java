package org.apache.commons.net.ftp.parser;

/*
 *  Copyright (c) 2004 Malte Tancred. All rights reserved.
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
 *  malte@tancred.com
 */

import java.util.Hashtable;

import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;

public class EPLFFTPEntryParser extends FTPFileEntryParserImpl {

    public Path parseFTPEntry(Path parent, String entry) {
        if (!entry.startsWith("+")) {
            return null;
        }

        Path f = PathFactory.createPath(parent.getSession());

        int indexOfTab = entry.indexOf("\t");
        if (indexOfTab == -1) {
            return null;
        }
        // parse name.
        int startName = indexOfTab + 1;
        String name = entry.substring(startName);
        if (name.endsWith("\r\n")) {
            int i = name.lastIndexOf("\r\n");
            name = name.substring(0, i);
        }
        if (null == name || name.equals("") || name.equals(".") || name.equals("..")) {
            return null;
        }
        f.setPath(parent.getAbsolute(), name);
        
        // parse facts.
        int i;
        int endFacts = startName - 2; // first char of name -> tab -> end of last fact.
        EPLFEntryParserContext factContext = new EPLFEntryParserContext(f);
        for (i = 1; i < endFacts; i++) {
            int factEnd = entry.indexOf(",", i);
            String fact = entry.substring(i, factEnd);
            factContext.handleFact(fact);
            i = factEnd;
        }
        factContext.conclude();

        if (!factContext.hasMayBeRetreivedFact() && !factContext.hasMayCWDToFact()) {
            return null;
        }
        return f;
    }

    private static class EPLFEntryParserContext {
        private Hashtable facts;
        private Path path = null;

        public EPLFEntryParserContext(Path aPath) {
            super();
            this.facts = new Hashtable();
            this.path = aPath;
        }

        protected boolean hasSpecifiedPermissionsFact() {
            return hasFact("up");
        }

        protected boolean hasMayBeRetreivedFact() {
            return hasFact("r");
        }

        protected boolean hasMayCWDToFact() {
            return hasFact("/");
        }

        private boolean hasFact(String factId) {
            if (facts.containsKey(factId)) {
                return true;
            }
            else {
                return false;
            }
        }

        protected void handleFact(String fact) {
            if (fact.length() == 0) {
                return;
            }

            // readable file
            if (fact.charAt(0) == 'r') {
                facts.put("r", "");
                return;
            }

            // readable directory
            if (fact.charAt(0) == '/') {
                facts.put("/", "");
                return;
            }

            // specified permissions
            if (fact.startsWith("up")) {
                facts.put("up", fact.substring(2));
                return;
            }

            // size fact
            if (fact.charAt(0) == 's') {
                String sizeString = fact.substring(1);
                facts.put("s", sizeString);
                try {
                    Long size = Long.valueOf(sizeString);
                    path.setSize(size.longValue());
				}
				catch (NumberFormatException e) {
					// intentionally do nothing
				}
                return;
            }
            
            // modification time fact
            if (fact.charAt(0) == 'm') {
                String timeString = fact.substring(1);
                facts.put("m", timeString);
                long secsSince1970 = 0;
                try {
                    Long stamp = Long.valueOf(timeString);
                    secsSince1970 = stamp.longValue();
                    path.attributes.setTimestamp((long)(secsSince1970 * 1000));
                }
                catch (NumberFormatException ignored) {
                }
                return;
            }
        }

        protected void conclude() {
            if (hasMayCWDToFact()) {
                path.attributes.setType(Path.DIRECTORY_TYPE);
            }
            else if (hasMayBeRetreivedFact()) {
                path.attributes.setType(Path.FILE_TYPE);
            }
            if (hasSpecifiedPermissionsFact()) {
                createAndSetSpecifiedPermission();
            }
        }

        private void createAndSetSpecifiedPermission() {
            Permission newPermission = createSpecifiedPermission();
            if (newPermission != null) {
                path.attributes.setPermission(newPermission);
            }
        }

        private Permission createSpecifiedPermission() {
            try {
                int perm = Integer.valueOf((String)facts.get("up"), 8).intValue();
                return new Permission(perm);
            }
            catch (NumberFormatException ignored) {
            }
            return null;
        }
    }
}
