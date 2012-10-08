package ch.cyberduck.core.date;

import java.text.SimpleDateFormat;

/**
 * @version $Id$
 */
public class MDTMSecondsDateFormatter extends AbstractDateFormatter {

    /**
     * Format to interpret MTDM timestamp
     */
    private static final SimpleDateFormat tsFormatSeconds =
            new SimpleDateFormat("yyyyMMddHHmmss");

    public MDTMSecondsDateFormatter() {
        super(tsFormatSeconds);
    }
}