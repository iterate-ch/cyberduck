package ch.cyberduck.core.ftp;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.net.ftp.FTPCmd;
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

    private FTPSession session;

    public FTPTimezoneCalculator(final FTPSession session) {
        this.session = session;
    }

    /**
     * Best guess of available timezones given the offset of the modification
     * date in the directory listing from the UTC timestamp returned from <code>MDTM</code>
     * if available. Result is error prone because of additional daylight saving offsets.
     *
     * @param workdir Directory listing
     * @return Matching timezones
     */
    public List<TimeZone> get(final Path workdir) throws BackgroundException {
        // Determine the server offset from UTC
        final AttributedList<Path> list = session.list(workdir, new DisabledListProgressListener());
        if(list.isEmpty()) {
            log.warn("Cannot determine timezone with empty directory listing");
            return Collections.emptyList();
        }
        for(Path test : list) {
            if(test.attributes().isFile()) {
                long local = test.attributes().getModificationDate();
                if(-1 == local) {
                    log.warn("No modification date in directory listing to calculate timezone");
                    return Collections.emptyList();
                }
                // Subtract seconds
                local -= local % 60000;
                // Read the modify fact which must be UTC
                try {
                    if(session.getClient().hasFeature(FTPCmd.MDTM.getCommand())) {
                        try {
                            // In UTC
                            Long utc = new FTPMlsdListResponseReader().parseTimestamp(
                                    session.getClient().getModificationTime(test.getAbsolute()));
                            if(-1 == utc) {
                                log.warn("No UTC support on server");
                                return Collections.emptyList();
                            }
                            // Subtract seconds
                            utc -= utc % 60000;
                            // Offset for timestamps in file listing compared to UTC
                            long offset = local - utc;
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Calculated UTC offset is %dms", offset));
                            }
                            if(TimeZone.getTimeZone(Preferences.instance().getProperty("ftp.timezone.default")).getOffset(utc) == offset) {
                                if(log.isInfoEnabled()) {
                                    log.info("Offset equals local timezone offset.");
                                }
                                return Collections.singletonList(TimeZone.getTimeZone(
                                        Preferences.instance().getProperty("ftp.timezone.default")));
                            }
                            // The offset should be the raw GMT offset without the daylight saving offset.
                            // However the determined offset *does* include daylight saving time and therefore
                            // the call to TimeZone#getAvailableIDs leads to erroneous results.
                            final String[] timezones = TimeZone.getAvailableIDs((int) offset);
                            final List<TimeZone> zones = new ArrayList<TimeZone>();
                            for(String timezone : timezones) {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Matching timezone identifier %s", timezone));
                                }
                                final TimeZone match = TimeZone.getTimeZone(timezone);
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Determined timezone %s", match));
                                }
                                zones.add(match);
                            }
                            if(zones.isEmpty()) {
                                log.warn("Failed to calculate timezone for offset:" + offset);
                                continue;
                            }
                            return zones;
                        }
                        catch(FTPException e) {
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
