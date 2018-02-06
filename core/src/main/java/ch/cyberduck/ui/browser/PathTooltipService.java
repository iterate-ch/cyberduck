package ch.cyberduck.ui.browser;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;

public class PathTooltipService implements TooltipService<Path> {

    @Override
    public String getTooltip(final Path file) {
        final StringBuilder tooltip = new StringBuilder(file.getAbsolute());
        final Checksum checksum = file.attributes().getChecksum();
        if(Checksum.NONE != checksum) {
            tooltip.append("\n").append(String.format("%s %s", StringUtils.upperCase(checksum.algorithm.name()), checksum.hash));
        }
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            tooltip.append("\n").append(file.attributes().getVersionId());
        }
        return tooltip.toString();
    }
}
