package ch.cyberduck.core.date;

import java.text.ParseException;
import java.util.Date;

/**
 * @version $Id:$
 */
public interface DateFormatter {

    public String format(Date input);

    Date parse(String input) throws ParseException;

    String format(long milliseconds);
}
