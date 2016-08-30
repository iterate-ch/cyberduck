package ch.cyberduck.core.formatter;

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

public interface SizeFormatter {

    /**
     * Rounding mode to round towards "nearest neighbor" unless both
     * neighbors are equidistant, in which case round up.
     *
     * @param size Number of bytes
     * @return The size of the file using BigDecimal.ROUND_HALF_UP rounding
     */
    String format(long size);

    /**
     * @param size  Bytes
     * @param plain Include plain format of bytes
     * @return Formatted size
     */
    String format(long size, boolean plain);
}
