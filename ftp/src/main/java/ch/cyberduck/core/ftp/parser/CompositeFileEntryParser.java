package ch.cyberduck.core.ftp.parser;

import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger log = LogManager.getLogger(CompositeFileEntryParser.class);

    private final List<? extends FTPFileEntryParser> parsers;
    private FTPFileEntryParser current;

    public CompositeFileEntryParser(final List<? extends FTPFileEntryParser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public List<String> preParse(final List<String> original) {
        for(FTPFileEntryParser parser : parsers) {
            parser.preParse(original);
        }
        return original;
    }

    @Override
    public FTPFile parseFTPEntry(final String line) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Parse %s", line));
        }
        if(current != null) {
            final FTPFile parsed = current.parseFTPEntry(line);
            if(null != parsed) {
                return parsed;
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Switching parser implementation because %s failed", current));
            }
            current = null;
        }
        for(FTPFileEntryParser parser : parsers) {
            final FTPFile matched = parser.parseFTPEntry(line);
            if(matched != null) {
                current = parser;
                if(log.isInfoEnabled()) {
                    log.info(String.format("Caching %s parser implementation", current));
                }
                return matched;
            }
        }
        log.warn(String.format("Failure parsing line %s", line));
        return null;
    }

    public FTPFileEntryParser getCurrent() {
        return current;
    }

    @Override
    public void configure(final FTPClientConfig config) {
        for(FTPFileEntryParser parser : parsers) {
            if(parser instanceof Configurable) {
                ((Configurable) parser).configure(config);
            }
        }
    }
}
