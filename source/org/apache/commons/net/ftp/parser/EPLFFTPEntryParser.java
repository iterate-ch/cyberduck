package org.apache.commons.net.ftp.parser;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;
import java.util.Hashtable;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

public class EPLFFTPEntryParser extends FTPFileEntryParserImpl {
    
    public Path parseFTPEntry(Path parent, String entry) {
        if (!entry.startsWith("+")) return null;
        
        Path newPath = PathFactory.createPath(parent.getSession());

        int indexOfTab = entry.indexOf("\t");
        if (indexOfTab == -1) return null;
        
        // parse name.
        int startName = indexOfTab + 1;
        String name = entry.substring(startName);
        if (name.endsWith("\r\n")) {
            int i = name.lastIndexOf("\r\n");
            name = name.substring(0, i);
        }
        if (name.equals(".") || name.equals("..") || name.equals("")) return null;
        newPath.setPath(parent.getAbsolute(), name);
        
        // set some reasonable defaults
        newPath.attributes.setPermission(new Permission("----------"));
        newPath.attributes.setOwner("unknown");
        newPath.attributes.setGroup("unknown");
        
        // parse facts.
        int i;
        int endFacts = startName - 2; // first char of name -> tab -> end of last fact.
        EPLFEntryParserContext factContext = new EPLFEntryParserContext(newPath);
        for (i = 1; i < endFacts; i++) {
            int factEnd = entry.indexOf(",", i);
            String fact = entry.substring(i, factEnd);
            factContext.handleFact(fact);
            i = factEnd;
        }
        factContext.conclude();
        
        if (!factContext.hasMayBeRetreivedFact() && !factContext.hasMayCWDToFact()) return null;

        return newPath;
    }

    private static class EPLFEntryParserContext {
        private Hashtable facts;
        private Path path = null;
        
        public EPLFEntryParserContext(Path aPath) {
            super();
            this.facts = new Hashtable();
            this.path = aPath;
        }
        
        public boolean hasSpecifiedPermissionsFact() {
            return hasFact("up");
        }
        
        public boolean hasMayBeRetreivedFact() {
            return hasFact("r");
        }
        
        public boolean hasMayCWDToFact() {
            return hasFact("/");
        }

        private boolean hasFact(String factId) {
            if (facts.containsKey(factId)) return true;
            else return false;
        }

        public void handleFact(String fact) {
            if (fact.length() == 0) return;

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
                    path.status.setSize(size.longValue());
                } catch (NumberFormatException ignored) {}
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
                } catch (NumberFormatException ignored) {}
                return;
            }
        }
        
        public void conclude() {
            if (hasMayCWDToFact()) {
                path.attributes.setType(Path.DIRECTORY_TYPE);
                if (hasSpecifiedPermissionsFact()) createAndSetSpecifiedDirPermission();
                else createAndSetStandardDirPermission();
                
            } else if (hasMayBeRetreivedFact()) {
                path.attributes.setType(Path.FILE_TYPE);
                if (hasSpecifiedPermissionsFact()) createAndSetSpecifiedPermission();
                else createAndSetStandardPermission();
            }
        }
        
        private void createAndSetSpecifiedDirPermission() {
            Permission newPermission = createSpecifiedDirPermission();
            if (newPermission != null) path.attributes.setPermission(newPermission);
        }

        private Permission createSpecifiedDirPermission() {
            try {
                int perm = Integer.valueOf((String)facts.get("up"), 8).intValue();
                Permission permission = new Permission(perm);
                String newMask = "d" + permission.getMask().substring(1);
                return new Permission(newMask);
            } catch (NumberFormatException ignored) {}
            return null;
        }

        private void createAndSetStandardDirPermission() {
            path.attributes.setPermission(new Permission("dr-xr-xr-x"));
        }

        private void createAndSetSpecifiedPermission() {
            Permission newPermission = createSpecifiedPermission();
            if (newPermission != null) path.attributes.setPermission(newPermission);
        }
        
        private Permission createSpecifiedPermission() {
            try {
                int perm = Integer.valueOf((String)facts.get("up"), 8).intValue();
                return new Permission(perm);
            } catch (NumberFormatException ignored) {}
            return null;
        }
        
        private void createAndSetStandardPermission() {
            path.attributes.setPermission(new Permission("-r--r--r--"));
        }
    }
}
