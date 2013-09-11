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

import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * @version $Id$
 */
public final class Host implements Serializable {
    private static final Logger log = Logger.getLogger(Host.class);

    /**
     * The protocol identifier.
     */
    private Protocol protocol
            = ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"));
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
    private Credentials credentials = new HostCredentials(this);

    /**
     * The credentials to authenticate with for the CDN
     */
    private Credentials cdnCredentials = new DistributionCredentials();

    /**
     * Unique identifier
     */
    private String uuid;

    /**
     * The given name by the user for the bookmark
     */
    private String nickname;

    /**
     * The initial working directory if any and absolute
     * path to document root of webserver for Web URL configuration
     */
    private String defaultpath;

    /**
     * Current working directory when session was interrupted
     */
    private Path workdir;

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
    private Local downloadFolder;

    /**
     * The timezone the server is living in
     */
    private TimeZone timezone;

    /**
     * Arbitrary text
     */
    private String comment;

    /**
     *
     */
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
        this(ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default")),
                hostname);
    }

    /**
     * New host with the default protocol for this port
     *
     * @param hostname The hostname of the server
     * @param port     The port number to connect to
     */
    public Host(String hostname, int port) {
        this(ProtocolFactory.getDefaultProtocol(port), hostname, port);
    }

    /**
     * @param protocol Scheme
     * @param hostname The hostname of the server
     */
    public Host(Protocol protocol, String hostname) {
        this(protocol, hostname, protocol.getDefaultPort());
    }

    /**
     * @param protocol    Scheme
     * @param hostname    The hostname of the server
     * @param credentials Login credentials
     */
    public Host(Protocol protocol, String hostname, Credentials credentials) {
        this(protocol, hostname, protocol.getDefaultPort());
        this.credentials = credentials;
    }

    /**
     * @param protocol The protocol to use, must be either Session.FTP or Session.SFTP
     * @param hostname The hostname of the server
     * @param port     The port number to connect to
     */
    public Host(Protocol protocol, String hostname, int port) {
        this(protocol, hostname, port, (String) null);
    }

    /**
     * @param protocol    Scheme
     * @param hostname    The hostname of the server
     * @param port        Port number
     * @param defaultpath Default working directory
     */
    public Host(Protocol protocol, String hostname, int port, String defaultpath) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
        this.defaultpath = defaultpath;
    }


    /**
     * @param protocol    Scheme
     * @param hostname    The hostname of the server
     * @param port        Port number
     * @param credentials Login credentials
     */
    public Host(Protocol protocol, String hostname, int port, Credentials credentials) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
        this.credentials = credentials;
    }

    /**
     * @param serialized A valid bookmark dictionary
     */
    public <T> Host(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        Object uuidObj = dict.stringForKey("UUID");
        if(uuidObj != null) {
            this.setUuid(uuidObj.toString());
        }
        Object protocolObj = dict.stringForKey("Protocol");
        if(protocolObj != null) {
            this.setProtocol(ProtocolFactory.forName(protocolObj.toString()));
        }
        Object providerObj = dict.stringForKey("Provider");
        if(providerObj != null) {
            final Protocol provider = ProtocolFactory.forName(providerObj.toString());
            if(null != provider) {
                this.setProtocol(provider);
            }
            else {
                log.warn(String.format("Provider %s no more available. Default to %s", providerObj, protocolObj));
            }
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
        Object cdnCredentialsObj = dict.stringForKey("CDN Credentials");
        if(cdnCredentialsObj != null) {
            cdnCredentials.setUsername(cdnCredentialsObj.toString());
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
            this.setWorkdir(new Path(workdirObj.toString(), Path.DIRECTORY_TYPE));
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
            if(connectModeObj.toString().equals(FTPConnectMode.PORT.toString())) {
                this.setFTPConnectMode(FTPConnectMode.PORT);
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
            this.setDownloadFolder(LocalFactory.createLocal(downloadObj.toString()));
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

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(this.getProtocol().getIdentifier(), "Protocol");
        if(StringUtils.isNotBlank(this.getProtocol().getProvider())) {
            if(!StringUtils.equals(this.getProtocol().getProvider(), this.getProtocol().getIdentifier())) {
                dict.setStringForKey(this.getProtocol().getProvider(), "Provider");
            }
        }
        dict.setStringForKey(this.getNickname(), "Nickname");
        dict.setStringForKey(this.getUuid(), "UUID");
        dict.setStringForKey(this.getHostname(), "Hostname");
        dict.setStringForKey(String.valueOf(this.getPort()), "Port");
        if(StringUtils.isNotBlank(this.getCredentials().getUsername())) {
            dict.setStringForKey(this.getCredentials().getUsername(), "Username");
        }
        if(StringUtils.isNotBlank(this.getCdnCredentials().getUsername())) {
            dict.setStringForKey(this.getCdnCredentials().getUsername(), "CDN Credentials");
        }
        if(StringUtils.isNotBlank(this.getDefaultPath())) {
            dict.setStringForKey(this.getDefaultPath(), "Path");
        }
        if(this.getWorkdir() != null) {
            dict.setStringForKey(this.getWorkdir().getAbsolute(), "Workdir");
        }
        if(StringUtils.isNotBlank(this.getEncoding())) {
            dict.setStringForKey(this.getEncoding(), "Encoding");
        }
        if(null != this.getCredentials().getIdentity()) {
            dict.setStringForKey(this.getCredentials().getIdentity().getAbbreviatedPath(), "Private Key File");
        }
        if(this.getProtocol().getType() == Protocol.Type.ftp) {
            if(null != this.getFTPConnectMode()) {
                if(this.getFTPConnectMode().equals(FTPConnectMode.PORT)) {
                    dict.setStringForKey(FTPConnectMode.PORT.toString(), "FTP Connect Mode");
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
        return dict.getSerialized();
    }

    /**
     * @param defaultpath The path to change the working directory to upon connecting
     */
    public void setDefaultPath(final String defaultpath) {
        this.defaultpath = StringUtils.isBlank(defaultpath) ? null :
                StringUtils.remove(StringUtils.remove(defaultpath, CharUtils.LF), CharUtils.CR).trim();
    }

    /**
     * @return Null if no default path is set
     */
    public String getDefaultPath() {
        return defaultpath;
    }

    public Path getWorkdir() {
        return workdir;
    }

    public void setWorkdir(final Path workdir) {
        this.workdir = workdir;
    }

    /**
     * @param username User
     * @param password Secret
     */
    public void setCredentials(final String username, final String password) {
        credentials.setUsername(username);
        credentials.setPassword(password);
    }

    public void setCredentials(Credentials credentials) {
        if(null != credentials) {
            this.credentials = credentials;
        }
    }

    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * @return Credentials to modify CDN configuration
     */
    public Credentials getCdnCredentials() {
        return cdnCredentials;
    }

    /**
     * @param p The protocol to use or null to use the default protocol for this port number
     */
    public void setProtocol(final Protocol p) {
        this.protocol = p != null ? p : ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"));
        this.setPort(HostnameConfiguratorFactory.get(protocol).getPort(hostname));
        this.setCredentials(CredentialsConfiguratorFactory.get(protocol).configure(this));
    }

    public Protocol getProtocol() {
        return protocol;
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
        if(StringUtils.isNotEmpty(hostname)) {
            return hostname + " \u2013 " + protocol.getName();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Sets a user-given name for this bookmark
     *
     * @param nickname Custom name
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
        return hostname;
    }

    /**
     * Sets the name for this host. Also reverts the nickname if no custom nickname is set.
     * <p/>
     * Configures credentials according to new hostname.
     *
     * @param hostname Server
     */
    public void setHostname(final String hostname) {
        if(protocol.isHostnameConfigurable()) {
            this.hostname = hostname.trim();
        }
        else {
            this.hostname = protocol.getDefaultHostname();
        }
        this.setPort(HostnameConfiguratorFactory.get(protocol).getPort(hostname));
        this.setCredentials(CredentialsConfiguratorFactory.get(protocol).configure(this));
    }

    /**
     * @param port The port number to connect to or -1 to use the default port for this protocol
     */
    public void setPort(final int port) {
        if(-1 == port) {
            this.port = protocol.getDefaultPort();
        }
        else {
            this.port = port;
        }
    }

    /**
     * @return The port number a socket should be opened to
     */
    public int getPort() {
        return port;
    }

    /**
     * The character encoding to be used with this host
     *
     * @param encoding Control connection encoding
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return The character encoding to be used when connecting to this server or null
     *         if the default encoding should be used
     */
    public String getEncoding() {
        return encoding;
    }

    public void setFTPConnectMode(final FTPConnectMode connectMode) {
        this.connectMode = connectMode;
    }

    /**
     * @return The connect mode to be used when connecting
     *         to this server or null if the default connect mode should be used
     */
    public FTPConnectMode getFTPConnectMode() {
        return connectMode;
    }

    /**
     * Set a custom number of concurrent sessions allowed for this host
     * If not set, connection.pool.max is used.
     *
     * @param n null to use the default value or -1 if no limit
     */
    public void setMaxConnections(final Integer n) {
        this.maxConnections = n;
    }

    /**
     * @return The number of concurrent sessions allowed. -1 if unlimited or null
     *         if the default should be used
     */
    public Integer getMaxConnections() {
        return maxConnections;
    }

    /**
     * Set a custom download folder instead of queue.download.folder
     *
     * @param folder Absolute path
     */
    public void setDownloadFolder(final Local folder) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set download folder for bookmark %s to %s", hostname, folder));
        }
        downloadFolder = folder;
    }

    /**
     * The custom folder if any or the default download location
     *
     * @return Absolute path
     */
    public Local getDownloadFolder() {
        if(null == downloadFolder) {
            return LocalFactory.createLocal(Preferences.instance().getProperty("queue.download.folder"));
        }
        return downloadFolder;
    }

    /**
     * @return True if no custom download location is set
     */
    public boolean isDefaultDownloadFolder() {
        return null == downloadFolder;
    }

    /**
     * Set a timezone for the remote server different from the local default timezone
     * May be useful to display modification dates of remote files correctly using the local timezone
     *
     * @param timezone Timezone of server
     */
    public void setTimezone(final TimeZone timezone) {
        this.timezone = timezone;
    }

    /**
     * @return The custom timezone or null if not set
     */
    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * @param comment Notice
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * @return Notice
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return HTTP accessible URL
     */
    public String getWebURL() {
        if(StringUtils.isBlank(webURL)) {
            return this.getDefaultWebURL();
        }
        final String regex = "^http(s)?://.*$";
        if(!webURL.matches(regex)) {
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
     * @return HTTP accessible URL with the same hostname as the server
     */
    public String getDefaultWebURL() {
        return String.format("http://%s", hostname);
    }

    public void setWebURL(final String url) {
        if(this.getDefaultWebURL().equals(url)) {
            webURL = null;
            return;
        }
        webURL = url;
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
     * @param timestamp Date and time
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Host) {
            return this.getUuid().equals(((Host) other).getUuid());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Host{");
        sb.append("credentials=").append(credentials);
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", port=").append(port);
        sb.append(", protocol=").append(protocol);
        sb.append('}');
        return sb.toString();
    }
}