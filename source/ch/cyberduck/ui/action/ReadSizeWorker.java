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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class ReadSizeWorker extends Worker<Long> {

    /**
     * Selected files.
     */
    private List<Path> files;

    public ReadSizeWorker(List<Path> files) {
        this.files = files;
    }

    private long total;

    @Override
    public Long run() {
        for(Path next : files) {
            if(-1 == next.attributes().getSize()) {
                next.readSize();
            }
            if(-1 < next.attributes().getSize()) {
                total += next.attributes().getSize();
            }
        }
        return total;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"), this.toString(files));
    }
}
