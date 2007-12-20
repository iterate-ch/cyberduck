package ch.cyberduck.core.ftp.parser;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

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
    private final FTPFileEntryParser[] ftpFileEntryParsers;
    private FTPFileEntryParser cachedFtpFileEntryParser;

    public CompositeFileEntryParser(FTPFileEntryParser[] ftpFileEntryParsers) {
        this.cachedFtpFileEntryParser = null;
        this.ftpFileEntryParsers = ftpFileEntryParsers;
    }

    public FTPFile parseFTPEntry(String listEntry) {
        if(cachedFtpFileEntryParser != null) {
            return cachedFtpFileEntryParser.parseFTPEntry(listEntry);
        }
        for(int iterParser = 0; iterParser < ftpFileEntryParsers.length; iterParser++) {
            FTPFileEntryParser ftpFileEntryParser = ftpFileEntryParsers[iterParser];
            FTPFile matched = ftpFileEntryParser.parseFTPEntry(listEntry);
            if(matched != null) {
                cachedFtpFileEntryParser = ftpFileEntryParser;
                return matched;
            }
        }
        return null;
    }

    public FTPFileEntryParser getCachedFtpFileEntryParser() {
        return cachedFtpFileEntryParser;
    }
}
