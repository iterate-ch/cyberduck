package ch.cyberduck.core.ftp.parser;

import org.apache.commons.net.ftp.*;
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
public class CompositeFileEntryParser extends FTPFileEntryParserImpl implements Configurable {
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
        for(FTPFileEntryParser parser : ftpFileEntryParsers) {
            FTPFile matched = parser.parseFTPEntry(listEntry);
            if(matched != null) {
                cachedFtpFileEntryParser = parser;
                log.info("Caching " + cachedFtpFileEntryParser + " parser implementation");
                return matched;
            }
        }
        return null;
    }

    public FTPFileEntryParser getCachedFtpFileEntryParser() {
        return cachedFtpFileEntryParser;
    }

    public void configure(FTPClientConfig config) {
        for(FTPFileEntryParser parser : ftpFileEntryParsers) {
            if(parser instanceof Configurable) {
                ((Configurable)parser).configure(config);
            }
        }
    }
}
