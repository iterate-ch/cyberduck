package ch.cyberduck.core.ftp.parser;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;
import org.apache.log4j.Logger;

/**
 * This implementation allows to pack some FileEntryParsers together
 * and handle the case where to returned dirstyle isn't clearly defined.
 * The matching parser will be cached.
 * If the cached parser wont match due to the server changed the dirstyle,
 * a new matching parser will be searched.
 *
 * @author Mario Ivankovits <mario@ops.co.at>
 */
public class CompositeFileEntryParser extends FTPFileEntryParserImpl {
    private static Logger log = Logger.getLogger(CompositeFileEntryParser.class);

    private final FTPFileEntryParser[] ftpFileEntryParsers;
    private FTPFileEntryParser cachedFtpFileEntryParser;

    public CompositeFileEntryParser(FTPFileEntryParser[] ftpFileEntryParsers) {
        this.cachedFtpFileEntryParser = null;
        this.ftpFileEntryParsers = ftpFileEntryParsers;
    }

    public FTPFile parseFTPEntry(String listEntry) {
        if(cachedFtpFileEntryParser != null) {
            final FTPFile parsed = cachedFtpFileEntryParser.parseFTPEntry(listEntry);
            if(null != parsed) {
                return parsed;
            }
            log.info("Switching parser implementation because "+cachedFtpFileEntryParser+" failed");
            cachedFtpFileEntryParser = null;
        }
        for(int iterParser = 0; iterParser < ftpFileEntryParsers.length; iterParser++) {
            FTPFileEntryParser ftpFileEntryParser = ftpFileEntryParsers[iterParser];
            FTPFile matched = ftpFileEntryParser.parseFTPEntry(listEntry);
            if(matched != null) {
                cachedFtpFileEntryParser = ftpFileEntryParser;
                log.info("Caching "+cachedFtpFileEntryParser+" parser implementation");
                return matched;
            }
        }
        return null;
    }

    public FTPFileEntryParser getCachedFtpFileEntryParser() {
        return cachedFtpFileEntryParser;
    }
}
