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

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.StringPrepParseException;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class Host extends NSObject {
    private static Logger log = Logger.getLogger(Host.class);

    /**
     * The protocol identifier. Must be one of <code>sftp</code>, <code>ftp</code> or <code>ftps</code>
     * @see ch.cyberduck.core.Session#FTP
     * @see ch.cyberduck.core.Session#FTP_TLS
     * @see ch.cyberduck.core.Session#SFTP
     */
    private String protocol;
    /**
     * The port number to connect to
     * @see ch.cyberduck.core.Session#FTP_PORT
     * @see ch.cyberduck.core.Session#SSH_PORT
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
    private Login login;
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

    /**
     * @param dict
     */
    public Host(NSDictionary dict) {
        this.init(dict);
    }

    /**
     * 
     * @param dict
     */
    public void init(NSDictionary dict) {
        Object protocolObj = dict.objectForKey(Host.PROTOCOL);
        if(protocolObj != null) {
            this.setProtocol((String) protocolObj);
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
            this.setPort(Integer.parseInt((String) portObj));
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
            this.setMaxConnections(Integer.valueOf((String) connObj));
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
        dict.setObjectForKey(this.getProtocol(), Host.PROTOCOL);
        dict.setObjectForKey(this.getNickname(), Host.NICKNAME);
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
        if(this.getProtocol().equals(Session.FTP) || this.getProtocol().equals(Session.FTP_TLS)) {
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

    public Object clone() {
        Host h = new Host(this.getAsDictionary());
        h.setCredentials((Login) this.getCredentials().clone());
        return h;
    }

    /**
     * New host with the default protocol
     *
     * @param hostname The hostname of the server
     */
    public Host(String hostname) {
        this(Preferences.instance().getProperty("connection.protocol.default"), hostname);
    }

    /**
     * New host with the default protocol for this port
     *
     * @param hostname The hostname of the server
     * @param port     The port number to connect to
     */
    public Host(String hostname, int port) {
        this(getDefaultProtocol(port), hostname, port);
    }

    /**
     * @param protocol
     * @param hostname
     */
    public Host(String protocol, String hostname) {
        this(protocol, hostname, getDefaultPort(protocol));
    }

    /**
     * @param protocol The protocol to use, must be either Session.FTP or Session.SFTP
     * @param hostname The hostname of the server
     * @param port     The port number to connect to
     */
    public Host(String protocol, String hostname, int port) {
        this(protocol, hostname, port, null);
    }

    /**
     * @param protocol
     * @param hostname
     * @param port
     * @param defaultpath
     */
    public Host(String protocol, String hostname, int port, String defaultpath) {
        this.setProtocol(protocol);
        this.setPort(port);
        this.setHostname(hostname);
        this.setNickname(nickname);
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
        if(null == input || input.length() == 0)
            throw new MalformedURLException("No hostname given");
        int begin = 0;
        int cut = 0;
        if(input.indexOf("://", begin) == -1 && input.indexOf('@', begin) == -1) {
            throw new MalformedURLException("No protocol or user delimiter");
        }
        String protocol = Preferences.instance().getProperty("connection.protocol.default");
        if(input.indexOf("://", begin) != -1) {
            cut = input.indexOf("://", begin);
            protocol = input.substring(begin, cut);
            begin += protocol.length() + 3;
        }
        String username = null;
        String password = null;
        if(protocol.equals(Session.FTP)) {
            username = Preferences.instance().getProperty("ftp.anonymous.name");
        }
        else if(protocol.equals(Session.FTP_TLS)) {
            username = Preferences.instance().getProperty("connection.login.name");
        }
        else if(protocol.equals(Session.SFTP)) {
            username = Preferences.instance().getProperty("connection.login.name");
        }
        else {
            throw new MalformedURLException("Unknown protocol: " + protocol);
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
        int port = getDefaultPort(protocol);
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

    /**
     * @param port
     * @return The standard protocol for this port number
     */
    protected static String getDefaultProtocol(int port) {
        switch(port) {
            case Session.FTP_PORT:
                return Session.FTP;
            case Session.SSH_PORT:
                return Session.SFTP;
        }
        log.warn("Cannot find default protocol for port number " + port);
        return Preferences.instance().getProperty("connection.protocol.default");
    }

    /**
     * @param protocol
     * @return The default port for this protocol
     */
    private static int getDefaultPort(String protocol) {
        if(protocol.equals(Session.FTP)) {
            return Session.FTP_PORT;
        }
        if(protocol.equals(Session.FTP_TLS)) {
            return Session.FTP_PORT;
        }
        if(protocol.equals(Session.SFTP)) {
            return Session.SSH_PORT;
        }
        log.warn("Cannot find default port number for protocol " + protocol);
        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP)) {
            return Session.FTP_PORT;
        }
        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP_TLS)) {
            return Session.FTP_PORT;
        }
        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.SFTP)) {
            return Session.SSH_PORT;
        }
        throw new IllegalArgumentException("Unsupported protocol: " + protocol);
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
        this.setCredentials(new Login(username, password, addToKeychain));
    }

    /**
     * @param login
     */
    public void setCredentials(Login login) {
        this.login = login;
    }

    public Login getCredentials() {
        return this.login;
    }

    /**
     * @param protocol The protocol to use or null to use the default protocol for this port number
     */
    public void setProtocol(String protocol) {
        if(null != this.protocol) {
            if(this.getNickname().equals(this.getDefaultNickname())) {
                //Revert the last default nickname set
                this.setNickname(null);
            }
        }
        this.protocol = protocol != null ? protocol :
                Preferences.instance().getProperty("connection.protocol.default");

        if(!this.protocol.equals(Session.SFTP)) {
            if(this.getCredentials() != null) {
                this.getCredentials().setPrivateKeyFile(null);
            }
        }
    }

    /**
     * @return
     * @see Session#FTP
     * @see Session#FTP_TLS
     * @see Session#SFTP
     */
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * The given name for this bookmark
     *
     * @return The user-given name of this bookmark
     */
    public String getNickname() {
        if(null == this.nickname) {
            this.setNickname(this.getDefaultNickname());
        }
        return this.nickname;
    }

    /**
     * @return The default given name of this bookmark
     */
    private String getDefaultNickname() {
        return this.getHostname() + " (" + this.getProtocol().toUpperCase() + ")";
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
                    log.info("IDN hostname for "+this.hostname+":"+idn);
                    this.punycode = idn;
                }
                catch(StringPrepParseException e) {
                    log.error("Cannot convert hostname to IDNA:"+e.getMessage());
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
        if(null != this.hostname) {
            if(this.getNickname().equals(this.getDefaultNickname())) {
                //Revert the last default nickname set
                this.setNickname(null);
            }
        }
        this.hostname = hostname;
        this.punycode = null;
    }

    /**
     * @param port The port number to connect to or -1 to use the default port for this protocol
     */
    public void setPort(int port) {
        this.port = port;
        if(-1 == port) {
            this.port = Host.getDefaultPort(this.getProtocol());
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
     *
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     *
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
        return this.getURL();
    }

    /**
     * protocol://user@host:port
     *
     * @return The URL of the remote host including user login hostname and port
     */
    public String getURL() {
        return this.getProtocol() + "://" + this.getCredentials().getUsername() + "@" + this.getHostname(true) + ":" + this.getPort();
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
     * @see #getURL
     */
    public boolean isReachable() {
        if(!Host.jni_load()) {
            return false;
        }
        if(!Preferences.instance().getBoolean("connection.hostname.check")) {
            return true;
        }
        return this.isReachable(this.getURL());
    }

    private native boolean isReachable(String url);

    /**
     * Opens the network configuration assistant for the URL denoting this host
     *
     * @see #getURL
     */
    public void diagnose() {
        if(!Host.jni_load()) {
            return;
        }
        this.diagnose(this.getURL());
    }

    private native void diagnose(String url);
}
