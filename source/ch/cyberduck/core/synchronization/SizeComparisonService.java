package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class SizeComparisonService implements ComparisonService {
    private static Logger log = Logger.getLogger(ComparisonService.class);

    @Override
    public Comparison compare(final Path p) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Compare size for %s", p.getAbsolute()));
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
}
