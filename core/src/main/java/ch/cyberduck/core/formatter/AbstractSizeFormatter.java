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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public abstract class AbstractSizeFormatter implements SizeFormatter {

    private final Unit kilo;
    private final Unit mega;
    private final Unit giga;
    private final Unit tera;

    private static final String UNKNOWN = "--";

    public AbstractSizeFormatter(final Unit kilo, final Unit mega, final Unit giga, final Unit tera) {
        this.kilo = kilo;
        this.mega = mega;
        this.giga = giga;
        this.tera = tera;
    }

    @Override
    public String format(final long size) {
        return format(size, false);
    }

    @Override
    public String format(final long size, final boolean plain) {
        if(size < 0) {
            return UNKNOWN;
        }
        if(size < kilo.multiple()) {
            return String.format("%d B", size);
        }
        StringBuilder formatted = new StringBuilder();
        if(size < mega.multiple()) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(kilo.multiple()),
                    1,
                    RoundingMode.HALF_UP)).append(" ").append(kilo.suffix());
        }
        else if(size < giga.multiple()) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(mega.multiple()),
                    1,
                    RoundingMode.HALF_UP)).append(" ").append(mega.suffix());
        }
        else if(size < tera.multiple()) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(giga.multiple()),
                    1,
                    RoundingMode.HALF_UP)).append(" ").append(giga.suffix());
        }
        else {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(tera.multiple()),
                    1,
                    RoundingMode.HALF_UP)).append(" ").append(tera.suffix());
        }
        if(plain) {
            formatted.append(" (").append(NumberFormat.getInstance().format(size)).append(" bytes)");
        }
        return formatted.toString();
    }
}
