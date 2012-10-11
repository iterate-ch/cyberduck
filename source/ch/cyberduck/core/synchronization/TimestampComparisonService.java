package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.CalendarService;
import ch.cyberduck.core.date.Instant;

import org.apache.log4j.Logger;

import java.util.Calendar;

/**
 * @version $Id:$
 */
public class TimestampComparisonService implements ComparisonService {
    private static Logger log = Logger.getLogger(CombinedComparisionService.class);

    private CalendarService calendarService = new CalendarService();

    @Override
    public Comparison compare(final Path p) {
        if(log.isDebugEnabled()) {
            log.debug("compareTimestamp:" + p);
        }
        if(-1 == p.attributes().getModificationDate()) {
            if(p.getSession().isReadTimestampSupported()) {
                // Make sure we have a UTC timestamp
                p.readTimestamp();
            }
        }
        if(-1 == p.attributes().getModificationDate()) {
            log.warn("No modification date available for comparison:" + p);
            return Comparison.UNEQUAL;
        }
        final Calendar remote = calendarService.asDate(p.attributes().getModificationDate(), Instant.SECOND);
        final Calendar local = calendarService.asDate(p.getLocal().attributes().getModificationDate(), Instant.SECOND);
        if(local.before(remote)) {
            return Comparison.REMOTE_NEWER;
        }
        if(local.after(remote)) {
            return Comparison.LOCAL_NEWER;
        }
        //same timestamp
        return Comparison.EQUAL;
    }
}
