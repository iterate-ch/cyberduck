package ch.cyberduck.core.date;

import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @version $Id:$
 */
public class CalendarService implements DateDomainService<Calendar> {
    private static Logger log = Logger.getLogger(CalendarService.class);

    /**
     * @param timestamp Milliseconds
     *                  #see Calendar#MILLISECOND
     *                  #see Calendar#SECOND
     *                  #see Calendar#MINUTE
     *                  #see Calendar#HOUR
     * @return Calendar from milliseconds
     */
    @Override
    public Calendar asDate(final long timestamp, final Instant precision) {
        if(log.isDebugEnabled()) {
            log.debug("asCalendar:" + timestamp);
        }
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(timestamp);
        if(precision == Instant.MILLISECOND) {
            return c;
        }
        c.clear(Calendar.MILLISECOND);
        if(precision == Instant.SECOND) {
            return c;
        }
        c.clear(Calendar.SECOND);
        if(precision == Instant.MINUTE) {
            return c;
        }
        c.clear(Calendar.MINUTE);
        if(precision == Instant.HOUR) {
            return c;
        }
        c.clear(Calendar.HOUR);
        return c;
    }
}
