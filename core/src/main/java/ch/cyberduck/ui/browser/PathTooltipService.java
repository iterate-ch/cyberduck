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
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.date.AbstractUserDateFormatter;
import ch.cyberduck.core.formatter.SizeFormatter;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;

public class PathTooltipService {

    private final SizeFormatter sizeFormatter;

    private final AbstractUserDateFormatter dateFormatter;

    public PathTooltipService() {
        this(SizeFormatterFactory.get(), UserDateFormatterFactory.get());
    }

    public PathTooltipService(final SizeFormatter sizeFormatter, final AbstractUserDateFormatter dateFormatter) {
        this.sizeFormatter = sizeFormatter;
        this.dateFormatter = dateFormatter;
    }

    public String getTooltip(final Path file) {
        final StringBuilder tooltip = new StringBuilder(file.getAbsolute());
        if(StringUtils.isNotBlank(file.attributes().getRegion())) {
            tooltip.append("\n").append(file.attributes().getRegion());
        }
        final Checksum checksum = file.attributes().getChecksum();
        if(Checksum.NONE != checksum) {
            tooltip.append("\n").append(String.format("%s %s", StringUtils.upperCase(checksum.algorithm.name()), checksum.hash));
        }
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            tooltip.append("\n").append(file.attributes().getVersionId());
        }
        tooltip.append("\n").append(sizeFormatter.format(file.attributes().getSize()));
        tooltip.append("\n").append(dateFormatter.getLongFormat(file.attributes().getModificationDate()));
        return tooltip.toString();
    }
}