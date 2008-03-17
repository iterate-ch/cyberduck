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

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.TimeZone;

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.StringPrepParseException;

/**
 * @version $Id$
 */
public class Host extends NSObject {
    private static Logger log = Logger.getLogger(Host.class);
    /**
     * The protocol identifier. Must be one of <code>sftp</code>, <code>ftp</code> or <code>ftps</code>
     *
     * @see Protocol#FTP
     * @see Protocol#FTP_TLS
     * @see Protocol#SFTP
     */
    private Protocol protocol;
    /**
     * The port number to connect to
     *
     * @see Protocol#getDefaultPort()
     */
    private int port;
    /**
     * The fully qualified hostname
     */
    private String hostname;
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
     * The credentials to authenticate with
     */
    private Credentials credentials;
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

    private static final String HOSTNAME = "Hostname";
    private static final String NICKNAME = "Nickname";
    private static final String PORT = "Port";
    private static final String PROTOCOL = "Protocol";
    private static final String USERNAME = "Username";
    private static final String PATH = "Path";
    private static final String ENCODING = "Encoding";
    private static final String KEYFILE = "Private Key File";
    private static final String FTPCONNECTMODE = "FTP Connect Mode";
    private static final String MAXCONNECTIONS = "Maximum Connections";
    private static final String DOWNLOADFOLDER = "Download Folder";
    private static final String TIMEZONE = "Timezone";
    private static final String COMMENT = "Comment";

    private Local file;

    /**
     * Read the bookmark from the given file
     *
     * @param file A valid bookmark dictionary
     */
    public Host(final Local file) throws IOException {
        this.file = file;
        this.read();
    }

    /**
     * @param dict A valid bookmark dictionary
     */
    public Host(NSDictionary dict) {
        this.init(dict);
    }

    /**
     * @param dict A valid bookmark dictionary
     */
    public void init(NSDictionary dict) {
        Object protocolObj = dict.objectForKey(Host.PROTOCOL);
        if(protocolObj != null) {
            this.setProtocol(Protocol.forName(protocolObj.toString()));
        }
        Object hostnameObj = dict.objectForKey(Host.HOSTNAME);
        if(hostnameObj != null) {
            this.setHostname((String) hostnameObj);
            Object usernameObj = dict.objectForKey(Host.USERNAME);
            if(usernameObj != null) {
                this.setCredentials((String) usernameObj, null);
            }
            this.getCredentials().setPrivateKeyFile((String) dict.objectForKey(Host.KEYFILE));
        }
        Object portObj = dict.objectForKey(Host.PORT);
        if(portObj != null) {
            this.setPort(Integer.parseInt(portObj.toString()));
        }
        Object pathObj = dict.objectForKey(Host.PATH);
        if(pathObj != null) {
            this.setDefaultPath((String) pathObj);
        }
        Object nicknameObj = dict.objectForKey(Host.NICKNAME);
        if(nicknameObj != null) {
            this.setNickname((String) nicknameObj);
        }
        Object encodingObj = dict.objectForKey(Host.ENCODING);
        if(encodingObj != null) {
            this.setEncoding((String) encodingObj);
        }
        Object connectModeObj = dict.objectForKey(Host.FTPCONNECTMODE);
        if(connectModeObj != null) {
            if(connectModeObj.equals(FTPConnectMode.ACTIVE.toString())) {
                this.setFTPConnectMode(FTPConnectMode.ACTIVE);
            }
            if(connectModeObj.equals(FTPConnectMode.PASV.toString())) {
                this.setFTPConnectMode(FTPConnectMode.PASV);
            }
        }
        Object connObj = dict.objectForKey(Host.MAXCONNECTIONS);
        if(connObj != null) {
            this.setMaxConnections(Integer.valueOf(connObj.toString()));
        }
        Object downloadObj = dict.objectForKey(Host.DOWNLOADFOLDER);
        if(downloadObj != null) {
            this.setDownloadFolder((String) downloadObj);
        }
        Object timezoneObj = dict.objectForKey(Host.TIMEZONE);
        if(timezoneObj != null) {
            this.setTimezone(TimeZone.getTimeZone((String) timezoneObj));
        }
        Object commentObj = dict.objectForKey(Host.COMMENT);
        if(commentObj != null) {
            this.setComment((String) commentObj);
        }
    }

    /**
     * @return
     */
    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.getProtocol().getIdentifier(), Host.PROTOCOL);
        if(!this.getNickname().equals(this.getDefaultNickname())) {
            dict.setObjectForKey(this.getNickname(), Host.NICKNAME);
        }
        dict.setObjectForKey(this.getHostname(), Host.HOSTNAME);
        dict.setObjectForKey(String.valueOf(this.getPort()), Host.PORT);
        dict.setObjectForKey(this.getCredentials().getUsername(), Host.USERNAME);
        if(null != this.defaultpath) {
            dict.setObjectForKey(this.defaultpath, Host.PATH);
        }
        if(null != this.encoding) {
            dict.setObjectForKey(this.encoding, Host.ENCODING);
        }
        if(this.getCredentials().getPrivateKeyFile() != null) {
            dict.setObjectForKey(this.getCredentials().getPrivateKeyFile(), Host.KEYFILE);
        }
        if(this.getProtocol().equals(Protocol.FTP) || this.getProtocol().equals(Protocol.FTP_TLS)) {
            if(null != this.connectMode) {
                if(connectMode.equals(FTPConnectMode.ACTIVE))
                    dict.setObjectForKey(FTPConnectMode.ACTIVE.toString(), Host.FTPCONNECTMODE);
                else if(connectMode.equals(FTPConnectMode.PASV))
                    dict.setObjectForKey(FTPConnectMode.PASV.toString(), Host.FTPCONNECTMODE);
            }
        }
        if(null != this.maxConnections) {
            dict.setObjectForKey(String.valueOf(this.maxConnections), Host.MAXCONNECTIONS);
        }
        if(null != this.downloadFolder) {
            dict.setObjectForKey(this.downloadFolder, Host.DOWNLOADFOLDER);
        }
        if(null != this.timezone) {
            dict.setObjectForKey(this.timezone.getID(), Host.TIMEZONE);
        }
        if(null != this.comment) {
            dict.setObjectForKey(comment, Host.COMMENT);
        }
        return dict;
    }

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
        this.setCredentials(null, null);
    }

    /**
     * Parses URL in the format ftp://username:pass@hostname:portnumber/path/to/file
     *
     * @param input
     * @return
     * @throws MalformedURLException
     */
    public static Host parse(String input) throws MalformedURLException {
        if(!StringUtils.hasText(input)) {
            throw new MalformedURLException("No hostname given");
        }
        int begin = 0;
        int cut;
        if(input.indexOf("://", begin) == -1 && input.indexOf('@', begin) == -1) {
            throw new MalformedURLException("No protocol or user delimiter");
        }
        Protocol protocol = null;
        if(input.indexOf("://", begin) != -1) {
            cut = input.indexOf("://", begin);
            protocol = Protocol.forScheme(input.substring(begin, cut));
            if(null != protocol) {
                begin += protocol.getScheme().length() + 3;
            }
            else {
                protocol = Protocol.forName(input.substring(begin, cut));
                begin += protocol.getIdentifier().length() + 3;
            }
        }
        if(null == protocol) {
            protocol = Protocol.forName(
                    Preferences.instance().getProperty("connection.protocol.default"));
        }
        String username = null;
        String password = null;
        if(protocol.equals(Protocol.FTP)) {
            username = Preferences.instance().getProperty("ftp.anonymous.name");
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
        String hostname = input.substring(begin, input.length());
        String path = null;
        int port = protocol.getDefaultPort();
        if(input.indexOf(':', begin) != -1) {
            cut = input.indexOf(':', begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length() + 1;
            try {
                String portString;
                if(input.indexOf('/', begin) != -1) {
                    portString = input.substring(begin, input.indexOf('/', begin));
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
                throw new MalformedURLException("Invalid port number given");
            }
        }
        else if(input.indexOf('/', begin) != -1) {
            cut = input.indexOf('/', begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length();
            try {
                path = URLDecoder.decode(input.substring(begin, input.length()), "UTF-8");
            }
            catch(UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }
        }
        Host h = new Host(protocol,
                hostname,
                port,
                path);
        h.setCredentials(username, password);
        return h;
    }

    // ----------------------------------------------------------

    /**
     * @param defaultpath The path to change the working directory to upon connecting
     */
    public void setDefaultPath(String defaultpath) {
        if(null == defaultpath || "".equals(defaultpath)) {
            this.defaultpath = null;
        }
        else {
            this.defaultpath = Path.normalize(defaultpath, false);
        }
    }

    /**
     * @return empty string if no default path is set
     */
    public String getDefaultPath() {
        if(null == this.defaultpath) {
            return "";
        }
        return this.defaultpath;
    }

    /**
     * @return Has a non empty default path set
     */
    public boolean hasReasonableDefaultPath() {
        return !this.getDefaultPath().equals("");
    }

    // ----------------------------------------------------------
    // Accessor methods
    // ----------------------------------------------------------

    /**
     * @param username
     * @param password
     */
    public void setCredentials(String username, String password) {
        this.setCredentials(username,
                password,
                Preferences.instance().getBoolean("connection.login.useKeychain"));
    }

    /**
     * @param username
     * @param password
     * @param addToKeychain
     */
    public void setCredentials(String username, String password, boolean addToKeychain) {
        this.setCredentials(new Credentials(username, password, addToKeychain));
    }

    /**
     * @param credentials
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return this.credentials;
    }

    /**
     * @param protocol The protocol to use or null to use the default protocol for this port number
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol != null ? protocol :
                Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"));

        if(!this.protocol.equals(Protocol.SFTP)) {
            if(this.getCredentials() != null) {
                this.getCredentials().setPrivateKeyFile(null);
            }
        }
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

    /**
     * The given name for this bookmark
     *
     * @return The user-given name of this bookmark
     */
    public String getNickname() {
        if(null == this.nickname) {
            return this.getDefaultNickname();
        }
        return this.nickname;
    }

    /**
     * @return The default given name of this bookmark
     */
    private String getDefaultNickname() {
        return this.getHostname() + " \u2013 " + this.getProtocol().getName();
    }

    /**
     * Sets a user-given name for this bookmark
     *
     * @param nickname
     */
    public void setNickname(String nickname) {
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
            if(null == this.punycode) {
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
            return this.punycode;
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
        this.hostname = hostname;
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
        this.downloadFolder = folder;
    }

    /**
     * The custom folder if any or the default download location
     *
     * @return
     */
    public Local getDownloadFolder() {
        if(null == this.downloadFolder) {
            return new Local(Preferences.instance().getProperty("queue.download.folder"));
        }
        return new Local(this.downloadFolder);
    }

    /**
     * Set a timezone for the remote server different from the local default timezone
     * May be useful to display modification dates of remote files correctly using the local timezone
     *
     * @param timezone
     */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    /**
     * @return The custom timezone or the default local timezone if not set
     */
    public TimeZone getTimezone() {
        if(null == this.timezone) {
            return TimeZone.getDefault();
        }
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
        if(null == comment) {
            return "";
        }
        return this.comment;
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

    public String toString() {
        return this.toURL();
    }

    /**
     * protocol://user@host:port
     *
     * @return The URL of the remote host including user login hostname and port
     */
    public String toURL() {
        return this.getProtocol().getScheme() + "://" + this.getCredentials().getUsername() + "@"
                + this.getHostname(true) + ":" + this.getPort();
    }

    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Host) {
            Host o = (Host) other;
            return this.getNickname().equals(o.getNickname());
        }
        if(other instanceof String) {
            //hack to allow comparision in CDBrowserController#handleMountScriptCommand
            return this.getNickname().equals(other);
        }
        return false;
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + super.toString());
        super.finalize();
    }

    private static boolean JNI_LOADED = false;

    private static final Object lock = new Object();

    private static boolean jni_load() {
        synchronized(lock) {
            if(!JNI_LOADED) {
                try {
                    NSBundle bundle = NSBundle.mainBundle();
                    String lib = bundle.resourcePath() + "/Java/" + "libDiagnostics.dylib";
                    log.info("Locating libDiagnostics.dylib at '" + lib + "'");
                    System.load(lib);
                    JNI_LOADED = true;
                    log.info("libDiagnostics.dylib loaded");
                }
                catch(UnsatisfiedLinkError e) {
                    log.error("Could not load the libDiagnostics.dylib library:" + e.getMessage());
                }
            }
            return JNI_LOADED;
        }
    }

    /**
     * @return True if the host is reachable. Returns false if there is a
     *         network configuration error, no such host is known or the server does
     *         not listing at any such port
     * @see #toURL
     */
    public boolean isReachable() {
        if(!Host.jni_load()) {
            return false;
        }
        if(!Preferences.instance().getBoolean("connection.hostname.check")) {
            return true;
        }
        return this.isReachable(this.toURL());
    }

    private native boolean isReachable(String url);

    /**
     * Opens the network configuration assistant for the URL denoting this host
     *
     * @see #toURL
     */
    public void diagnose() {
        if(!Host.jni_load()) {
            return;
        }
        this.diagnose(this.toURL());
    }

    private native void diagnose(String url);

    /**
     * @return The imported bookmark deserialized as a #Host
     * @pre Host#setFile()
     */
    private void read() throws IOException {
        log.info("Reading bookmark from:" + file);
        NSData plistData;
        try {
            plistData = new NSData(new URL(file.toURL()));
        }
        catch(MalformedURLException e) {
            throw new IOException(e.getMessage());
        }
        final String[] errorString = new String[]{null};
        Object propertyListFromXMLData =
                NSPropertyListSerialization.propertyListFromData(plistData,
                        NSPropertyListSerialization.PropertyListImmutable,
                        new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                        errorString);
        if(StringUtils.hasText(errorString[0])) {
            throw new IOException("Problem reading bookmark file:" + errorString[0]);
        }
        if(propertyListFromXMLData instanceof NSDictionary) {
            this.init((NSDictionary) propertyListFromXMLData);
        }
        else {
            throw new IOException("Invalid file format:" + file);
        }
    }


    /**
     * Serializes the bookmark to the given folder
     *
     * @pre Host#setFile()
     */
    public void write() throws IOException {
        log.info("Exporting bookmark " + this.toURL() + " to " + file);
        NSMutableData collection = new NSMutableData();
        final String[] errorString = new String[]{null};
        collection.appendData(NSPropertyListSerialization.dataFromPropertyList(this.getAsDictionary(),
                NSPropertyListSerialization.PropertyListXMLFormat,
                errorString));
        if(StringUtils.hasText(errorString[0])) {
            throw new IOException("Problem writing bookmark file:" + errorString[0]);
        }
        try {
            if(collection.writeToURL(new URL(file.toURL()), true)) {
                log.info("Bookmarks sucessfully saved in :" + file);
                NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(file.getAbsolute());
            }
            else {
                throw new IOException("Error saving bookmark to:" + file);
            }
        }
        catch(MalformedURLException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     */
    public void setFile(Local file) {
        this.file = file;
    }

    /**
     * @return Null if bookmark is not persisted
     */
    public Local getFile() {
        return this.file;
    }
}