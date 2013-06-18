package ch.cyberduck.core.filter;

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.local.Local;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;

/**
 * @version $Id$
 */
public class UploadRegexFilter implements Filter<Local> {
    private static final Logger log = Logger.getLogger(UploadRegexFilter.class);

    private final Pattern pattern
            = Pattern.compile(Preferences.instance().getProperty("queue.upload.skip.regex"));

    @Override
    public boolean accept(final Local file) {
        if(file.attributes().isDuplicate()) {
            return false;
        }
        if(Preferences.instance().getBoolean("queue.upload.skip.enable")) {
            if(pattern.matcher(file.getName()).matches()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Skip %s excluded with regex", file.getAbsolute()));
                }
                return false;
            }
        }
        return true;
    }
}
