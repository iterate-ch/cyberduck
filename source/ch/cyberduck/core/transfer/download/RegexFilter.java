package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;

/**
 * @version $Id:$
 */
public class RegexFilter implements PathFilter<Path> {
    private static final Logger log = Logger.getLogger(RegexFilter.class);

    private final Pattern pattern
            = Pattern.compile(Preferences.instance().getProperty("queue.download.skip.regex"));

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isDuplicate()) {
            return false;
        }
        if(Preferences.instance().getBoolean("queue.download.skip.enable")) {
            if(pattern.matcher(file.getName()).matches()) {
                return false;
            }
        }
        return true;
    }
}
