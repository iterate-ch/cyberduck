package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id$
 */
public abstract class AbstractPath {

    /**
     * Shortcut for the home directory
     */
    public static final String HOME = "~";

    public static final int FILE_TYPE = 1;
    public static final int DIRECTORY_TYPE = 2;
    public static final int SYMBOLIC_LINK_TYPE = 4;
    public static final int VOLUME_TYPE = 8;

//    public enum Type {
//        file {
//            @Override
//            public int legacy() {
//                return 1;
//            }
//        },
//        directory {
//            @Override
//            public int legacy() {
//                return 2;
//            }
//        },
//        symboliclink {
//            @Override
//            public int legacy() {
//                return 4;
//            }
//        },
//        volume {
//            @Override
//            public int legacy() {
//                return 8;
//            }
//        };
//
//        public abstract int legacy();
//    }

    /**
     * @return Descriptive features for path
     */
    public abstract Attributes attributes();

    /**
     * Default implementation returning a reference to self. You can override this
     * if you need a different strategy to compare hashcode and equality for caching
     * in a model.
     *
     * @return Reference to the path to be used in table models an file listing cache.
     * @see ch.cyberduck.core.Cache#lookup(PathReference)
     */
    public abstract PathReference getReference();

    public abstract char getDelimiter();

    /**
     * @return true if this paths points to '/'
     * @see #getDelimiter()
     */
    public boolean isRoot() {
        return this.getAbsolute().equals(String.valueOf(this.getDelimiter()));
    }

    public abstract String getAbsolute();

    public abstract String getName();

    /**
     * @return the extension if any or null otherwise
     */
    public String getExtension() {
        final String extension = FilenameUtils.getExtension(this.getName());
        if(StringUtils.isEmpty(extension)) {
            return null;
        }
        return extension;
    }
}