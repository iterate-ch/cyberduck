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

import ch.cyberduck.core.LocaleFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * @version $Id$
 */
public class AbstractSizeFormatter implements SizeFormatter {

    private final long kilo;
    private final long mega;
    private final long giga;

    public AbstractSizeFormatter(final long kilo, final long mega, final long giga) {
        this.kilo = kilo;
        this.mega = mega;
        this.giga = giga;
    }

    @Override
    public String format(final long size) {
        return format(size, false);
    }

    @Override
    public String format(final long size, final boolean plain) {
        return format(size, plain, true);
    }


    @Override
    public String format(final long size, final boolean plain, final boolean bytes) {
        if(-1 == size) {
            return LocaleFactory.localizedString("--");
        }
        if(size < kilo) {
            return (int) size + (bytes ? " B" : " bit");
        }
        StringBuilder formatted = new StringBuilder();
        if(size < mega) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(kilo),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " KB" : " kbit");
        }
        else if(size < giga) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(mega),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " MB" : " Mbit");
        }
        else {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(giga),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " GB" : " Gbit");
        }
        if(plain) {
            formatted.append(" (").append(NumberFormat.getInstance().format(size)).append(" bytes)");
        }
        return formatted.toString();
    }
}
