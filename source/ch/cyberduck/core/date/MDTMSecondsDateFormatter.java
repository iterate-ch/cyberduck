package ch.cyberduck.core.date;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class MDTMSecondsDateFormatter extends AbstractDateFormatter {

    /**
     * Format to interpret MTDM timestamp
     */
    private static final SimpleDateFormat tsFormatSeconds =
            new SimpleDateFormat("yyyyMMddHHmmss");

    static {
        tsFormatSeconds.setTimeZone(TimeZone.getDefault());
    }

    public MDTMSecondsDateFormatter() {
        super(tsFormatSeconds);
    }
}