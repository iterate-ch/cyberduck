package ch.cyberduck.core;

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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Description of an archive format
 *
 * @version $Id$
 */
public abstract class Archive {
    private static Logger log = Logger.getLogger(Archive.class);

    public static final Archive TAR
            = new Archive("tar") {
        public String getDescription() {
            return NSBundle.localizedString("tar archive", "Archive", "");
        }
    };

    public static final Archive TARGZ
            = new Archive("tar.gz") {
        public String getDescription() {
            return NSBundle.localizedString("gzip compressed tar archive", "Archive", "");
        }

        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "tgz"};
        }
    };

    public static final Archive TARBZ2
            = new Archive("tar.bz2") {
        public String getDescription() {
            return NSBundle.localizedString("bzip2 compressed tar archive", "Archive", "");
        }

        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "tbz", "tbz2"};
        }
    };

    public static final Archive ZIP
            = new Archive("zip") {
        public String getDescription() {
            return NSBundle.localizedString("ZIP archive", "Archive", "");
        }
    };

    public static final Archive GZIP
            = new Archive("gz") {
        public String getDescription() {
            return NSBundle.localizedString("gzip compressed tar archive", "Archive", "");
        }

        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "gzip"};
        }
    };

    public static final Archive BZ2
            = new Archive("bz2") {
        public String getDescription() {
            return NSBundle.localizedString("bzip2 compressed archive", "Archive", "");
        }

        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "bz", "bzip2"};
        }
    };

    /**
     * @return The default archiver set in the Preferences
     */
    public static Archive getDefault() {
        return Archive.forName(Preferences.instance().getProperty("archive.default"));
    }

    /**
     * @return
     */
    public static Archive[] getKnownArchives() {
        return new Archive[]{TAR, TARGZ, TARBZ2, ZIP};
    }

    /**
     * Factory
     *
     * @param name
     * @return
     */
    public static Archive forName(final String name) {
        final Archive[] archives = getKnownArchives();
        for(int i = 0; i < archives.length; i++) {
            for(int k = 0; k < archives[i].getExtensions().length; k++) {
                if(name.endsWith(archives[i].getExtensions()[k])) {
                    return archives[i];
                }
            }
        }
        log.fatal("Unknown archive:" + name);
        return null;
    }

    /**
     * Typical filename extension. The default
     */
    private String identifier;

    /**
     * @param path
     */
    private Archive(final String extension) {
        this.identifier = extension;
    }

    /**
     * @return
     */

    public String[] getExtensions() {
        return new String[]{this.getIdentifier()};
    }

    /**
     * @return
     */
    public String getIdentifier() {
        return identifier;
    }

    public abstract String getDescription();

    /**
     * @param files
     * @return
     */
    public Path getArchive(final List<Path> files) {
        if(files.size() == 0) {
            return null;
        }
        if(files.size() == 1) {
            return PathFactory.createPath(files.get(0).getSession(), files.get(0).getParent().getAbsolute(),
                    files.get(0).getName() + "." + this.getIdentifier(),
                    Path.FILE_TYPE);
        }
        return PathFactory.createPath(files.get(0).getSession(), files.get(0).getParent().getAbsolute(),
                NSBundle.localizedString("Archive", "Archive", "") + "." + this.getIdentifier(),
                Path.FILE_TYPE);
    }

    /**
     *
     * @param files
     * @return
     */
    public List<Path> getExpanded(final List<Path> files) {
        final List<Path> expanded = new ArrayList<Path>();
        for(Path file : files) {
            expanded.add(PathFactory.createPath(file.getSession(), file.getParent().getAbsolute(),
                    StringUtils.remove(file.getName(), "." + this.getIdentifier()),
                    Path.FILE_TYPE));
        }
        return expanded;
    }

    /**
     * @param files
     * @return
     */
    public String getTitle(final List<Path> files) {
        Path archive = this.getArchive(files);
        if(null == archive) {
            return this.getIdentifier();
        }
        return archive.getName();
    }

    /**
     * @return
     */
    public String getCompressCommand(final List<Path> files) {
        String archive;
        if(files.size() == 1) {
            archive = files.get(0).getAbsolute();
        }
        else {
            archive = files.get(0).getParent().getAbsolute() + Path.DELIMITER + NSBundle.localizedString("Archive", "Archive", "");
        }
        return MessageFormat.format(Preferences.instance().getProperty("archive.command.create." + this.getIdentifier()),
                archive, StringUtils.join(files, " "));
    }

    /**
     * @return
     */
    public String getDecompressCommand(final Path path) {
        return MessageFormat.format(Preferences.instance().getProperty("archive.command.expand." + this.getIdentifier()),
                path.getAbsolute(), path.getParent().getAbsolute());
    }


    /**
     * @return True if a known file extension for compressed archives
     */
    public static boolean isArchive(final String filename) {
        if(StringUtils.isNotBlank(filename)) {
            Archive[] archives = getKnownArchives();
            for(int i = 0; i < archives.length; i++) {
                String[] extensions = archives[i].getExtensions();
                for(int k = 0; k < extensions.length; k++) {
                    if(filename.endsWith(extensions[k])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
