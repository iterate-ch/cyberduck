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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;
import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.InetAddress;

/**
 * @version $Id$
 */
public class Host extends NSObject {
    private static Logger log = Logger.getLogger(Host.class);

    private String protocol;
    private int port;
    private String hostname;
    private String nickname;
    private String identification;
    private String defaultpath;
    private Login login;
    private String encoding;
    private com.enterprisedt.net.ftp.FTPConnectMode connectMode;

    public static final String HOSTNAME = "Hostname";
    public static final String NICKNAME = "Nickname";
    public static final String PORT = "Port";
    public static final String PROTOCOL = "Protocol";
    public static final String USERNAME = "Username";
    public static final String PATH = "Path";
    public static final String ENCODING = "Encoding";
    public static final String KEYFILE = "Private Key File";
    public static final String FTPCONNECTMODE = "FTP Connect Mode";

    public Host(NSDictionary dict) {
        Object protocolObj = dict.objectForKey(Host.PROTOCOL);
        if (protocolObj != null) {
            this.setProtocol((String) protocolObj);
        }
        Object hostnameObj = dict.objectForKey(Host.HOSTNAME);
        if (hostnameObj != null) {
            this.setHostname((String) hostnameObj);
            Object usernameObj = dict.objectForKey(Host.USERNAME);
            if (usernameObj != null) {
                this.setCredentials((String) usernameObj, null);
            }
            this.getCredentials().setPrivateKeyFile((String) dict.objectForKey(Host.KEYFILE));
        }
        Object portObj = dict.objectForKey(Host.PORT);
        if (portObj != null) {
            this.setPort(Integer.parseInt((String) portObj));
        }
        Object pathObj = dict.objectForKey(Host.PATH);
        if (pathObj != null) {
            this.setDefaultPath((String) pathObj);
        }
        Object nicknameObj = dict.objectForKey(Host.NICKNAME);
        if (nicknameObj != null) {
            this.setNickname((String) nicknameObj);
        }
        Object encodingObj = dict.objectForKey(Host.ENCODING);
        if (encodingObj != null) {
            this.setEncoding((String) encodingObj);
        }
        Object connectModeObj = dict.objectForKey(Host.FTPCONNECTMODE);
        if (connectModeObj != null) {
            if (connectModeObj.equals("active")) {
                this.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE);
            }
            else {
                this.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.PASV);
            }
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.getProtocol(), Host.PROTOCOL);
        dict.setObjectForKey(this.getNickname(), Host.NICKNAME);
        dict.setObjectForKey(this.getHostname(), Host.HOSTNAME);
        dict.setObjectForKey(String.valueOf(this.getPort()), Host.PORT);
        dict.setObjectForKey(this.getCredentials().getUsername(), Host.USERNAME);
        dict.setObjectForKey(this.getDefaultPath(), Host.PATH);
        dict.setObjectForKey(this.getEncoding(), Host.ENCODING);
        if (this.getCredentials().getPrivateKeyFile() != null) {
            dict.setObjectForKey(this.getCredentials().getPrivateKeyFile(), Host.KEYFILE);
        }
        if (this.getProtocol().equals(Session.FTP) || this.getProtocol().equals(Session.FTP_TLS)) {
            if (this.getFTPConnectMode().equals(com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE)) {
                dict.setObjectForKey("active", Host.FTPCONNECTMODE);
            }
            else {
                dict.setObjectForKey("passive", Host.FTPCONNECTMODE);
            }
        }
        return dict;
    }

    public Object clone() {
        return new Host(this.getAsDictionary());
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
     *
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
     *
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
        if (null == input || input.length() == 0)
            throw new MalformedURLException("No hostname given");
        int begin = 0;
        int cut = 0;
        if (input.indexOf("://", begin) == -1 && input.indexOf('@', begin) == -1) {
            throw new MalformedURLException("No protocol or user delimiter");
        }
        String protocol = Preferences.instance().getProperty("connection.protocol.default");
        if (input.indexOf("://", begin) != -1) {
            cut = input.indexOf("://", begin);
            protocol = input.substring(begin, cut);
            begin += protocol.length() + 3;
        }
        String username = null;
        String password = null;
        if (protocol.equals(Session.FTP)) {
            username = Preferences.instance().getProperty("ftp.anonymous.name");
        }
        else if (protocol.equals(Session.FTP_TLS)) {
            username = Preferences.instance().getProperty("connection.login.name");
        }
        else if (protocol.equals(Session.SFTP)) {
            username = Preferences.instance().getProperty("connection.login.name");
        }
        else {
            throw new MalformedURLException("Unknown protocol: " + protocol);
        }
        if (input.indexOf('@', begin) != -1) {
            if (input.indexOf(':', begin) != -1 && input.indexOf('@', begin) > input.indexOf(':', begin)) {
                // ':' is not for the port number but username:pass seperator
                cut = input.indexOf(':', begin);
                username = input.substring(begin, cut);
                begin += username.length() + 1;
                cut = input.indexOf('@', begin);
                password = input.substring(begin, cut);
                begin += password.length() + 1;
            }
            else {
                //no password given
                cut = input.indexOf('@', begin);
                username = input.substring(begin, cut);
                begin += username.length() + 1;
            }
        }
        String hostname = input.substring(begin, input.length());
        String path = null;
        int port = getDefaultPort(protocol);
        if (input.indexOf(':', begin) != -1) {
            cut = input.indexOf(':', begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length() + 1;
            try {
                String portString;
                if (input.indexOf('/', begin) != -1) {
                    portString = input.substring(begin, input.indexOf('/', begin));
                    begin += portString.length();
                    path = input.substring(begin, input.length());
                }
                else {
                    portString = input.substring(begin, input.length());
                }
                port = Integer.parseInt(portString);
            }
            catch (NumberFormatException e) {
                throw new MalformedURLException("Invalid port number given");
            }
        }
        else if (input.indexOf('/', begin) != -1) {
            cut = input.indexOf('/', begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length();
            path = input.substring(begin, input.length());
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
     *
     * @param defaultpath The path to change the working directory to upon connecting
     */
    public void setDefaultPath(String defaultpath) {
        this.defaultpath = defaultpath;
    }

    /**
     * @return empty string if no default path is set
     */
    public String getDefaultPath() {
        if (null == this.defaultpath) {
            return "";
        }
        if (this.defaultpath.equals("")) {
            return "";
        }
        if (this.defaultpath.charAt(0) == '/') {
            return this.defaultpath;
        }
        return Path.DELIMITER + this.defaultpath;
    }

    /**
     *
     * @return Has a non empty default path set
     */
    public boolean hasReasonableDefaultPath() {
        return !this.getDefaultPath().equals("");
    }

    /**
     *
     * @param port
     * @return The standard protocol for this port number
     */
    protected static String getDefaultProtocol(int port) {
        switch (port) {
            case Session.FTP_PORT:
                return Session.FTP;
            case Session.SSH_PORT:
                return Session.SFTP;
        }
        log.warn("Cannot find default protocol for port number " + port);
        return Preferences.instance().getProperty("connection.protocol.default");
    }

    /**
     *
     * @param protocol
     * @return The default port for this protocol
     */
    private static int getDefaultPort(String protocol) {
        if (protocol.equals(Session.FTP)) {
            return Session.FTP_PORT;
        }
        if (protocol.equals(Session.FTP_TLS)) {
            return Session.FTP_PORT;
        }
        if (protocol.equals(Session.SFTP)) {
            return Session.SSH_PORT;
        }
        log.warn("Cannot find default port number for protocol " + protocol);
        if (Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP)) {
            return Session.FTP_PORT;
        }
        if (Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP_TLS)) {
            return Session.FTP_PORT;
        }
        if (Preferences.instance().getProperty("connection.protocol.default").equals(Session.SFTP)) {
            return Session.SSH_PORT;
        }
        throw new IllegalArgumentException("Unsupported protocol: " + protocol);
    }

    // ----------------------------------------------------------
    // Accessor methods
    // ----------------------------------------------------------

    /**
     *
     * @param login
     */
    public void setCredentials(Login login) {
        this.login = login;
    }

    /**
     *
     * @param username
     * @param password
     */
    public void setCredentials(String username, String password) {
        this.setCredentials(username,
                password,
                Preferences.instance().getBoolean("connection.login.useKeychain"));
    }

    /**
     *
     * @param username
     * @param password
     * @param addToKeychain
     */
    public void setCredentials(String username, String password, boolean addToKeychain) {
        this.setCredentials(new Login(this.getHostname(), this.getProtocol(), username, password, addToKeychain));
    }

    public Login getCredentials() {
        return this.login;
    }

    /**
     * @param protocol The protocol to use or null to use the default protocol for this port number
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol != null ? protocol : Preferences.instance().getProperty("connection.protocol.default");
        if (this.getProtocol().equals(Session.FTP) || this.getProtocol().equals(Session.FTP_TLS)) {
            if (Preferences.instance().getProperty("ftp.connectmode").equals("active"))
                this.connectMode = com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE;
            if (Preferences.instance().getProperty("ftp.connectmode").equals("passive"))
                this.connectMode = com.enterprisedt.net.ftp.FTPConnectMode.PASV;
        }
        if (null == this.login)
            return;
        this.login.setProtocol(this.protocol);
    }

    public String getProtocol() {
        return this.protocol;
    }

    /**
     * @return The remote host identification such as the response to the SYST command in FTP
     */
    public String getIdentification() {
        return this.identification;
    }

    /**
     *
     * @param id
     */
    public void setIdentification(String id) {
        this.identification = id;
    }

    public String getNickname() {
        if (this.nickname != null)
            return this.nickname;
        return this.getHostname() + " (" + this.getProtocol().toUpperCase() + ")";
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        if (null == this.login)
            return;
        this.login.setHostname(hostname);
    }

    /**
     * @param port The port number to connect to or -1 to use the default port for this protocol
     */
    public void setPort(int port) {
        this.port = port;
        if (-1 == port) {
            this.port = Host.getDefaultPort(this.getProtocol());
        }
    }

    public int getPort() {
        return this.port;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        if (null == this.encoding)
            this.encoding = Preferences.instance().getProperty("browser.charset.encoding");
        return this.encoding;
    }

    public void setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode connectMode) {
        this.connectMode = connectMode;
    }

    public com.enterprisedt.net.ftp.FTPConnectMode getFTPConnectMode() {
        if (null == this.connectMode) {
            if (Preferences.instance().getProperty("ftp.connectmode").equals("active"))
                this.connectMode = com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE;
            if (Preferences.instance().getProperty("ftp.connectmode").equals("passive"))
                this.connectMode = com.enterprisedt.net.ftp.FTPConnectMode.PASV;
        }
        return this.connectMode;
    }

    /**
     * @return The IP address of the remote host if available
     */
    public String getIp() throws UnknownHostException {
        try {
            return InetAddress.getByName(hostname).toString();
        }
        catch (UnknownHostException e) {
            throw new UnknownHostException(hostname+" cannot be resolved");
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
        return this.getProtocol() + "://" + this.getCredentials().getUsername() + "@" + this.getHostname() + ":" + this.getPort();
    }

    public boolean equals(Object other) {
        if (null == other) {
            return false;
        }
        if (other instanceof Host) {
            Host o = (Host) other;
            return this.getProtocol().equals(o.getProtocol())
                    && this.getCredentials().getUsername().equals(o.getCredentials().getUsername())
                    && this.getHostname().equals(o.getHostname())
                    && this.getNickname().equals(o.getNickname())
                    && this.getPort() == o.getPort()
                    && this.getDefaultPath().equals(o.getDefaultPath());
        }
        return this.toString().equals(other.toString());
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + super.toString());
        super.finalize();
    }

    private static boolean JNI_LOADED = false;

    private static final Object lock = new Object();

    private boolean jni_load() {
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
                catch (UnsatisfiedLinkError e) {
                    log.error("Could not load the libDiagnostics.dylib library:" + e.getMessage());
                }
            }
            return JNI_LOADED;
        }
    }

    /**
     *
     */
    public boolean isReachable() {
        if(!this.jni_load()) {
            return false;
        }
        boolean available = this.isReachable(this.getURL());
        if(!available) {
            log.warn("Unreachable hostname:"+this.getHostname());
        }
        return available;
    }

    /**
     *
     * @param url
     * @return
     */
    private native boolean isReachable(String url);

    /**
     *
     */
    public void diagnose() {
        if(!this.jni_load()) {
            return;
        }
        this.diagnose(this.getURL());
    }

    /**
     *
     * @param url
     */
    private native void diagnose(String url);
}
