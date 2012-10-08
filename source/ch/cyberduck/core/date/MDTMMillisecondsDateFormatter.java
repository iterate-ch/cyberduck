package ch.cyberduck.core.date;

import java.text.SimpleDateFormat;

/**
 * @version $Id$
 */
public class MDTMMillisecondsDateFormatter extends AbstractDateFormatter {

    /**
     * Format to interpret MTDM timestamp
     */
    private static final SimpleDateFormat tsFormatMilliseconds =
            new SimpleDateFormat("yyyyMMddHHmmss.SSS");

    public MDTMMillisecondsDateFormatter() {
        super(tsFormatMilliseconds);
    }
}
