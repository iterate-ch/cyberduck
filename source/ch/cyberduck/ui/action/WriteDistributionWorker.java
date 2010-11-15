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
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.i18n.Locale;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class WriteDistributionWorker extends Worker<Distribution> {

    /**
     * Selected files.
     */
    private List<Path> files;

    public WriteDistributionWorker(List<Path> files) {
        this.files = files;
    }

    @Override
    public Distribution run() {
        for(Path next : files) {
        }
        return null;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                this.toString(files));
    }
}
