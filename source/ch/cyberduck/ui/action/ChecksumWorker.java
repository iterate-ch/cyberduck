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

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class ChecksumWorker extends Worker<List<String>> {

    /**
     * Selected files.
     */
    private List<Path> files;

    public ChecksumWorker(List<Path> files) {
        this.files = files;
    }

    @Override
    public List<String> run() {
        List<String> checksum = new ArrayList<String>();
        for(Path file : files) {
            if(StringUtils.isBlank(file.attributes().getChecksum())) {
                file.readChecksum();
            }
            checksum.add(file.attributes().getChecksum());
        }
        return checksum;
    }


    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                this.toString(files));
    }
}
