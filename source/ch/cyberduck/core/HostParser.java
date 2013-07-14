package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @version $Id:$
 */
public final class HostParser {
    private static final Logger log = Logger.getLogger(HostParser.class);

    private HostParser() {
        //
    }

    /**
     * Parses URL in the format ftp://username:pass@hostname:portnumber/path/to/file
     *
     * @param url URL
     * @return Bookmark
     */
    public static Host parse(final String url) {
        final String input = url.trim();
        int begin = 0;
        int cut;
        Protocol protocol = null;
        if(input.indexOf("://", begin) != -1) {
            cut = input.indexOf("://", begin);
            protocol = ProtocolFactory.forScheme(input.substring(begin, cut));
            if(null != protocol) {
                begin += cut - begin + 3;
            }
        }
        if(null == protocol) {
            protocol = ProtocolFactory.forName(
                    Preferences.instance().getProperty("connection.protocol.default"));
        }
        String username;
        String password = null;
        if(protocol.isAnonymousConfigurable()) {
            username = Preferences.instance().getProperty("connection.login.anon.name");
        }
        else {
            username = Preferences.instance().getProperty("connection.login.name");
        }
        if(input.lastIndexOf('@') != -1) {
            if(input.indexOf(':', begin) != -1 && input.lastIndexOf('@') > input.indexOf(':', begin)) {
                // ':' is not for the port number but username:pass seperator
                cut = input.indexOf(':', begin);
                username = input.substring(begin, cut);
                begin += username.length() + 1;
                cut = input.lastIndexOf('@');
                password = input.substring(begin, cut);
                begin += password.length() + 1;
            }
            else {
                //no password given
                cut = input.lastIndexOf('@');
                username = input.substring(begin, cut);
                begin += username.length() + 1;
            }
        }
        String hostname = Preferences.instance().getProperty("connection.hostname.default");
        if(StringUtils.isNotBlank(input)) {
            hostname = input.substring(begin, input.length());
        }
        String path = null;
        int port = protocol.getDefaultPort();
        if(input.indexOf(':', begin) != -1) {
            cut = input.indexOf(':', begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length() + 1;
            try {
                String portString;
                if(input.indexOf(Path.DELIMITER, begin) != -1) {
                    portString = input.substring(begin, input.indexOf(Path.DELIMITER, begin));
                    begin += portString.length();
                    try {
                        path = URLDecoder.decode(input.substring(begin, input.length()), "UTF-8");
                    }
                    catch(UnsupportedEncodingException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                else {
                    portString = input.substring(begin, input.length());
                }
                port = Integer.parseInt(portString);
            }
            catch(NumberFormatException e) {
                log.warn("Invalid port number given");
            }
        }
        else if(input.indexOf(Path.DELIMITER, begin) != -1) {
            cut = input.indexOf(Path.DELIMITER, begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length();
            try {
                path = URLDecoder.decode(input.substring(begin, input.length()), "UTF-8");
            }
            catch(UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }
        final Host h = new Host(protocol, hostname, port, path);
        h.setCredentials(username, password);
        return h;
    }
}
