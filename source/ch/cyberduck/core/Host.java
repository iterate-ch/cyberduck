package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.http.HTTPSession;
import ch.cyberduck.core.sftp.SFTPSession;

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import com.sshtools.j2ssh.transport.HostKeyVerification;

import org.apache.log4j.Logger;

public class Host {
	private static Logger log = Logger.getLogger(Host.class);

	private String protocol;
	private int port;
	private String hostname;
	private String nickname;
	private String defaultpath = Path.HOME;
	private transient HostKeyVerification hostKeyVerification;
	private transient Login login;

	public static final String HOSTNAME = "Hostname";
	public static final String NICKNAME = "Nickname";
	public static final String PORT = "Port";
	public static final String PROTOCOL = "Protocol";
	public static final String USERNAME = "Username";
	public static final String PATH = "Path";
	public static final String KEYFILE = "Private Key File";

	public Host(NSDictionary dict) {
		this((String) dict.objectForKey(Host.PROTOCOL),
		    (String) dict.objectForKey(Host.HOSTNAME),
		    Integer.parseInt((String) dict.objectForKey(Host.PORT)),
		    new Login((String) dict.objectForKey(Host.HOSTNAME), (String) dict.objectForKey(Host.USERNAME)),
		    (String) dict.objectForKey(Host.PATH),
		    (String) dict.objectForKey(Host.NICKNAME)
		);
		this.getLogin().setPrivateKeyFile((String) dict.objectForKey(Host.KEYFILE));
	}

	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.getProtocol(), Host.PROTOCOL);
		dict.setObjectForKey(this.getNickname(), Host.NICKNAME);
		dict.setObjectForKey(this.getHostname(), Host.HOSTNAME);
		dict.setObjectForKey(this.getPort() + "", Host.PORT);
		dict.setObjectForKey(this.getLogin().getUsername(), Host.USERNAME);
		dict.setObjectForKey(this.getDefaultPath(), Host.PATH);
		if (this.getLogin().getPrivateKeyFile() != null)
			dict.setObjectForKey(this.getLogin().getPrivateKeyFile(), Host.KEYFILE);
		return dict;
	}

	/**
	 * For internal use only.
	 * @param url Must be in the format protocol://user@hostname:portnumber
	 */
	public Host(String url) throws java.net.MalformedURLException {
		try {
			this.protocol = url.substring(0, url.indexOf("://"));
			this.hostname = url.substring(url.indexOf("@") + 1, url.lastIndexOf(":"));
			this.port = Integer.parseInt(url.substring(url.lastIndexOf(":") + 1, url.length()));
			this.login = new Login(hostname, url.substring(url.indexOf("://") + 3, url.lastIndexOf("@")));
			this.nickname = this.getLogin().getUsername() + "@" + this.getHostname();
		}
		catch (NumberFormatException e) {
			log.error(e.getMessage());
			throw new java.net.MalformedURLException("Not a valid URL: " + url);
		}
		catch (IndexOutOfBoundsException e) {
			log.error(e.getMessage());
			throw new java.net.MalformedURLException("Not a valid URL: " + url);
		}
		log.debug(this.toString());
	}

	/**
	 * New host with the default protocol and port
	 * @param hostname The hostname of the server
	 * @param  login The login credentials to use
	 */
	public Host(String hostname, Login login) {
		this(Preferences.instance().getProperty("connection.protocol.default"), hostname, Integer.parseInt(Preferences.instance().getProperty("connection.port.default")), login);
	}

	/**
	 * New host with the default protocol for this port
	 * @param hostname The hostname of the server
	 * @param  login The login credentials to use
	 */
	public Host(String hostname, int port, Login login) {
		this(getDefaultProtocol(port), hostname, port, login);
	}

//	public Host(String protocol, String hostname, int port, String path, String nickname)

	/**
	 * @param protocol The protocol to use, must be either Session.HTTP, Session.FTP or Session.SFTP
	 * @param hostname The hostname of the server
	 * @param port The port number to connect to
	 * @param  login The login credentials to use
	 */
	public Host(String protocol, String hostname, int port, Login login) {
		this(protocol, hostname, port, login, "", null);
	}

	public Host(String protocol, String hostname, int port, Login login, String defaultpath) {
		this(protocol, hostname, port, login, defaultpath, null);
	}

	public Host(String hostname, int port, Login login, String nickname) {
		this(getDefaultProtocol(port), hostname, port, login, "", nickname);
	}

	public Host(String protocol, String hostname, int port, Login login, String defaultpath, String nickname) {
		this.setProtocol(protocol);
		this.setPort(port);
		this.setHostname(hostname);
		this.setLogin(login);
		this.setNickname(nickname);
		this.setDefaultPath(defaultpath);
		log.debug(this.toString());
	}

	// ----------------------------------------------------------

	/*
	public Session createSession() {
		log.debug("createSession");
		if (this.getProtocol().equalsIgnoreCase(Session.HTTP)) {
			return new HTTPSession(this);
		}
		//  if(this.getProtocol().equalsIgnoreCase(Session.HTTPS)) {
//            return new HTTPSession(this);
//        }
		else if (this.getProtocol().equalsIgnoreCase(Session.FTP)) {
			return new FTPSession(this);
		}
		else if (this.getProtocol().equalsIgnoreCase(Session.SFTP)) {
			return new SFTPSession(this);
		}
		else {
			throw new IllegalArgumentException("Unknown protocol");
		}
	}
	 */

	public void setDefaultPath(String defaultpath) {
		this.defaultpath = defaultpath;
	}

	public String getDefaultPath() {
		return this.defaultpath;
	}

	public boolean hasReasonableDefaultPath() {
		return this.defaultpath != null && !this.defaultpath.equals("") && !this.defaultpath.equals(Path.HOME);
//        log.debug("hasReasonableDefaultPath:"+reasonable+"("+defaultpath+")");
//		return reasonable;
	}

//    public void closeSession() {
//      log.debug("closeSession");
//		if(session != null) {
//			this.session.close();
//			this.session = null;
//		}
//    }

	protected static String getDefaultProtocol(int port) {
		switch (port) {
			case Session.HTTP_PORT:
				return Session.HTTP;
			case Session.FTP_PORT:
				return Session.FTP;
			case Session.SSH_PORT:
				return Session.SFTP;
			default:
				throw new IllegalArgumentException("Cannot find protocol for port number " + port);
		}

	}

	private static int getDefaultPort(String protocol) {
		if (protocol.equals(Session.FTP))
			return Session.FTP_PORT;
		else if (protocol.equals(Session.SFTP))
			return Session.SSH_PORT;
		else if (protocol.equals(Session.HTTP))
			return Session.HTTP_PORT;
		throw new IllegalArgumentException("Cannot find port number for protocol " + protocol);
	}

	// ----------------------------------------------------------
	// Accessor methods
	// ----------------------------------------------------------

	public void setLogin(Login login) {
		this.login = login;
	}

	public Login getLogin() {
		return this.login;
	}

	/**
	 * @param protocol The protocol to use or null to use the default protocol for this port number
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol != null ? protocol : Preferences.instance().getProperty("connection.protocol.default");
	}

	public String getProtocol() {
		return this.protocol;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
//		log.debug("setNickname:"+nickname);
		this.nickname = nickname != null ? nickname : this.getHostname() + " (" + this.getProtocol().toUpperCase() + ")";
	}

	public String getHostname() {
		return this.hostname;
	}

	public void setHostname(String hostname) {
//		log.debug("setHostname:"+hostname);
		this.hostname = hostname;
	}

	/**
	 * @param port The port number to connect to or -1 to use the default port for this protocol
	 */
	public void setPort(int port) {
		this.port = port != -1 ? port : this.getDefaultPort(this.getProtocol());
	}

	public int getPort() {
		return this.port;
	}

	//ssh specific
	public void setHostKeyVerificationController(HostKeyVerification h) {
		this.hostKeyVerification = h;
	}

	public HostKeyVerification getHostKeyVerificationController() {
		return this.hostKeyVerification;
	}

	/**
	 * @return The IP address of the remote host if available
	 */
	public String getIp() {
		//if we call getByName(null) InetAddress would return localhost
		if (this.hostname == null)
			return "Unknown host";
		try {
			return java.net.InetAddress.getByName(hostname).toString();
		}
		catch (java.net.UnknownHostException e) {
			return "Unknown host";
		}
	}

	public String toString() {
		return this.getURL();
	}

	/**
	 * protocol://user@host:port
	 @return The URL of the remote host including user login hostname and port
	 */
	public String getURL() {
		return this.getProtocol() + "://" + this.getLogin().getUsername() + "@" + this.getHostname() + ":" + this.getPort() + "/" + this.getDefaultPath();
	}

	public boolean equals(Object other) {
		return this.toString().equals(other.toString());
	}
}
