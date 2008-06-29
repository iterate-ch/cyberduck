package ch.cyberduck.core.ftp.parser;

/*
 *  Copyright (c) 2005 Malte Tancred. All rights reserved.
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

import ch.cyberduck.core.Permission;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

import java.util.Calendar;
import java.util.Hashtable;

/**
 * @version $Id$
 */
public class EPLFFTPEntryParser extends FTPFileEntryParserImpl {

    public FTPFile parseFTPEntry(String entry) {
        FTPFile file = new FTPFile();
        file.setRawListing(entry);

        if(!entry.startsWith("+")) {
            return null;
        }
        int indexOfTab = entry.indexOf("\t");
        if(indexOfTab == -1) {
            return null;
        }
        // parse name.
        int startName = indexOfTab + 1;
        String name = entry.substring(startName);
        if(name.endsWith("\r\n")) {
            int i = name.lastIndexOf("\r\n");
            name = name.substring(0, i);
        }
        if(null == name || name.equals("") || name.equals(".") || name.equals("..")) {
            return null;
        }
        file.setName(name);

        // parse facts.
        int i;
        int endFacts = startName - 2; // first char of name -> tab -> end of last fact.
        EPLFEntryParserContext factContext = new EPLFEntryParserContext(file);
        for(i = 1; i < endFacts; i++) {
            int factEnd = entry.indexOf(",", i);
            String fact = entry.substring(i, factEnd);
            factContext.handleFact(fact);
            i = factEnd;
        }
        factContext.conclude();

        if(!factContext.hasMayBeRetreivedFact() && !factContext.hasMayCWDToFact()) {
            return null;
        }
        return file;
    }

    private static class EPLFEntryParserContext {
        private Hashtable<String, String> facts;
        private FTPFile file = null;

        public EPLFEntryParserContext(FTPFile f) {
            super();
            this.facts = new Hashtable<String, String>();
            this.file = f;
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
            if(facts.containsKey(factId)) {
                return true;
            }
            else {
                return false;
            }
        }

        protected void handleFact(String fact) {
            if(fact.length() == 0) {
                return;
            }

            // readable file
            if(fact.charAt(0) == 'r') {
                facts.put("r", "");
                return;
            }

            // readable directory
            if(fact.charAt(0) == '/') {
                facts.put("/", "");
                return;
            }

            // specified permissions
            if(fact.startsWith("up")) {
                facts.put("up", fact.substring(2));
                return;
            }

            // size fact
            if(fact.charAt(0) == 's') {
                String sizeString = fact.substring(1);
                facts.put("s", sizeString);
                try {
                    file.setSize(Long.parseLong(sizeString));
                }
                catch(NumberFormatException e) {
                    // intentionally do nothing
                }
                return;
            }

            // modification time fact
            if(fact.charAt(0) == 'm') {
                String timeString = fact.substring(1);
                facts.put("m", timeString);
                long secsSince1970 = 0;
                try {
                    Long stamp = Long.valueOf(timeString);
                    secsSince1970 = stamp.longValue();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(secsSince1970 * 1000);
                    file.setTimestamp(calendar);
                }
                catch(NumberFormatException ignored) {
                }
                return;
            }
        }

        protected void conclude() {
            if(hasMayCWDToFact()) {
                file.setType(FTPFile.DIRECTORY_TYPE);
            }
            else if(hasMayBeRetreivedFact()) {
                file.setType(FTPFile.FILE_TYPE);
            }
            if(hasSpecifiedPermissionsFact()) {
                createAndSetSpecifiedPermission();
            }
        }

        private void createAndSetSpecifiedPermission() {
            Permission newPermission = this.createSpecifiedPermission();
            if(newPermission != null) {
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, newPermission.getOwnerPermissions()[Permission.READ]);
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, newPermission.getOwnerPermissions()[Permission.WRITE]);
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, newPermission.getOwnerPermissions()[Permission.EXECUTE]);
                file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, newPermission.getOwnerPermissions()[Permission.READ]);
                file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, newPermission.getOwnerPermissions()[Permission.WRITE]);
                file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, newPermission.getOwnerPermissions()[Permission.EXECUTE]);
                file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, newPermission.getOwnerPermissions()[Permission.READ]);
                file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, newPermission.getOwnerPermissions()[Permission.WRITE]);
                file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, newPermission.getOwnerPermissions()[Permission.EXECUTE]);
            }
        }

        private Permission createSpecifiedPermission() {
            try {
                int perm = Integer.valueOf(facts.get("up"), 8);
                return new Permission(perm);
            }
            catch(NumberFormatException ignored) {
            }
            return null;
        }
    }
}