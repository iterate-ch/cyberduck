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
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.TimeZone;

public class Host implements Serializable, Comparable<Host> {

    /**
     * The credentials to authenticate with for the CDN
     */
    private final Credentials cloudfront = new DistributionCredentials();
    /**
     * The protocol identifier.
     */
    private Protocol protocol;
    private String region;
    /**
     * The port number to connect to
     *
     * @see Protocol#getDefaultPort()
     */
    private Integer port = -1;
    /**
     * The fully qualified hostname
     */
    private String hostname;
    /**
     * The credentials to authenticate with
     */
    private Credentials credentials = new HostCredentials(this);
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
    private FTPConnectMode connectMode
            = FTPConnectMode.unknown;

    /**
     * The maximum number of concurrent sessions to this host
     */
    private TransferType transfer
            = TransferType.unknown;

    /**
     * The custom download folder
     */
    private Local downloadFolder;

    /**
     * The custom download folder
     */
    private Local uploadFolder;

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
     * Mount target
     */
    private Local volume;

    /**
     * Connect with readonly mode
     */
    private Boolean readonly;

    /**
     * @param protocol Scheme
     */
    public Host(final Protocol protocol) {
        this(protocol, protocol.getDefaultHostname());
    }

    /**
     * @param protocol Scheme
     * @param hostname The hostname of the server
     */
    public Host(final Protocol protocol, final String hostname) {
        this(protocol, hostname, protocol.getDefaultPort());
    }

    /**
     * @param protocol    Scheme
     * @param hostname    The hostname of the server
     * @param credentials Login credentials
     */
    public Host(final Protocol protocol, final String hostname, final Credentials credentials) {
        this(protocol, hostname, protocol.getDefaultPort(), credentials);
    }

    /**
     * @param protocol The protocol to use, must be either Session.FTP or Session.SFTP
     * @param hostname The hostname of the server
     * @param port     The port number to connect to
     */
    public Host(final Protocol protocol, final String hostname, final int port) {
        this(protocol, hostname, port, (String) null);
    }

    /**
     * @param protocol    Scheme
     * @param hostname    The hostname of the server
     * @param port        Port number
     * @param defaultpath Default working directory
     */
    public Host(final Protocol protocol, final String hostname, final int port, final String defaultpath) {
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
    public Host(final Protocol protocol, final String hostname, final int port, final Credentials credentials) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
        this.credentials.setUsername(credentials.getUsername());
        this.credentials.setIdentity(credentials.getIdentity());
        if(!credentials.isAnonymousLogin()) {
            this.credentials.setPassword(credentials.getPassword());
        }
    }

    /**
     * @param protocol Scheme
     * @param hostname The hostname of the server
     * @param port     Port number
     */
    public Host(final Protocol protocol, final String hostname, final int port, final String defaultpath, final Credentials credentials) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
        this.defaultpath = defaultpath;
        this.credentials.setUsername(credentials.getUsername());
        this.credentials.setIdentity(credentials.getIdentity());
        if(!credentials.isAnonymousLogin()) {
            this.credentials.setPassword(credentials.getPassword());
        }
    }

    /**
     * Auto configuration
     */
    protected void configure() {
        this.configure(HostnameConfiguratorFactory.get(protocol), CredentialsConfiguratorFactory.get(protocol));
    }

    protected void configure(final HostnameConfigurator hostnameConfigurator,
                             final CredentialsConfigurator credentialsConfigurator) {
        final int port = hostnameConfigurator.getPort(hostname);
        if(port != -1) {
            // External configuration found
            this.setPort(port);
        }
        credentials = credentialsConfigurator.configure(this);
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(protocol.getIdentifier(), "Protocol");
        if(StringUtils.isNotBlank(protocol.getProvider())) {
            if(!StringUtils.equals(protocol.getProvider(), protocol.getIdentifier())) {
                dict.setStringForKey(protocol.getProvider(), "Provider");
            }
        }
        if(StringUtils.isNotBlank(nickname)) {
            dict.setStringForKey(nickname, "Nickname");
        }
        dict.setStringForKey(this.getUuid(), "UUID");
        dict.setStringForKey(hostname, "Hostname");
        dict.setStringForKey(String.valueOf(this.getPort()), "Port");
        if(StringUtils.isNotBlank(credentials.getUsername())) {
            dict.setStringForKey(credentials.getUsername(), "Username");
        }
        if(StringUtils.isNotBlank(cloudfront.getUsername())) {
            dict.setStringForKey(cloudfront.getUsername(), "CDN Credentials");
        }
        if(StringUtils.isNotBlank(defaultpath)) {
            dict.setStringForKey(defaultpath, "Path");
        }
        if(workdir != null) {
            dict.setObjectForKey(workdir, "Workdir Dictionary");
        }
        if(StringUtils.isNotBlank(encoding)) {
            dict.setStringForKey(encoding, "Encoding");
        }
        if(StringUtils.isNotBlank(credentials.getCertificate())) {
            dict.setStringForKey(credentials.getCertificate(), "Client Certificate");
        }
        if(null != credentials.getIdentity()) {
            dict.setStringForKey(credentials.getIdentity().getAbbreviatedPath(), "Private Key File");
            dict.setObjectForKey(credentials.getIdentity(), "Private Key File Dictionary");
        }
        if(protocol.getType() == Protocol.Type.ftp) {
            if(connectMode != FTPConnectMode.unknown) {
                dict.setStringForKey(connectMode.name(), "FTP Connect Mode");
            }
        }
        if(transfer != TransferType.unknown) {
            dict.setStringForKey(transfer.name(), "Transfer Connection");
        }
        if(null != downloadFolder) {
            dict.setStringForKey(downloadFolder.getAbbreviatedPath(), "Download Folder");
            dict.setObjectForKey(downloadFolder, "Download Folder Dictionary");
        }
        if(null != uploadFolder) {
            dict.setObjectForKey(uploadFolder, "Upload Folder Dictionary");
        }
        if(null != timezone) {
            dict.setStringForKey(this.getTimezone().getID(), "Timezone");
        }
        if(StringUtils.isNotBlank(comment)) {
            dict.setStringForKey(comment, "Comment");
        }
        if(!this.isDefaultWebURL()) {
            dict.setStringForKey(this.getWebURL(), "Web URL");
        }
        if(null != timestamp) {
            dict.setStringForKey(String.valueOf(timestamp.getTime()), "Access Timestamp");
        }
        if(null != volume) {
            dict.setStringForKey(String.valueOf(volume.getAbbreviatedPath()), "Volume");
        }
        if(null != readonly) {
            dict.setStringForKey(String.valueOf(readonly), "Readonly");
        }
        return dict.getSerialized();
    }

    /**
     * @return Null if no default path is set
     */
    public String getDefaultPath() {
        return defaultpath;
    }

    /**
     * @param defaultpath The path to change the working directory to upon connecting
     */
    public void setDefaultPath(final String defaultpath) {
        this.defaultpath = StringUtils.isBlank(defaultpath) ? null : StringUtils.trim(defaultpath);
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

    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * @return Credentials to modify CDN configuration
     */
    public Credentials getCdnCredentials() {
        return cloudfront;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * @param protocol The protocol to use or null to use the default protocol for this port number
     */
    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
        this.configure();
    }

    public String getUuid() {
        if(null == uuid) {
            uuid = new UUIDRandomStringService().random();
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
        return nickname;
    }

    /**
     * Sets a user-given name for this bookmark
     *
     * @param nickname Custom name
     */
    public void setNickname(String nickname) {
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
     * <p>
     * Configures credentials according to new hostname.
     *
     * @param hostname Server
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname.trim();
        this.configure();
    }

    /**
     * @return The port number a socket should be opened to
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port The port number to connect to or -1 to use the default port for this protocol
     */
    public void setPort(final int port) {
        this.port = -1 == port ? protocol.getDefaultPort() : port;
    }

    /**
     * @return The character encoding to be used when connecting to this server or null
     * if the default encoding should be used
     */
    public String getEncoding() {
        if(null == encoding) {
            return PreferencesFactory.get().getProperty("browser.charset.encoding");
        }
        return encoding;
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
     * @return The connect mode to be used when connecting
     * to this server or null if the default connect mode should be used
     */
    public FTPConnectMode getFTPConnectMode() {
        return connectMode;
    }

    public void setFTPConnectMode(final FTPConnectMode connectMode) {
        this.connectMode = connectMode;
    }

    /**
     * @return The number of concurrent sessions allowed. -1 if unlimited or null
     * if the default should be used
     */
    public TransferType getTransferType() {
        switch(transfer) {
            case unknown:
                return Host.TransferType.valueOf(PreferencesFactory.get().getProperty("queue.transfer.type"));
        }
        return transfer;
    }

    /**
     * Set a custom number of concurrent sessions allowed for this host
     * If not set, connection.pool.max is used.
     *
     * @param transfer null to use the default value or -1 if no limit
     */
    public void setTransfer(final TransferType transfer) {
        this.transfer = transfer;
    }

    /**
     * The custom folder if any or the default download location
     *
     * @return Absolute path
     */
    public Local getDownloadFolder() {
        return downloadFolder;
    }

    /**
     * Set a custom download folder instead of queue.download.folder
     *
     * @param folder Absolute path
     */
    public void setDownloadFolder(final Local folder) {
        downloadFolder = folder;
    }

    public Local getUploadFolder() {
        return uploadFolder;
    }

    public void setUploadFolder(final Local folder) {
        this.uploadFolder = folder;
    }

    /**
     * @return The custom timezone or null if not set
     */
    public TimeZone getTimezone() {
        return timezone;
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

    public String getRegion() {
        if(StringUtils.isBlank(region)) {
            return protocol.getRegion();
        }
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    /**
     * @return Notice
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment Notice
     */
    public void setComment(final String comment) {
        this.comment = comment;
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
            webURL = String.format("http://%s", webURL);
        }
        return webURL;
    }

    public void setWebURL(final String url) {
        if(this.getDefaultWebURL().equals(url)) {
            webURL = null;
        }
        else {
            webURL = url;
        }
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
        return String.format("http://%s", StringUtils.strip(hostname));
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

    public Local getVolume() {
        return volume;
    }

    public void setVolume(final Local volume) {
        this.volume = volume;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(final Boolean readonly) {
        this.readonly = readonly;
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
    public int compareTo(final Host o) {
        if(protocol.compareTo(o.protocol) < 0) {
            return -1;
        }
        else if(protocol.compareTo(o.protocol) > 0) {
            return 1;
        }
        if(port.compareTo(o.port) < 0) {
            return -1;
        }
        else if(port.compareTo(o.port) > 0) {
            return 1;
        }
        if(hostname.compareTo(o.hostname) < 0) {
            return -1;
        }
        else if(hostname.compareTo(o.hostname) > 0) {
            return 1;
        }
        if(credentials.compareTo(o.credentials) < 0) {
            return -1;
        }
        else if(credentials.compareTo(o.credentials) > 0) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Host{");
        sb.append("credentials=").append(credentials);
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", defaultpath='").append(defaultpath).append('\'');
        sb.append(", port=").append(port);
        sb.append(", protocol=").append(protocol);
        sb.append('}');
        return sb.toString();
    }

    public enum TransferType {
        unknown {
            @Override
            public String toString() {
                return LocaleFactory.localizedString("Default");
            }
        },
        browser {
            @Override
            public String toString() {
                return LocaleFactory.localizedString("Use browser connection", "Transfer");
            }
        },
        /**
         * Single connnection in Transfer window
         */
        newconnection {
            @Override
            public String toString() {
                return LocaleFactory.localizedString("Open single connection", "Transfer");
            }
        },
        concurrent {
            @Override
            public String toString() {
                return LocaleFactory.localizedString("Open multiple connections", "Transfer");
            }
        },
        udt {
            @Override
            public String toString() {
                return LocaleFactory.localizedString("Qloudsonic (UDP-based Data Transfer Protocol)", "Transfer");
            }
        }
    }
}