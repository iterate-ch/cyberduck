package ch.cyberduck.ui;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;

import org.apache.commons.lang.StringUtils;

import java.text.Collator;
import java.util.Locale;

/**
 * @version $Id$
 */
public class ExtensionComparator extends BrowserComparator {
    private Collator impl = Collator.getInstance(Locale.getDefault());

    public ExtensionComparator(boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(Path p1, Path p2) {
        if(p1.attributes().isDirectory() && p2.attributes().isDirectory()) {
            return 0;
        }
        if(p1.attributes().isFile() && p2.attributes().isFile()) {
            if(StringUtils.isBlank(p1.getExtension()) && StringUtils.isBlank(p2.getExtension())) {
                return 0;
            }
            if(StringUtils.isBlank(p1.getExtension())) {
                return -1;
            }
            if(StringUtils.isBlank(p2.getExtension())) {
                return 1;
            }
            if(ascending) {
                return impl.compare(p1.getExtension(), p2.getExtension());
            }
            return -impl.compare(p1.getExtension(), p2.getExtension());
        }
        if(p1.attributes().isFile()) {
            return ascending ? 1 : -1;
        }
        return ascending ? -1 : 1;
    }

    @Override
    public String getIdentifier() {
        return "icon";
    }
}
