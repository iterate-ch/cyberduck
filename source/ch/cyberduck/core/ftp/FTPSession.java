package ch.cyberduck.core.ftp;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.io.IOException;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPMessageListener;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * Opens a connection to the remote server via ftp protocol
 *
 * @version $Id$
 */
public class FTPSession extends Session {
	private static Logger log = Logger.getLogger(FTPSession.class);

	static {
		SessionFactory.addFactory(Session.FTP, new Factory());
	}

	private static class Factory extends SessionFactory {
		protected Session create(Host h) {
			return new FTPSession(h);
		}
	}

	protected FTPClient FTP;
	protected FTPFileEntryParser parser;

	private FTPSession(Host h) {
		super(h);
	}

	public synchronized void close() {
		try {
			if(this.FTP != null) {
				this.log("Disconnecting...", Message.PROGRESS);
				this.FTP.quit();
				this.host.getCredentials().setPassword(null);
				this.FTP = null;
			}
		}
		catch(FTPException e) {
			log.error("FTP Error: "+e.getMessage());
		}
		catch(IOException e) {
			log.error("IO Error: "+e.getMessage());
		}
		finally {
			this.log("Disconnected", Message.PROGRESS);
			this.setClosed();
		}
	}

	public synchronized void connect(String encoding) throws IOException {
		this.log("Opening FTP connection to "+host.getIp()+"...", Message.PROGRESS);
		this.setConnected();
		this.log("=====================================", Message.TRANSCRIPT);
		this.log(new java.util.Date().toString(), Message.TRANSCRIPT);
		this.log(host.getIp(), Message.TRANSCRIPT);
		this.FTP = new FTPClient(host.getHostname(),
		    host.getPort(),
		    Preferences.instance().getInteger("connection.timeout"), //timeout
		    encoding);
		this.FTP.setMessageListener(new FTPMessageListener() {
			public void logCommand(String cmd) {
				FTPSession.this.log(cmd, Message.TRANSCRIPT);
			}

			public void logReply(String reply) {
				FTPSession.this.log(reply, Message.TRANSCRIPT);
			}
		});
		this.FTP.setStrictReturnCodes(true);
		if(Proxy.isSOCKSProxyEnabled()) {
			log.info("Using SOCKS Proxy");
			this.FTP.initSOCKS(Proxy.getSOCKSProxyPort(),
			    Proxy.getSOCKSProxyHost());
			if(Proxy.isSOCKSAuthenticationEnabled()) {
				log.info("Using SOCKS Proxy Authentication");
				this.FTP.initSOCKSAuthentication(Proxy.getSOCKSProxyUser(),
				    Proxy.getSOCKSProxyPassword());
			}
		}
		if(Preferences.instance().getProperty("ftp.connectmode").equals("active")) {
			this.FTP.setConnectMode(FTPConnectMode.ACTIVE);
		}
		else {
			this.FTP.setConnectMode(FTPConnectMode.PASV);
		}
		this.log("FTP connection opened", Message.PROGRESS);
		this.login();
		if(Preferences.instance().getBoolean("ftp.sendSystemCommand")) {
			this.host.setIdentification(this.FTP.system());
		}
		this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(this.host.getIdentification());
	}

	private synchronized void login() throws IOException {
		log.debug("login");
		Login credentials = host.getCredentials();
		if(credentials.check()) {
			try {
				this.log("Authenticating as "+host.getCredentials().getUsername()+"...", Message.PROGRESS);
				this.FTP.login(credentials.getUsername(), credentials.getPassword());
				credentials.addInternetPasswordToKeychain();
				this.setAuthenticated();
				this.log("Login successful", Message.PROGRESS);
			}
			catch(FTPException e) {
				this.log("Login failed", Message.PROGRESS);
				host.setCredentials(credentials.promptUser("Authentication for user "+credentials.getUsername()+" failed. The server response is: "+e.getMessage()));
				if(host.getCredentials().tryAgain()) {
					this.login();
				}
				else {
					throw new FTPException("Login as user "+credentials.getUsername()+" canceled.");
				}
			}
		}
		else {
			throw new FTPException("Login as user "+host.getCredentials().getUsername()+" failed.");
		}
	}

	public synchronized Path workdir() {
		try {
			this.check();
			Path workdir = PathFactory.createPath(this, this.FTP.pwd());
			workdir.attributes.setType(Path.DIRECTORY_TYPE);
			return workdir;
		}
		catch(FTPException e) {
			this.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			this.log("IO Error: "+e.getMessage(), Message.ERROR);
			this.close();
		}
		return null;
	}

	public synchronized void noop() throws IOException {
		if(this.isConnected()) {
			this.FTP.noop();
		}
	}

	public synchronized void check() throws IOException {
		this.log("Working", Message.START);
		if(null == this.FTP) {
			this.connect();
			return;
		}
		this.host.getIp();
		if(!this.FTP.isAlive()) {
			this.close();
			this.connect();
		}
	}
}