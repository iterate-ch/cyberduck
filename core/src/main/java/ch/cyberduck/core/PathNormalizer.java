package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.unicode.NFCNormalizer;
import ch.cyberduck.core.unicode.UnicodeNormalizer;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;

public final class PathNormalizer {

    private static final boolean IS_ENABLED = PreferencesFactory.get().getBoolean("path.normalize");

    private static final UnicodeNormalizer UNICODE_NORMALIZER = PreferencesFactory.get().getBoolean("path.normalize.unicode") ? new NFCNormalizer() : UnicodeNormalizer.DISABLED;

    private PathNormalizer() {
        //
    }

    public static String name(final String path) {
        if(String.valueOf(Path.DELIMITER).equals(path)) {
            return path;
        }
        if(!StringUtils.contains(path, Path.DELIMITER)) {
            return UNICODE_NORMALIZER.normalize(path).toString();
        }
        if(StringUtils.endsWith(path, String.valueOf(Path.DELIMITER))) {
            return UNICODE_NORMALIZER.normalize(StringUtils.substringAfterLast(normalize(path), String.valueOf(Path.DELIMITER))).toString();
        }
        return UNICODE_NORMALIZER.normalize(StringUtils.substringAfterLast(path, String.valueOf(Path.DELIMITER))).toString();
    }

    public static String parent(final String absolute, final char delimiter) {
        if(String.valueOf(delimiter).equals(absolute)) {
            return null;
        }
        int index = absolute.length() - 1;
        if(absolute.charAt(index) == delimiter) {
            if(index > 0) {
                index--;
            }
        }
        int cut = absolute.lastIndexOf(delimiter, index);
        if(cut > 0) {
            return UNICODE_NORMALIZER.normalize(absolute.substring(0, cut)).toString();
        }
        //if (index == 0) parent is root
        return String.valueOf(delimiter);
    }

    public static String normalize(final String path) {
        return normalize(path, true);
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.
     *
     * @param path     The path to parse
     * @param absolute If the path is absolute
     * @return the normalized path.
     */
    public static String normalize(final String path, final boolean absolute) {
        if(null == path) {
            return String.valueOf(Path.DELIMITER);
        }
        String normalized = path;
        if(IS_ENABLED) {
            if(absolute) {
                while(!normalized.startsWith(String.valueOf(Path.DELIMITER))) {
                    normalized = Path.DELIMITER + normalized;
                }
            }
            while(!normalized.endsWith(String.valueOf(Path.DELIMITER))) {
                normalized += Path.DELIMITER;
            }
            // Resolve occurrences of "/./" in the normalized path
            while(true) {
                int index = normalized.indexOf("/./");
                if(index < 0) {
                    break;
                }
                normalized = normalized.substring(0, index) +
                        normalized.substring(index + 2);
            }
            // Resolve occurrences of "/../" in the normalized path
            while(true) {
                int index = normalized.indexOf("/../");
                if(index < 0) {
                    break;
                }
                if(index == 0) {
                    // The only left path is the root.
                    return String.valueOf(Path.DELIMITER);
                }
                normalized = normalized.substring(0, normalized.lastIndexOf(Path.DELIMITER, index - 1)) +
                        normalized.substring(index + 3);
            }
            StringBuilder n = new StringBuilder();
            if(normalized.startsWith("//")) {
                // see #972. Omit leading delimiter
                n.append(Path.DELIMITER);
                n.append(Path.DELIMITER);
            }
            else if(absolute) {
                // convert to absolute path
                n.append(Path.DELIMITER);
            }
            else if(normalized.startsWith(String.valueOf(Path.DELIMITER))) {
                // Keep absolute path
                n.append(Path.DELIMITER);
            }
            // Remove duplicated Path.DELIMITERs

            final String[] segments = StringUtils.split(normalized, String.valueOf(Path.DELIMITER));
            for(String segment : segments) {
                if(segment.equals(StringUtils.EMPTY)) {
                    continue;
                }
                n.append(segment);
                n.append(Path.DELIMITER);
            }
            normalized = n.toString();
            while(normalized.endsWith(String.valueOf(Path.DELIMITER)) && normalized.length() > 1) {
                //Strip any redundant delimiter at the end of the path
                normalized = normalized.substring(0, normalized.length() - 1);
            }
        }
        // Return the normalized path that we have completed
        return UNICODE_NORMALIZER.normalize(normalized).toString();
    }

    /**
     * Prunes the list of selected files. Files which are a child of an already included directory
     * are removed from the returned list.
     *
     * @param selected Selected files for transfer
     * @return Normalized
     */
    public static List<Path> normalize(final List<Path> selected) {
        final List<Path> normalized = new Collection<>();
        for(Path f : selected) {
            boolean duplicate = false;
            for(Path n : normalized) {
                if(f.isChild(n)) {
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
            }
            if(!duplicate) {
                normalized.add(f);
            }
        }
        return normalized;
    }

    /**
     * Compose path name
     *
     * @param root Parent directory
     * @param path Filename or path relative to workdir
     * @return Composed path
     */
    public static Path compose(final Path root, final String path) {
        if(StringUtils.startsWith(path, String.valueOf(Path.DELIMITER))) {
            // Mount absolute path
            final String normalized = normalize(StringUtils.replace(path, "\\", String.valueOf(Path.DELIMITER)), true);
            if(StringUtils.equals(normalized, String.valueOf(Path.DELIMITER))) {
                return root;
            }
            return new Path(normalized, normalized.equals(String.valueOf(Path.DELIMITER)) ?
                    EnumSet.of(Path.Type.volume, Path.Type.directory) : EnumSet.of(Path.Type.directory));
        }
        else {
            final String normalized;
            if(StringUtils.startsWith(path, String.format("%s%s", Path.HOME, Path.DELIMITER))) {
                // Relative path to the home directory
                normalized = normalize(StringUtils.removeStart(StringUtils.removeStart(
                        StringUtils.replace(path, "\\", String.valueOf(Path.DELIMITER)), Path.HOME), String.valueOf(Path.DELIMITER)), false);
            }
            else {
                // Relative path
                normalized = normalize(StringUtils.replace(path, "\\", String.valueOf(Path.DELIMITER)), false);
            }
            if(StringUtils.equals(normalized, String.valueOf(Path.DELIMITER))) {
                return root;
            }
            return new Path(String.format("%s%s%s", root.getAbsolute(), root.isRoot() ? StringUtils.EMPTY : Path.DELIMITER, normalized), EnumSet.of(Path.Type.directory));
        }
    }
}
