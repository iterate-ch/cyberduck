package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;

/**
 * @version $Id:$
 */
public class RegexFilter implements PathFilter<Local> {
    private static final Logger log = Logger.getLogger(RegexFilter.class);

    private final Pattern pattern
            = Pattern.compile(Preferences.instance().getProperty("queue.upload.skip.regex"));

    @Override
    public boolean accept(final Local child) {
        if(child.attributes().isDuplicate()) {
            return false;
        }
        if(Preferences.instance().getBoolean("queue.upload.skip.enable")) {
            if(pattern.matcher(child.getName()).matches()) {
                return false;
            }
        }
        return true;
    }
}
