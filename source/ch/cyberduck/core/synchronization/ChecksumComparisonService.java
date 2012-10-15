package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ChecksumComparisonService implements ComparisonService {
    private static Logger log = Logger.getLogger(ComparisonService.class);

    @Override
    public Comparison compare(final Path p) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Compare checksum for %s", p.getAbsolute()));
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
}
