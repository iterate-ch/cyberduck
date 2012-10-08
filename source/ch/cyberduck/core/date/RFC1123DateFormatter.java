package ch.cyberduck.core.date;

import java.text.SimpleDateFormat;

/**
 * @version $Id$
 */
public class RFC1123DateFormatter extends AbstractDateFormatter {

    /**
     * Format to RFC 1123 timestamp
     * Expires: Thu, 01 Dec 1994 16:00:00 GMT
     */
    private static final SimpleDateFormat rfc1123 =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.ENGLISH);

    public RFC1123DateFormatter() {
        super(rfc1123);
    }
}