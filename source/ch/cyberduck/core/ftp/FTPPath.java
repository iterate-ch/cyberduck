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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.apple.cocoa.foundation.NSDictionary;

import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.log4j.Logger;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class FTPPath extends Path {
	private static Logger log = Logger.getLogger(FTPPath.class);

	private static final String DOS_LINE_SEPARATOR = "\r\n";
	private static final String MAC_LINE_SEPARATOR = "\r";
	private static final String UNIX_LINE_SEPARATOR = "\n";

	static {
		PathFactory.addFactory(Session.FTP, new Factory());
	}

	private static class Factory extends PathFactory {
		protected Path create(Session session, String parent, String name) {
			return new FTPPath((FTPSession)session, parent, name);
		}

		protected Path create(Session session) {
			return new FTPPath((FTPSession)session);
		}

		protected Path create(Session session, String path) {
			return new FTPPath((FTPSession)session, path);
		}

		protected Path create(Session session, String path, Local file) {
			return new FTPPath((FTPSession)session, path, file);
		}

		protected Path create(Session session, NSDictionary dict) {
			return new FTPPath((FTPSession)session, dict);
		}
	}

	private FTPSession session;

	/**
	 * @param session The connection to work with for regular file operations
	 * @param parent  The parent directory relative to this file
	 * @param name    The filename of this path
	 */
	private FTPPath(FTPSession s, String parent, String name) {
		super(parent, name);
		this.session = s;
	}

	private FTPPath(FTPSession s, String path) {
		super(path);
		this.session = s;
	}

	private FTPPath(FTPSession s) {
		super();
		this.session = s;
	}

	/**
	 * @param session The connection to work with for regular file operations
	 * @param parent  The parent directory relative to this file
	 * @param file    The corresponding local file to the remote path
	 */
	private FTPPath(FTPSession s, String parent, Local file) {
		super(parent, file);
		this.session = s;
	}

	private FTPPath(FTPSession s, NSDictionary dict) {
		super(dict);
		this.session = s;
	}

	public Session getSession() {
		return this.session;
	}

	public List list(String encoding, boolean refresh, boolean showHidden) {
		synchronized(session) {
			List files = session.cache().get(this.getAbsolute());
			session.addPathToHistory(this);
			if(refresh || null == files) {
				files = new ArrayList();
				session.log("Listing "+this.getAbsolute(), Message.PROGRESS);
				try {
					session.check();
					session.FTP.setTransferType(FTPTransferType.ASCII);
					session.FTP.chdir(this.getAbsolute());
					String[] lines = session.FTP.dir(encoding);
					for(int i = 0; i < lines.length; i++) {
						Path p = session.parser.parseFTPEntry(this, lines[i]);
						if(p != null) {
							String filename = p.getName();
							if(!(filename.charAt(0) == '.') || showHidden) {
								files.add(p);
							}
						}
					}
					session.cache().put(this.getAbsolute(), files);
					session.log("Idle", Message.STOP);
				}
				catch(FTPException e) {
					session.log("FTP Error: "+e.getMessage(), Message.ERROR);
					return files;
				}
				catch(IOException e) {
					session.log("IO Error: "+e.getMessage(), Message.ERROR);
					session.close();
					return files;
				}
			}
			session.callObservers(this);
			return files;
		}
	}

	public void cwdir() throws IOException {
		synchronized(session) {
			session.FTP.chdir(this.getAbsolute());
		}
	}
	
	public void mkdir(boolean recursive) {
		synchronized(session) {
			log.debug("mkdir:"+this.getName());
			try {
				if(recursive) {
					if(!this.getParent().exists()) {
						this.getParent().mkdir(recursive);
					}
				}
				session.check();
				session.log("Make directory "+this.getName(), Message.PROGRESS);
				session.FTP.mkdir(this.getAbsolute());
				session.cache().put(this.getAbsolute(), new ArrayList());
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(FTPException e) {
				session.log("FTP Error: "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
			}
		}
	}
	
	public void rename(String filename) {
		synchronized(session) {
			log.debug("rename:"+filename);
			try {
				session.check();
				session.log("Renaming "+this.getName()+" to "+filename, Message.PROGRESS);
				session.FTP.rename(this.getAbsolute(), filename);
				this.setPath(filename);
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(FTPException e) {
				session.log("FTP Error: "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
			}
		}
	}
	
	public void reset() {
		synchronized(session) {
			if(this.attributes.isFile() && this.attributes.isUndefined()) {
				if(this.exists()) {
					try {
						session.check();
						session.log("Getting timestamp of "+this.getName(), Message.PROGRESS);
						this.attributes.setTimestamp(session.FTP.modtime(this.getAbsolute()));
						session.log("Getting size of "+this.getName(), Message.PROGRESS);
						if(Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
							if(this.getExtension() != null && Preferences.instance().getProperty("ftp.transfermode.ascii.extensions").indexOf(this.getExtension()) != -1) {
								session.FTP.setTransferType(FTPTransferType.ASCII);
							}
							else {
								session.FTP.setTransferType(FTPTransferType.BINARY);
							}
						}
						else if(Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
							session.FTP.setTransferType(FTPTransferType.BINARY);
						}
						else if(Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
							session.FTP.setTransferType(FTPTransferType.ASCII);
						}
						else {
							throw new FTPException("Transfer type not set");
						}
						this.attributes.setSize(session.FTP.size(this.getAbsolute()));
					}
					catch(FTPException e) {
						log.error(e.getMessage());
						//ignore
					}
					catch(IOException e) {
						session.log("IO Error: "+e.getMessage(), Message.ERROR);
						session.close();
					}
				}
			}
		}
	}
	
	public void delete() {
		synchronized(session) {
			log.debug("delete:"+this.toString());
			try {
				session.check();
				if(this.attributes.isFile()) {
					session.FTP.chdir(this.getParent().getAbsolute());
					session.log("Deleting "+this.getName(), Message.PROGRESS);
					session.FTP.delete(this.getName());
				}
				else if(this.attributes.isDirectory()) {
					List files = this.list(true, true);
					java.util.Iterator iterator = files.iterator();
					Path file = null;
					while(iterator.hasNext()) {
						file = (Path)iterator.next();
						if(file.attributes.isFile()) {
							session.log("Deleting "+this.getName(), Message.PROGRESS);
							session.FTP.delete(file.getName());
						}
						if(file.attributes.isDirectory()) {
							file.delete();
						}
					}
					session.FTP.cdup();
					session.log("Deleting "+this.getName(), Message.PROGRESS);
					session.FTP.rmdir(this.getName());
				}
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(FTPException e) {
				session.log("FTP Error: "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
			}
		}
	}
	
	public void changePermissions(Permission perm, boolean recursive) {
		synchronized(session) {
			log.debug("changePermissions:"+perm);
			String command = "chmod";
			try {
				session.check();
				if(this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
					session.log("Changing permission to "+perm.getOctalCode()+" on "+this.getName(), Message.PROGRESS);
					session.FTP.site(command+" "+perm.getOctalCode()+" "+this.getAbsolute());
				}
				else if(this.attributes.isDirectory()) {
					session.log("Changing permission to "+perm.getOctalCode()+" on "+this.getName(), Message.PROGRESS);
					session.FTP.site(command+" "+perm.getOctalCode()+" "+this.getAbsolute());
					if(recursive) {
						List files = this.list(false, true);
						java.util.Iterator iterator = files.iterator();
						Path file = null;
						while(iterator.hasNext()) {
							file = (Path)iterator.next();
							file.changePermissions(perm, recursive);
						}
					}
				}
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(FTPException e) {
				session.log("FTP Error: "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
			}
		}
	}
	
	public void download() {
		synchronized(session) {
			log.debug("download:"+this.toString());
			try {
				session.check();
				if(this.attributes.isFile()) {
					if(Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
						if(this.getExtension() != null && Preferences.instance().getProperty("ftp.transfermode.ascii.extensions").indexOf(this.getExtension()) != -1) {
							this.downloadASCII();
						}
						else {
							this.downloadBinary();
						}
					}
					else if(Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
						this.downloadBinary();
					}
					else if(Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
						this.downloadASCII();
					}
					else {
						throw new FTPException("Transfer mode not set");
					}
					if(this.status.isComplete()) {
						log.info("Updating permissions");
						if(Preferences.instance().getProperty("queue.download.changePermissions").equals("true")) {
							Permission perm = null;
							if(Preferences.instance().getProperty("queue.download.permissions.useDefault").equals("true")) {
								perm = new Permission(Preferences.instance().getProperty("queue.download.permissions.default"));
							}
							else {
								perm = this.attributes.getPermission();
							}
							if(!perm.isUndefined()) {
								this.getLocal().setPermission(perm);
							}
						}
					}
					if(Preferences.instance().getProperty("queue.download.preserveDate").equals("true")) {
						this.getLocal().setLastModified(this.attributes.getTimestamp().getTime());
					}
				}
				if(this.attributes.isDirectory()) {
					this.getLocal().mkdir();
				}
				session.log("Idle", Message.STOP);
			}
			catch(FTPException e) {
				session.log("FTP Error: ("+this.getName()+") "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
			}
		}
	}
	
	private void downloadBinary() throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			session.FTP.setTransferType(FTPTransferType.BINARY);
			if(this.status.isResume()) {
				this.status.setCurrent(this.getLocal().getSize());
			}
			out = new FileOutputStream(this.getLocal(), this.status.isResume());
			if(out == null) {
				throw new IOException("Unable to buffer data");
			}
			in = session.FTP.get(this.getAbsolute(), this.status.isResume() ? this.getLocal().getSize() : 0);
			if(in == null) {
				throw new IOException("Unable opening data stream");
			}
			this.download(in, out);
			if(this.status.isComplete()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.validateTransfer();
			}
			if(this.status.isCanceled()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.abor();
			}
			session.log("Idle", Message.STOP);
		}
		finally {
			try {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void downloadASCII() throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			String lineSeparator = System.getProperty("line.separator"); //default value
			if(Preferences.instance().getProperty("ftp.line.separator").equals("unix")) {
				lineSeparator = UNIX_LINE_SEPARATOR;
			}
			else if(Preferences.instance().getProperty("ftp.line.separator").equals("mac")) {
				lineSeparator = MAC_LINE_SEPARATOR;
			}
			else if(Preferences.instance().getProperty("ftp.line.separator").equals("win")) {
				lineSeparator = DOS_LINE_SEPARATOR;
			}
			session.FTP.setTransferType(FTPTransferType.ASCII);
			if(this.status.isResume()) {
				this.status.setCurrent(this.getLocal().getSize());
			}
			out = new FromNetASCIIOutputStream(new FileOutputStream(this.getLocal(),
			    this.status.isResume()),
			    lineSeparator);
			if(out == null) {
				throw new IOException("Unable to buffer data");
			}
			in = new FromNetASCIIInputStream(session.FTP.get(this.getAbsolute(),
			    this.status.isResume() ? this.getLocal().getSize() : 0),
			    lineSeparator);
			if(in == null) {
				throw new IOException("Unable opening data stream");
			}
			this.download(in, out);
			if(this.status.isComplete()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.validateTransfer();
			}
			if(this.status.isCanceled()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.abor();
			}
			session.log("Idle", Message.STOP);
		}
		finally {
			try {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public void upload() {
		synchronized(session) {
			log.debug("upload:"+this.toString());
			try {
				session.check();
				if(this.attributes.isFile()) {
					this.attributes.setSize(this.getLocal().getSize());
					if(Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
						if(this.getExtension() != null && Preferences.instance().getProperty("ftp.transfermode.ascii.extensions").indexOf(this.getExtension()) != -1) {
							this.uploadASCII();
						}
						else {
							this.uploadBinary();
						}
					}
					else if(Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
						this.uploadBinary();
					}
					else if(Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
						this.uploadASCII();
					}
					else {
						throw new FTPException("Transfer mode not set");
					}
					if(Preferences.instance().getProperty("queue.upload.changePermissions").equals("true")) {
						try {
							if(Preferences.instance().getProperty("queue.upload.permissions.useDefault").equals("true")) {
								Permission perm = new Permission(Preferences.instance().getProperty("queue.upload.permissions.default"));
								session.FTP.site("CHMOD "+perm.getOctalCode()+" "+this.getAbsolute());
							}
							else {
								Permission perm = this.getLocal().getPermission();
								if(!perm.isUndefined()) {
									session.FTP.site("CHMOD "+perm.getOctalCode()+" "+this.getAbsolute());
								}
							}
						}
						catch(FTPException e) {
							log.warn(e.getMessage());
						}
					}
					if(Preferences.instance().getProperty("queue.upload.preserveDate").equals("true")) {
						try {
							session.FTP.setmodtime(this.getLocal().getTimestamp(), this.getAbsolute());
						}
						catch(FTPException e) {
							log.warn(e.getMessage());
						}
					}
				}
				if(this.attributes.isDirectory()) {
					this.mkdir();
				}
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(FTPException e) {
				session.log("FTP Error: ("+this.getName()+") "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
			}
		}
	}
	
	private void uploadBinary() throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			session.FTP.setTransferType(FTPTransferType.BINARY);
			if(this.status.isResume()) {
				try {
					this.status.setCurrent(this.session.FTP.size(this.getAbsolute()));
				}
				catch(FTPException e) {
					log.error(e.getMessage());
					//ignore; SIZE command not recognized
					this.status.setCurrent(0);
				}
			}
			in = new FileInputStream(this.getLocal());
			if(in == null) {
				throw new IOException("Unable to buffer data");
			}
			out = session.FTP.put(this.getAbsolute(), this.status.isResume());
			if(out == null) {
				throw new IOException("Unable opening data stream");
			}
			this.upload(out, in);
			if(this.status.isComplete()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.validateTransfer();
			}
			if(status.isCanceled()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.abor();
			}
			session.log("Idle", Message.STOP);
		}
		finally {
			try {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void uploadASCII() throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			session.FTP.setTransferType(FTPTransferType.ASCII);
			if(this.status.isResume()) {
				try {
					this.status.setCurrent(this.session.FTP.size(this.getAbsolute()));
				}
				catch(FTPException e) {
					log.error(e.getMessage());
					//ignore; SIZE command not recognized
					this.status.setCurrent(0);
				}
			}
			in = new ToNetASCIIInputStream(new FileInputStream(this.getLocal()));
			if(in == null) {
				throw new IOException("Unable to buffer data");
			}
			out = new ToNetASCIIOutputStream(session.FTP.put(this.getAbsolute(),
			    this.status.isResume()));
			if(out == null) {
				throw new IOException("Unable opening data stream");
			}
			this.upload(out, in);
			if(this.status.isComplete()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.validateTransfer();
			}
			if(status.isCanceled()) {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
				session.FTP.abor();
			}
			session.log("Idle", Message.STOP);
		}
		finally {
			try {
				if(in != null) {
					in.close();
					in = null;
				}
				if(out != null) {
					out.close();
					out = null;
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
	