package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class CombinedComparisionService implements ComparisonService {
    private static Logger log = Logger.getLogger(CombinedComparisionService.class);

    private ComparisonService checksum
            = new ChecksumComparisonService();

    private ComparisonService size
            = new SizeComparisonService();

    private ComparisonService timestamp
            = new TimestampComparisonService();

    /**
     * @see Comparison#EQUAL
     * @see Comparison#REMOTE_NEWER
     * @see Comparison#LOCAL_NEWER
     */
    @Override
    public Comparison compare(final Path p) {
        if(p.getLocal().exists() && p.exists()) {
            if(Preferences.instance().getBoolean("queue.sync.compare.hash")) {
                // MD5/ETag Checksum is supported
                Comparison comparison = checksum.compare(p);
                if(!Comparison.UNEQUAL.equals(comparison)) {
                    // Decision is available
                    return comparison;
                }
            }
            if(Preferences.instance().getBoolean("queue.sync.compare.size")) {
                Comparison comparison = size.compare(p);
                if(!Comparison.UNEQUAL.equals(comparison)) {
                    // Decision is available
                    return comparison;
                }
            }
            // Default comparison is using timestamp of file.
            Comparison comparison = timestamp.compare(p);
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
}
