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
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class Host implements Serializable, Comparable<Host>, PreferencesReader {

    /**
     * The credentials to authenticate with for the CDN
     */
    private final Credentials cloudfront = new Credentials();
    /**
     * Jump host configuration for SSH bastion host connections
     */
    private Host jumphost;
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
    private Integer port;
    /**
     * The fully qualified hostname
     */
    private String hostname;
    /**
     * The credentials to authenticate with
     */
    private Credentials credentials;
    /**
     * Unique identifier
     */
    private String uuid;

    /**
     * The given name by the user for the bookmark
     */
    private String nickname;

    /**
     * The initial working directory if any and absolute path to document root of webserver for Web URL configuration
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
     * Custom options
     */
    private Map<String, String> custom;

    /**
     * Group bookmarks in view
     */
    private Set<String> labels;

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
     * @param credentials Login credentials
     */
    public Host(final Protocol protocol, final Credentials credentials) {
        this(protocol, protocol.getDefaultHostname(), credentials);
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
        this(protocol, hostname, port, protocol.getDefaultPath());
    }

    /**
     * @param protocol    Scheme
     * @param hostname    The hostname of the server
     * @param port        Port number
     * @param defaultpath Default working directory
     */
    public Host(final Protocol protocol, final String hostname, final int port, final String defaultpath) {
        this(protocol, hostname, port, defaultpath, new Credentials());
    }

    /**
     * @param protocol    Scheme
     * @param hostname    The hostname of the server
     * @param port        Port number
     * @param credentials Login credentials
     */
    public Host(final Protocol protocol, final String hostname, final int port, final Credentials credentials) {
        this(protocol, hostname, port, protocol.getDefaultPath(), credentials);
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
        this.credentials = credentials;
    }

    public Host(final Host other) {
        this.protocol = other.protocol;
        this.region = other.region;
        this.port = other.port;
        this.hostname = other.hostname;
        this.credentials = new Credentials(other.credentials);
        this.uuid = other.uuid;
        this.nickname = other.nickname;
        this.defaultpath = other.defaultpath;
        this.workdir = other.workdir;
        this.encoding = other.encoding;
        this.connectMode = other.connectMode;
        this.transfer = other.transfer;
        this.downloadFolder = other.downloadFolder;
        this.uploadFolder = other.uploadFolder;
        this.timezone = other.timezone;
        this.comment = other.comment;
        this.webURL = other.webURL;
        this.timestamp = other.timestamp;
        this.volume = other.volume;
        this.readonly = other.readonly;
        this.custom = other.custom;
        this.labels = other.labels;
    }

    @Override
    public <T> T serialize(final Serializer<T> dict) {
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
        if(null != webURL) {
            dict.setStringForKey(webURL, "Web URL");
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
        if(null != custom) {
            dict.setMapForKey(custom, "Custom");
        }
        if(null != labels) {
            dict.setStringListForKey(labels, "Labels");
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
    public Host setDefaultPath(final String defaultpath) {
        this.defaultpath = defaultpath;
        return this;
    }

    public Path getWorkdir() {
        return workdir;
    }

    public Host setWorkdir(final Path workdir) {
        this.workdir = workdir;
        return this;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public Host setCredentials(final Credentials credentials) {
        log.debug("Setting credentials for {} to {}", this, credentials);
        this.credentials = credentials;
        return this;
    }

    /**
     * @return Jump host configuration for SSH bastion host connections
     */
    public Host getJumphost() {
        return jumphost;
    }

    /**
     * @param jumphost Jump host configuration for SSH bastion host connections
     */
    public void setJumphost(final Host jumphost) {
        this.jumphost = jumphost;
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
     * @param protocol Connection profile
     */
    public Host setProtocol(final Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getUuid() {
        if(null == uuid) {
            uuid = new UUIDRandomStringService().random();
        }
        return uuid;
    }

    public Host setUuid(String uuid) {
        this.uuid = uuid;
        return this;
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
    public Host setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    /**
     * @return User readable hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname Server
     */
    public Host setHostname(final String hostname) {
        this.hostname = StringUtils.trim(hostname);
        return this;
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
    public Host setPort(final int port) {
        this.port = -1 == port ? protocol.getDefaultPort() : port;
        return this;
    }

    /**
     * @return The character encoding to be used when connecting to this server or null if the default encoding should
     * be used
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
    public Host setEncoding(final String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * @return The connect mode to be used when connecting to this server or null if the default connect mode should be
     * used
     */
    public FTPConnectMode getFTPConnectMode() {
        return connectMode;
    }

    public Host setFTPConnectMode(final FTPConnectMode connectMode) {
        this.connectMode = connectMode;
        return this;
    }

    /**
     * @return The number of concurrent sessions allowed. -1 if unlimited or null if the default should be used
     */
    public TransferType getTransferType() {
        return transfer;
    }

    /**
     * Set a custom number of concurrent sessions allowed for this host If not set, connection.pool.max is used.
     *
     * @param transfer null to use the default value or -1 if no limit
     */
    public Host setTransferType(final TransferType transfer) {
        this.transfer = transfer;
        return this;
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
    public Host setDownloadFolder(final Local folder) {
        downloadFolder = folder;
        return this;
    }

    public Local getUploadFolder() {
        return uploadFolder;
    }

    public Host setUploadFolder(final Local folder) {
        this.uploadFolder = folder;
        return this;
    }

    /**
     * @return The custom timezone or null if not set
     */
    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Set a timezone for the remote server different from the local default timezone May be useful to display
     * modification dates of remote files correctly using the local timezone
     *
     * @param timezone Timezone of server
     */
    public Host setTimezone(final TimeZone timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * Read property from protocol with fallback to generic preferences
     *
     * @param key Property name
     * @return Value for property key
     */
    @Override
    public String getProperty(final String key) {
        final Map<String, String> overrides = this.getCustom();
        if(overrides.containsKey(key)) {
            return overrides.get(key);
        }
        if(credentials.getProperty(key) != null) {
            return credentials.getProperty(key);
        }
        return protocol.getProperties().get(key);
    }

    public Host setProperty(final String key, final String value) {
        final Map<String, String> overrides = new HashMap<>(this.getCustom());
        overrides.put(key, value);
        return this.setCustom(overrides);
    }

    public String getRegion() {
        if(StringUtils.isBlank(region)) {
            return protocol.getRegion();
        }
        return region;
    }

    public Host setRegion(final String region) {
        this.region = region;
        return this;
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
    public Host setComment(final String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * @return HTTP accessible URL
     */
    public String getWebURL() {
        return webURL;
    }

    public Host setWebURL(final String url) {
        webURL = url;
        return this;
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
    public Host setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Local getVolume() {
        return volume;
    }

    public Host setVolume(final Local volume) {
        this.volume = volume;
        return this;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public Host setReadonly(final Boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public Map<String, String> getCustom() {
        return null == custom ? Collections.emptyMap() : custom;
    }

    public Host setCustom(final Map<String, String> custom) {
        this.custom = custom;
        return this;
    }

    public Set<String> getLabels() {
        return null == labels ? Collections.emptySet() : labels;
    }

    public Host setLabels(final Set<String> labels) {
        this.labels = labels;
        return this;
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
        return StringUtils.compare(defaultpath, o.defaultpath);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Host{");
        sb.append("protocol=").append(protocol);
        sb.append(", region='").append(region).append('\'');
        sb.append(", port=").append(port);
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", credentials=").append(credentials);
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", defaultpath='").append(defaultpath).append('\'');
        sb.append(", workdir=").append(workdir);
        sb.append(", custom=").append(custom);
        sb.append(", labels=").append(labels);
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
        };

        public static TransferType getType(Host host) {
            if(Host.TransferType.unknown.equals(host.getTransferType())) {
                return Host.TransferType.valueOf(PreferencesFactory.get().getProperty("queue.transfer.type"));
            }
            return host.getTransferType();
        }
    }
}
