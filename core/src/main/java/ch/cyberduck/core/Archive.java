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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Description of an archive format

 */
public abstract class Archive {
    private static final Logger log = Logger.getLogger(Archive.class);

    private Preferences preferences
            = PreferencesFactory.get();

    public static final Archive TAR
            = new Archive("tar") {
        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("tar archive", "Archive");
        }
    };

    public static final Archive TARGZ
            = new Archive("tar.gz") {
        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("gzip compressed tar archive", "Archive");
        }

        @Override
        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "tgz"};
        }
    };

    public static final Archive TARBZ2
            = new Archive("tar.bz2") {
        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("bzip2 compressed tar archive", "Archive");
        }

        @Override
        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "tbz", "tbz2"};
        }
    };

    public static final Archive ZIP
            = new Archive("zip") {
        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("ZIP archive", "Archive");
        }
    };

    public static final Archive GZIP
            = new Archive("gz") {
        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("gzip compressed tar archive", "Archive");
        }

        @Override
        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "gzip"};
        }
    };

    public static final Archive BZ2
            = new Archive("bz2") {
        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("bzip2 compressed archive", "Archive");
        }

        @Override
        public String[] getExtensions() {
            return new String[]{this.getIdentifier(), "bz", "bzip2"};
        }
    };

    /**
     * @return The default archiver set in the Preferences
     */
    public static Archive getDefault() {
        return Archive.forName(PreferencesFactory.get().getProperty("archive.default"));
    }

    /**
     * @return List of archive types
     */
    public static Archive[] getKnownArchives() {
        return new Archive[]{TAR, TARGZ, TARBZ2, ZIP};
    }

    /**
     * Factory
     *
     * @param name Identifier
     * @return Archive description
     */
    public static Archive forName(final String name) {
        if(StringUtils.isNotBlank(name)) {
            for(Archive archive : getKnownArchives()) {
                for(String extension : archive.getExtensions()) {
                    if(name.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
                        return archive;
                    }
                }
            }
        }
        log.fatal(String.format("Unknown archive %s", name));
        return null;
    }

    /**
     * Typical filename extension. The default
     */
    private String identifier;

    /**
     * @param extension Filename extension for archive format
     */
    private Archive(final String extension) {
        this.identifier = extension;
    }

    /**
     * @return Known filename extensions for archive format
     */
    public String[] getExtensions() {
        return new String[]{this.getIdentifier()};
    }

    /**
     * @return Archive identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    public abstract String getDescription();

    /**
     * @param files Files
     * @return Archived path for files
     */
    public Path getArchive(final List<Path> files) {
        if(files.size() == 0) {
            return null;
        }
        if(files.size() == 1) {
            return new Path(files.get(0).getParent(),
                    String.format("%s.%s", files.get(0).getName(), this.getIdentifier()),
                    EnumSet.of(Path.Type.file));
        }
        return new Path(files.get(0).getParent(),
                String.format("%s.%s", LocaleFactory.localizedString("Archive", "Archive"), this.getIdentifier()),
                EnumSet.of(Path.Type.file));
    }

    /**
     * @param files Files
     * @return Expanded filenames
     */
    public List<Path> getExpanded(final List<Path> files) {
        final List<Path> expanded = new ArrayList<Path>();
        for(Path file : files) {
            expanded.add(new Path(file.getParent(),
                    StringUtils.remove(file.getName(), String.format(".%s", this.getIdentifier())), EnumSet.of(Path.Type.file)));
        }
        return expanded;
    }

    /**
     * @param files Files to archive
     * @return Name of archive
     */
    public String getTitle(final List<Path> files) {
        final Path archive = this.getArchive(files);
        if(null == archive) {
            return this.getIdentifier();
        }
        return archive.getName();
    }

    /**
     * @param workdir Working directory
     * @param files   Files to archive
     * @return Archive command
     */
    public String getCompressCommand(final Path workdir, final List<Path> files) {
        final StringBuilder archive = new StringBuilder();
        if(files.size() == 1) {
            archive.append(this.escape(files.get(0).getAbsolute()));
        }
        else {
            // Use default filename
            archive.append(this.escape(files.get(0).getParent().getAbsolute())).append(Path.DELIMITER).append("Archive");
        }
        final List<String> command = new ArrayList<String>();
        for(Path path : files) {
            command.add(this.escape(PathRelativizer.relativize(workdir.getAbsolute(), path.getAbsolute())));
        }
        return MessageFormat.format(preferences.getProperty(String.format("archive.command.create.%s", this.getIdentifier())),
                archive.toString(), StringUtils.join(command, " "), this.escape(workdir.getAbsolute()));
    }

    /**
     * @param path Filename
     * @return Unarchive command
     */
    public String getDecompressCommand(final Path path) {
        return MessageFormat.format(preferences.getProperty(String.format("archive.command.expand.%s", this.getIdentifier())),
                this.escape(path.getAbsolute()), this.escape(path.getParent().getAbsolute()));
    }

    /**
     * Escape blank
     *
     * @param path Filename
     * @return Escaped whitespace in path
     */
    protected String escape(final String path) {
        final StringBuilder escaped = new StringBuilder();
        for(char c : path.toCharArray()) {
            if(StringUtils.isAlphanumeric(String.valueOf(c))
                    || c == Path.DELIMITER) {
                escaped.append(c);
            }
            else {
                escaped.append("\\").append(c);
            }
        }
        return escaped.toString();
    }

    /**
     * @param filename File extension to match with archive format
     * @return True if a known file extension for compressed archives
     */
    public static boolean isArchive(final String filename) {
        if(StringUtils.isNotBlank(filename)) {
            for(Archive archive : getKnownArchives()) {
                for(String extension : archive.getExtensions()) {
                    if(filename.endsWith(extension)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
