package ch.cyberduck.ui.comparator;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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
import ch.cyberduck.core.local.FileDescriptor;
import ch.cyberduck.core.local.FileDescriptorFactory;

import java.text.Collator;
import java.util.Locale;

public class FileTypeComparator extends BrowserComparator {
    private static final long serialVersionUID = 3354482708309574292L;

    private final Collator impl = Collator.getInstance(Locale.getDefault());

    private final FileDescriptor descriptor = FileDescriptorFactory.get();

    public FileTypeComparator(boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(final Path p1, final Path p2) {
        if((p1.isDirectory() && p2.isDirectory())
                || p1.isFile() && p2.isFile()) {
            if(ascending) {
                return impl.compare(descriptor.getKind(p1), descriptor.getKind(p2));
            }
            return -impl.compare(descriptor.getKind(p1), descriptor.getKind(p2));
        }
        if(p1.isFile()) {
            return ascending ? 1 : -1;
        }
        return ascending ? -1 : 1;
    }
}
