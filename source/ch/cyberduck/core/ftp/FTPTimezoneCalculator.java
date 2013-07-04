package ch.cyberduck.core.ftp;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.net.ftp.FTPCommand;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class FTPTimezoneCalculator {
    private static final Logger log = Logger.getLogger(FTPTimezoneCalculator.class);

    /**
     * Best guess of available timezones given the offset of the modification
     * date in the directory listing from the UTC timestamp returned from <code>MDTM</code>
     * if available. Result is error prone because of additional daylight saving offsets.
     *
     * @param workdir Directory listing
     * @return Matching timezones
     */
    public List<TimeZone> get(final FTPSession session, final Path workdir) throws BackgroundException {
        // Determine the server offset from UTC
        final AttributedList<Path> list = workdir.list();
        if(list.isEmpty()) {
            log.warn("Cannot determine timezone with empty directory listing");
            return Collections.emptyList();
        }
        for(Path test : list) {
            if(test.attributes().isFile()) {
                long local = test.attributes().getModificationDate();
                if(-1 == local) {
                    log.warn("No modification date in directory listing to calculate timezone");
                    continue;
                }
                // Subtract seconds
                local -= local % 60000;
                // Read the modify fact which must be UTC
                try {
                    if(session.getClient().isFeatureSupported(FTPCommand.MDTM)) {
                        final String timestamp = session.getClient().getModificationTime(test.getAbsolute());
                        if(null != timestamp) {
                            Long utc = new FTPMlsdListResponseReader().parseTimestamp(timestamp);
                            if(-1 == utc) {
                                log.warn("No UTC support on server");
                                return Collections.emptyList();
                            }
                            // Subtract seconds
                            utc -= utc % 60000;
                            long offset = local - utc;
                            log.info(String.format("Calculated UTC offset is %dms", offset));
                            final List<TimeZone> zones = new ArrayList<TimeZone>();
                            if(TimeZone.getTimeZone(Preferences.instance().getProperty("ftp.timezone.default")).getOffset(utc) == offset) {
                                log.info("Offset equals local timezone offset.");
                                zones.add(TimeZone.getTimeZone(Preferences.instance().getProperty("ftp.timezone.default")));
                                return zones;
                            }
                            // The offset should be the raw GMT offset without the daylight saving offset.
                            // However the determied offset *does* include daylight saving time and therefore
                            // the call to TimeZone#getAvailableIDs leads to errorneous results.
                            final String[] timezones = TimeZone.getAvailableIDs((int) offset);
                            for(String timezone : timezones) {
                                log.info(String.format("Matching timezone identifier %s", timezone));
                                final TimeZone match = TimeZone.getTimeZone(timezone);
                                log.info(String.format("Determined timezone %s", match));
                                zones.add(match);
                            }
                            if(zones.isEmpty()) {
                                log.warn("Failed to calculate timezone for offset:" + offset);
                                continue;
                            }
                            return zones;
                        }
                        else {
                            return Collections.emptyList();
                        }
                    }
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e, test);
                }
            }
        }
        log.warn("No file in directory listing to calculate timezone");
        return Collections.emptyList();
    }
}
