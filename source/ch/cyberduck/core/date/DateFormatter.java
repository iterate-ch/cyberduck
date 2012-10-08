package ch.cyberduck.core.date;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public interface DateFormatter {

    String format(Date input, TimeZone zone);

    String format(long milliseconds, TimeZone zone);

    Date parse(String input) throws ParseException;

}
