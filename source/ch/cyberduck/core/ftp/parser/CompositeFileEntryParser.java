package ch.cyberduck.core.ftp.parser;

import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;
import org.apache.log4j.Logger;

import java.util.List;

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

    private final List<? extends FTPFileEntryParser> ftpFileEntryParsers;
    private FTPFileEntryParser cachedFtpFileEntryParser;

    public CompositeFileEntryParser(List<? extends FTPFileEntryParser> ftpFileEntryParsers) {
        this.cachedFtpFileEntryParser = null;
        this.ftpFileEntryParsers = ftpFileEntryParsers;
    }

    @Override
    public FTPFile parseFTPEntry(final String line) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Parse %s", line));
        }
        if(cachedFtpFileEntryParser != null) {
            final FTPFile parsed = cachedFtpFileEntryParser.parseFTPEntry(line);
            if(null != parsed) {
                return parsed;
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Switching parser implementation because %s failed", cachedFtpFileEntryParser));
            }
            cachedFtpFileEntryParser = null;
        }
        for(FTPFileEntryParser parser : ftpFileEntryParsers) {
            FTPFile matched = parser.parseFTPEntry(line);
            if(matched != null) {
                cachedFtpFileEntryParser = parser;
                if(log.isInfoEnabled()) {
                    log.info(String.format("Caching %s parser implementation", cachedFtpFileEntryParser));
                }
                return matched;
            }
        }
        log.warn(String.format("Failure parsing %s", line));
        return null;
    }

    public FTPFileEntryParser getCachedFtpFileEntryParser() {
        return cachedFtpFileEntryParser;
    }

    @Override
    public void configure(FTPClientConfig config) {
        for(FTPFileEntryParser parser : ftpFileEntryParsers) {
            if(parser instanceof Configurable) {
                ((Configurable) parser).configure(config);
            }
        }
    }
}
