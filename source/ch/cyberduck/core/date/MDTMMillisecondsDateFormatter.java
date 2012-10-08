package ch.cyberduck.core.date;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class MDTMMillisecondsDateFormatter extends AbstractDateFormatter {

    /**
     * Format to interpret MTDM timestamp
     */
    private static final SimpleDateFormat tsFormatMilliseconds =
            new SimpleDateFormat("yyyyMMddHHmmss.SSS");

    static {
        tsFormatMilliseconds.setTimeZone(TimeZone.getDefault());
    }

    public MDTMMillisecondsDateFormatter() {
        super(tsFormatMilliseconds);
    }
}
