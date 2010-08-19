package ch.cyberduck.ui.action;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class CalculateSizeWorker extends Worker<Long> {

    /**
     * Selected files.
     */
    private List<Path> files;

    public CalculateSizeWorker(List<Path> files) {
        this.files = files;
    }

    private long total;

    @Override
    public Long run() {
        for(Path next : files) {
            next.attributes().setSize(this.calculateSize(next));
            if(!next.getSession().isConnected()) {
                break;
            }
        }
        return total;
    }

    /**
     * Calculates recursively the size of this path if a directory
     *
     * @return The size of the file or the sum of all containing files if a directory
     * @warn Potentially lengthy operation
     */
    private long calculateSize(final AbstractPath p) {
        long size = 0;
        if(p.attributes().isDirectory()) {
            for(AbstractPath next : p.children()) {
                size += this.calculateSize(next);
            }
        }
        else if(p.attributes().isFile()) {
            size += p.attributes().getSize();
            total += size;
            this.update(total);
        }
        return size;
    }

    /**
     * Incremental update with latest size value.
     */
    protected abstract void update(long size);

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"), this.toString(files));
    }
}
