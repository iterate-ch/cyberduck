package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.date.CalendarService;
import ch.cyberduck.core.date.Instant;

import org.apache.log4j.Logger;

import java.util.Calendar;

/**
 * @version $Id$
 */
public class ComparisonService {
    private static Logger log = Logger.getLogger(ComparisonService.class);

    private CalendarService calendarService = new CalendarService();

    /**
     * @param p File
     * @return #see Comparison#EQUAL
     *         #see Comparison#REMOTE_NEWER
     *         #see Comparison#LOCAL_NEWER
     */
    public Comparison compare(final Path p) {
        if(p.getLocal().exists() && p.exists()) {
            if(Preferences.instance().getBoolean("queue.sync.compare.hash")) {
                // MD5/ETag Checksum is supported
                Comparison comparison = this.compareChecksum(p);
                if(!Comparison.UNEQUAL.equals(comparison)) {
                    // Decision is available
                    return comparison;
                }
            }
            if(Preferences.instance().getBoolean("queue.sync.compare.size")) {
                Comparison comparison = this.compareSize(p);
                if(!Comparison.UNEQUAL.equals(comparison)) {
                    // Decision is available
                    return comparison;
                }
            }
            // Default comparison is using timestamp of file.
            Comparison comparison = this.compareTimestamp(p);
            if(!Comparison.UNEQUAL.equals(comparison)) {
                // Decision is available
                return comparison;
            }
        }
        else if(p.exists()) {
            // Only the remote file exists
            return Comparison.REMOTE_NEWER;
        }
        else if(p.getLocal().exists()) {
            // Only the local file exists
            return Comparison.LOCAL_NEWER;
        }
        return Comparison.EQUAL;
    }

    private Comparison compareSize(Path p) {
        if(log.isDebugEnabled()) {
            log.debug("compareSize:" + p);
        }
        if(p.attributes().isFile()) {
            if(p.attributes().getSize() == -1) {
                p.readSize();
            }
            //fist make sure both files are larger than 0 bytes
            if(p.attributes().getSize() == 0 && p.getLocal().attributes().getSize() == 0) {
                return Comparison.EQUAL;
            }
            if(p.attributes().getSize() == 0) {
                return Comparison.LOCAL_NEWER;
            }
            if(p.getLocal().attributes().getSize() == 0) {
                return Comparison.REMOTE_NEWER;
            }
            if(p.attributes().getSize() == p.getLocal().attributes().getSize()) {
                return Comparison.EQUAL;
            }
        }
        //different file size - further comparison check
        return Comparison.UNEQUAL;
    }

    private Comparison compareChecksum(Path p) {
        if(log.isDebugEnabled()) {
            log.debug("compareHash:" + p);
        }
        if(p.attributes().isFile()) {
            if(null == p.attributes().getChecksum()) {
                if(p.getSession().isChecksumSupported()) {
                    p.readChecksum();
                }
            }
            if(null == p.attributes().getChecksum()) {
                log.warn("No checksum available for comparison:" + p);
                return Comparison.UNEQUAL;
            }
            //fist make sure both files are larger than 0 bytes
            if(p.attributes().getChecksum().equals(p.getLocal().attributes().getChecksum())) {
                return Comparison.EQUAL;
            }
        }
        //different sum - further comparison check
        return Comparison.UNEQUAL;
    }

    private Comparison compareTimestamp(Path p) {
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
