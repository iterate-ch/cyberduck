package ch.cyberduck.ui.comparator;

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

import org.apache.commons.lang3.StringUtils;

import java.text.Collator;
import java.util.Locale;

public class ExtensionComparator extends BrowserComparator {
    private static final long serialVersionUID = -7930478156003767294L;

    private final Collator impl = Collator.getInstance(Locale.getDefault());

    public ExtensionComparator(boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(final Path p1, final Path p2) {
        if(p1.isDirectory() && p2.isDirectory()) {
            return 0;
        }
        if(p1.isFile() && p2.isFile()) {
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
        if(p1.isFile()) {
            return ascending ? 1 : -1;
        }
        return ascending ? -1 : 1;
    }

}
