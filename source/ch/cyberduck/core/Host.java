package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.StringPrepParseException;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * @version $Id$
 */
public class Host implements Serializable {
    private static Logger log = Logger.getLogger(Host.class);
    /**
     * The protocol identifier. Must be one of <code>sftp</code>, <code>ftp</code> or <code>ftps</code>
     *
     * @see Protocol#FTP
     * @see Protocol#FTP_TLS
     * @see Protocol#SFTP
     */
    private Protocol protocol
            = Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"));
    /**
     * The port number to connect to
     *
     * @see Protocol#getDefaultPort()
     */
    private int port = -1;
    /**
     * The fully qualified hostname
     */
    private String hostname
            = Preferences.instance().getProperty("connection.hostname.default");
    /**
     * The credentials to authenticate with
     */
    private Credentials credentials = new Credentials() {
        @Override
        public String getUsernamePlaceholder() {
            return Host.this.getProtocol().getUsernamePlaceholder();
        }

        @Override
        public String getPasswordPlaceholder() {
            return Host.this.getProtocol().getPasswordPlaceholder();
        }
    };
    private String uuid;
    /**
     * IDN normalized hostname
     */
    private String punycode;
    /**
     * The given name by the user for the bookmark
     */
    private String nickname;
    /**
     * The initial working directory if any
     */
    private String defaultpath;
    /**
     * Current working directory when session was interrupted
     */
    private String workdir;
    /**
     * The character encoding to use for file listings
     */
    private String encoding;
    /**
     * The connect mode to use if FTP
     */
    private FTPConnectMode connectMode;
    /**
     * The maximum number of concurrent sessions to this host
     */
    private Integer maxConnections;
    /**
     * The custom download folder
     */
    private String downloadFolder;

    /**
     * The timezone the server is living in
     */
    private TimeZone timezone;

    /**
     * Arbitrary text
     */
    private String comment;

    private String webURL;

    /**
     * Last accessed timestamp
     */
    private Date timestamp;

    /**
     * New host with the default protocol
     *
     * @param hostname The hostname of the server
     */
    public Host(String hostname) {
        this(Protocol.forName(Preferences.instance().getProperty("connection.protocol.default")),
                hostname);
    }

    /**
     * New host with the default protocol for this port
     *
     * @param hostname The hostname of the server
     * @param port     The port number to connect to
     */
    public Host(String hostname, int port) {
        this(Protocol.getDefaultProtocol(port), hostname, port);
    }

    /**
     * @param protocol
     * @param hostname
     */
    public Host(Protocol protocol, String hostname) {
        this(protocol, hostname, protocol.getDefaultPort());
    }

    /**
     * @param protocol The protocol to use, must be either Session.FTP or Session.SFTP
     * @param hostname The hostname of the server
     * @param port     The port number to connect to
     */
    public Host(Protocol protocol, String hostname, int port) {
        this(protocol, hostname, port, null);
    }

    /**
     * @param protocol
     * @param hostname
     * @param port
     * @param defaultpath
     */
    public Host(Protocol protocol, String hostname, int port, String defaultpath) {
        this.setProtocol(protocol);
        this.setPort(port);
        this.setHostname(hostname);
        this.setDefaultPath(defaultpath);
    }

    /**
     * @param dict A valid bookmark dictionary
     */
    public <T> Host(T dict) {
        this.init(dict);
    }

    /**
     * @param serialized A valid bookmark dictionary
     */
    public <T> void init(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        Object uuidObj = dict.stringForKey("UUID");
        if(uuidObj != null) {
            this.setUuid(uuidObj.toString());
        }
        Object protocolObj = dict.stringForKey("Protocol");
        if(protocolObj != null) {
            this.setProtocol(Protocol.forName(protocolObj.toString()));
        }
        Object hostnameObj = dict.stringForKey("Hostname");
        if(hostnameObj != null) {
            this.setHostname(hostnameObj.toString());
        }
        Object usernameObj = dict.stringForKey("Username");
        if(usernameObj != null) {
            credentials.setUsername(usernameObj.toString());
        }
        Object passwordObj = dict.stringForKey("Password");
        if(passwordObj != null) {
            credentials.setPassword(passwordObj.toString());
        }
        Object keyObj = dict.stringForKey("Private Key File");
        if(keyObj != null) {
            this.getCredentials().setIdentity(LocalFactory.createLocal(keyObj.toString()));
        }
        Object portObj = dict.stringForKey("Port");
        if(portObj != null) {
            this.setPort(Integer.parseInt(portObj.toString()));
        }
        Object pathObj = dict.stringForKey("Path");
        if(pathObj != null) {
            this.setDefaultPath(pathObj.toString());
        }
        Object workdirObj = dict.stringForKey("Workdir");
        if(workdirObj != null) {
            this.setWorkdir(workdirObj.toString());
        }
        Object nicknameObj = dict.stringForKey("Nickname");
        if(nicknameObj != null) {
            this.setNickname(nicknameObj.toString());
        }
        Object encodingObj = dict.stringForKey("Encoding");
        if(encodingObj != null) {
            this.setEncoding(encodingObj.toString());
        }
        Object connectModeObj = dict.stringForKey("FTP Connect Mode");
        if(connectModeObj != null) {
            if(connectModeObj.toString().equals(FTPConnectMode.ACTIVE.toString())) {
                this.setFTPConnectMode(FTPConnectMode.ACTIVE);
            }
            if(connectModeObj.toString().equals(FTPConnectMode.PASV.toString())) {
                this.setFTPConnectMode(FTPConnectMode.PASV);
            }
        }
        Object connObj = dict.stringForKey("Maximum Connections");
        if(connObj != null) {
            this.setMaxConnections(Integer.valueOf(connObj.toString()));
        }
        Object downloadObj = dict.stringForKey("Download Folder");
        if(downloadObj != null) {
            this.setDownloadFolder(downloadObj.toString());
        }
        Object timezoneObj = dict.stringForKey("Timezone");
        if(timezoneObj != null) {
            this.setTimezone(TimeZone.getTimeZone(timezoneObj.toString()));
        }
        Object commentObj = dict.stringForKey("Comment");
        if(commentObj != null) {
            this.setComment(commentObj.toString());
        }
        Object urlObj = dict.stringForKey("Web URL");
        if(urlObj != null) {
            this.setWebURL(urlObj.toString());
        }
        Object accessObj = dict.stringForKey("Access Timestamp");
        if(accessObj != null) {
            this.setTimestamp(new Date(Long.parseLong(accessObj.toString())));
        }
    }

    public <T> T getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setStringForKey(this.getProtocol().getIdentifier(), "Protocol");
        dict.setStringForKey(this.getNickname(), "Nickname");
        dict.setStringForKey(this.getUuid(), "UUID");
        dict.setStringForKey(this.getHostname(), "Hostname");
        dict.setStringForKey(String.valueOf(this.getPort()), "Port");
        if(StringUtils.isNotBlank(this.getCredentials().getUsername())) {
            dict.setStringForKey(this.getCredentials().getUsername(), "Username");
        }
        if(StringUtils.isNotBlank(this.getDefaultPath())) {
            dict.setStringForKey(this.getDefaultPath(), "Path");
        }
        if(StringUtils.isNotBlank(this.getWorkdir())) {
            dict.setStringForKey(this.getWorkdir(), "Workdir");
        }
        if(StringUtils.isNotBlank(this.getEncoding())) {
            dict.setStringForKey(this.getEncoding(), "Encoding");
        }
        if(null != this.getCredentials().getIdentity()) {
            dict.setStringForKey(this.getCredentials().getIdentity().getAbbreviatedPath(), "Private Key File");
        }
        if(this.getProtocol().equals(Protocol.FTP) || this.getProtocol().equals(Protocol.FTP_TLS)) {
            if(null != this.getFTPConnectMode()) {
                if(this.getFTPConnectMode().equals(FTPConnectMode.ACTIVE)) {
                    dict.setStringForKey(FTPConnectMode.ACTIVE.toString(), "FTP Connect Mode");
                }
                else if(this.getFTPConnectMode().equals(FTPConnectMode.PASV)) {
                    dict.setStringForKey(FTPConnectMode.PASV.toString(), "FTP Connect Mode");
                }
            }
        }
        if(null != this.getMaxConnections()) {
            dict.setStringForKey(String.valueOf(this.getMaxConnections()), "Maximum Connections");
        }
        if(!this.isDefaultDownloadFolder()) {
            dict.setStringForKey(this.getDownloadFolder().getAbbreviatedPath(), "Download Folder");
        }
        if(null != this.getTimezone()) {
            dict.setStringForKey(this.getTimezone().getID(), "Timezone");
        }
        if(StringUtils.isNotBlank(this.getComment())) {
            dict.setStringForKey(this.getComment(), "Comment");
        }
        if(!this.isDefaultWebURL()) {
            dict.setStringForKey(this.getWebURL(), "Web URL");
        }
        if(null != this.getTimestamp()) {
            dict.setStringForKey(String.valueOf(this.getTimestamp().getTime()), "Access Timestamp");
        }
        return dict.<T>getSerialized();
    }

    /**
     * Parses URL in the format ftp://username:pass@hostname:portnumber/path/to/file
     *
     * @param input
     * @return
     */
    public static Host parse(String input) {
        input = input.trim();
        int begin = 0;
        int cut;
        Protocol protocol = null;
        if(input.indexOf("://", begin) != -1) {
            cut = input.indexOf("://", begin);
            protocol = Protocol.forScheme(input.substring(begin, cut));
            if(null != protocol) {
                begin += cut - begin + 3;
            }
        }
        if(null == protocol) {
            protocol = Protocol.forName(
                    Preferences.instance().getProperty("connection.protocol.default"));
        }
        String username = null;
        String password = null;
        if(protocol.equals(Protocol.FTP)) {
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
                        log.error(e.getMessage());
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
                log.error(e.getMessage());
            }
        }
        final Host h = new Host(protocol, hostname, port, path);
        h.setCredentials(username, password);
        return h;
    }

    // ----------------------------------------------------------

    /**
     * @param defaultpath The path to change the working directory to upon connecting
     */
    public void setDefaultPath(String defaultpath) {
        this.defaultpath = StringUtils.isNotBlank(defaultpath) ? defaultpath.trim() : null;
    }

    /**
     * @return Null if no default path is set
     */
    public String getDefaultPath() {
        return this.defaultpath;
    }

    public String getWorkdir() {
        return workdir;
    }

    public void setWorkdir(String workdir) {
        this.workdir = workdir;
    }

    /**
     * @param username
     * @param password
     */
    public void setCredentials(final String username, final String password) {
        credentials.setUsername(username);
        credentials.setPassword(password);
    }

    /**
     * @return
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * @param protocol The protocol to use or null to use the default protocol for this port number
     */
    public void setProtocol(Protocol protocol) {
        log.debug("setProtocol:" + protocol);
        this.protocol = protocol != null ? protocol :
                Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @return
     * @see Protocol#FTP
     * @see Protocol#FTP_TLS
     * @see Protocol#SFTP
     */
    public Protocol getProtocol() {
        return this.protocol;
    }

    public String getUuid() {
        if(null == uuid) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * The given name for this bookmark
     *
     * @return The user-given name of this bookmark
     */
    public String getNickname() {
        if(StringUtils.isEmpty(nickname)) {
            return this.getDefaultNickname();
        }
        return nickname;
    }

    /**
     * @return The default given name of this bookmark
     */
    private String getDefaultNickname() {
        if(StringUtils.isNotEmpty(this.getHostname())) {
            return this.getHostname() + " \u2013 " + this.getProtocol().getName();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Sets a user-given name for this bookmark
     *
     * @param nickname
     */
    public void setNickname(String nickname) {
        if(this.getDefaultNickname().equals(nickname)) {
            return;
        }
        this.nickname = nickname;
    }

    /**
     * @return User readable hostname
     */
    public String getHostname() {
        return this.getHostname(false);
    }

    /**
     * @param punycode Use the ToASCII operation as defined in the IDNA RFC
     */
    public String getHostname(boolean punycode) {
        if(punycode && Preferences.instance().getBoolean("connection.hostname.idn")) {
            if(null == this.punycode && StringUtils.isNotEmpty(this.hostname)) {
                try {
                    // Convenience function that implements the IDNToASCII operation as defined in
                    // the IDNA RFC. This operation is done on complete domain names, e.g: "www.example.com".
                    // It is important to note that this operation can fail. If it fails, then the input
                    // domain name cannot be used as an Internationalized Domain Name and the application
                    // should have methods defined to deal with the failure.
                    // IDNA.DEFAULT Use default options, i.e., do not process unassigned code points
                    // and do not use STD3 ASCII rules If unassigned code points are found
                    // the operation fails with ParseException
                    final String idn = IDNA.convertIDNToASCII(this.hostname, IDNA.DEFAULT).toString();
                    log.info("IDN hostname for " + this.hostname + ":" + idn);
                    this.punycode = idn;
                }
                catch(StringPrepParseException e) {
                    log.error("Cannot convert hostname to IDNA:" + e.getMessage());
                }
            }
            if(StringUtils.isNotEmpty(this.punycode)) {
                return this.punycode;
            }
        }
        return this.hostname;
    }

    /**
     * Sets the name for this host
     * Also reverts the nickname if no custom nickname is set
     *
     * @param hostname
     */
    public void setHostname(String hostname) {
        log.debug("setHostname:" + hostname);
        this.hostname = hostname.trim();
        this.punycode = null;
    }

    /**
     * @param port The port number to connect to or -1 to use the default port for this protocol
     */
    public void setPort(int port) {
        this.port = port;
        if(-1 == port) {
            this.port = this.getProtocol().getDefaultPort();
        }
    }

    /**
     * @return The port number a socket should be opened to
     */
    public int getPort() {
        return this.port;
    }

    /**
     * The character encoding to be used with this host
     *
     * @param encoding
     */
    public void setEncoding(String encoding) {
        log.debug("setEncoding:" + encoding);
        this.encoding = encoding;
    }

    /**
     * @return The character encoding to be used when connecting
     *         to this server or null if the default encoding should be used
     */
    public String getEncoding() {
        return this.encoding;
    }

    public void setFTPConnectMode(FTPConnectMode connectMode) {
        log.debug("setFTPConnectMode:" + connectMode);
        this.connectMode = connectMode;
    }

    /**
     * @return The connect mode to be used when connecting
     *         to this server or null if the default connect mode should be used
     */
    public FTPConnectMode getFTPConnectMode() {
        return this.connectMode;
    }

    /**
     * Set a custom number of concurrent sessions allowed for this host
     * If not set, connection.pool.max is used.
     *
     * @param n null to use the default value or -1 if no limit
     */
    public void setMaxConnections(Integer n) {
        log.debug("setMaxConnections:" + n);
        this.maxConnections = n;
    }

    /**
     * @return The number of concurrent sessions allowed. -1 if unlimited or null
     *         if the default should be used
     */
    public Integer getMaxConnections() {
        return this.maxConnections;
    }

    /**
     * Set a custom download folder instead of queue.download.folder
     *
     * @param folder
     */
    public void setDownloadFolder(String folder) {
        log.debug("setDownloadFolder:" + folder);
        this.downloadFolder = LocalFactory.createLocal(folder).getAbbreviatedPath();
    }

    /**
     * The custom folder if any or the default download location
     *
     * @return
     */
    public Local getDownloadFolder() {
        if(null == this.downloadFolder) {
            return LocalFactory.createLocal(Preferences.instance().getProperty("queue.download.folder"));
        }
        return LocalFactory.createLocal(this.downloadFolder);
    }

    /**
     * @return True if no custom download location is set
     */
    public boolean isDefaultDownloadFolder() {
        return null == this.downloadFolder;
    }

    /**
     * Set a timezone for the remote server different from the local default timezone
     * May be useful to display modification dates of remote files correctly using the local timezone
     *
     * @param timezone
     */
    public void setTimezone(TimeZone timezone) {
        log.debug("setTimezone:" + timezone);
        this.timezone = timezone;
    }

    /**
     * @return The custom timezone or null if not set
     */
    public TimeZone getTimezone() {
        return this.timezone;
    }

    /**
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @return
     */
    public String getWebURL() {
        if(null == webURL) {
            return this.getDefaultWebURL();
        }
        final String protocol = "^http(s)?://.*$";
        if(!webURL.matches(protocol)) {
            webURL = "http://" + webURL;
        }
        return webURL;
    }

    /**
     * @return True if no custom web URL has been set
     */
    public boolean isDefaultWebURL() {
        return this.getWebURL().equals(this.getDefaultWebURL());
    }

    /**
     * @return
     */
    public String getDefaultWebURL() {
        return "http://" + this.getHostname();
    }

    public void setWebURL(String webURL) {
        if(this.getDefaultWebURL().equals(webURL)) {
            return;
        }
        this.webURL = webURL;
    }

    /**
     * @return The date this bookmark was last accessed.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Update the time this bookmark was last accessed.
     *
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return The IP address of the remote host if available
     * @throws UnknownHostException If the address cannot be resolved
     */
    public String getIp() throws UnknownHostException {
        try {
            return InetAddress.getByName(hostname).toString();
        }
        catch(UnknownHostException e) {
            throw new UnknownHostException(hostname + " cannot be resolved");
        }
    }

    /**
     * @return
     * @see #toURL()
     */
    @Override
    public String toString() {
        return this.toURL();
    }

    /**
     * protocol://user@host:port
     *
     * @return The URL of the remote host including user login hostname and port
     */
    public String toURL() {
        StringBuilder url = new StringBuilder(this.getProtocol().getScheme());
        url.append("://");
        if(StringUtils.isNotEmpty(this.getCredentials().getUsername())) {
            url.append(this.getCredentials().getUsername()).append("@");
        }
        url.append(this.getHostname(true));
        if(this.getPort() != this.getProtocol().getDefaultPort()) {
            url.append(":").append(this.getPort());
        }
        return url.toString();
    }

    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Host) {
            return this.getUuid().equals(((Host) other).getUuid());
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return this.toURL().hashCode();
    }

    public boolean isReachable() {
        return ReachabilityFactory.instance().isReachable(this);
    }

    public void diagnose() {
        ReachabilityFactory.instance().diagnose(this);
    }
}