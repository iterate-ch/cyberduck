package ch.cyberduck.core.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public abstract class AbstractDateFormatter implements DateFormatter {

    private final DateFormat format;

    protected AbstractDateFormatter(final DateFormat format) {
        this.format = format;
    }

    @Override
    public String format(final Date input, final TimeZone zone) {
        synchronized(format) {
            format.setTimeZone(zone);
            return format.format(input);
        }
    }

    @Override
    public String format(final long milliseconds, final TimeZone zone) {
        synchronized(format) {
            format.setTimeZone(zone);
            return format.format(milliseconds);
        }
    }

    @Override
    public Date parse(final String input) throws ParseException {
        synchronized(format) {
            return format.parse(input);
        }
    }
}
