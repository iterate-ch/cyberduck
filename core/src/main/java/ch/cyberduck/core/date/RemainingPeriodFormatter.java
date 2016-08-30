package ch.cyberduck.core.date;

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
import java.text.MessageFormat;

public class RemainingPeriodFormatter implements PeriodFormatter {

    /**
     * @param remaining Seconds
     * @return Humean readable string for seconds in hours, minutes or seconds remaining
     */
    @Override
    public String format(final long remaining) {
        StringBuilder b = new StringBuilder();
        if(remaining < 0) {
            // File sizes larger than advertised
            return LocaleFactory.localizedString("Unknown");
        }
        if(remaining > 7200) { // More than two hours
            b.append(MessageFormat.format(LocaleFactory.localizedString("{0} hours remaining", "Status"),
                    new BigDecimal(remaining).divide(new BigDecimal(3600), 1, BigDecimal.ROUND_DOWN).toString())
            );
        }
        else if(remaining > 120) { // More than two minutes
            b.append(MessageFormat.format(LocaleFactory.localizedString("{0} minutes remaining", "Status"),
                    String.valueOf((int) (remaining / 60)))
            );
        }
        else {
            b.append(MessageFormat.format(LocaleFactory.localizedString("{0} seconds remaining", "Status"),
                    String.valueOf((int) remaining))
            );
        }
        return b.toString();
    }
}
