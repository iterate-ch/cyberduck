package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.*;

import com.apple.cocoa.foundation.NSDictionary;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.commons.net.ftp.FTPFileEntryParser;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class FTPPath extends Path {
	private static Logger log = Logger.getLogger(FTPPath.class);

	static {
		PathFactory.addFactory(Session.FTP, new Factory());
	}

	private static class Factory extends PathFactory {
		protected Path create(Session session, String parent, String name) {
			return new FTPPath((FTPSession) session, parent, name);
		}

		protected Path create(Session session) {
			return new FTPPath((FTPSession) session);
		}

		protected Path create(Session session, String path) {
			return new FTPPath((FTPSession) session, path);
		}

		protected Path create(Session session, String path, Local file) {
			return new FTPPath((FTPSession) session, path, file);
		}

		protected Path create(Session session, NSDictionary dict) {
			return new FTPPath((FTPSession) session, dict);
		}
	}

	private FTPSession session;

	/**
	 * @param session The connection to work with for regular file operations
	 * @param parent The parent directory relative to this file
	 * @param name The filename of this path
	 */
	private FTPPath(FTPSession session, String parent, String name) {
		super(parent, name);
		this.session = session;
	}

	private FTPPath(FTPSession session, String path) {
		super(path);
		this.session = session;
	}

	private FTPPath(FTPSession session) {
		super();
		this.session = session;
	}
	
	/**
	 * @param session The connection to work with for regular file operations
	 * @param parent The parent directory relative to this file
	 * @param file The corresponding local file to the remote path
	 */
	private FTPPath(FTPSession session, String parent, Local file) {
		super(parent, file);
		this.session = session;
	}

	private FTPPath(FTPSession session, NSDictionary dict) {
		super(dict);
		this.session = session;
	}

	public Session getSession() {
		return this.session;
	}
		 
	public synchronized List list() {
		return this.list(false);
	}
	
	public synchronized List list(boolean refresh) {
		return this.list(refresh, Preferences.instance().getProperty("browser.showHidden").equals("true"));
	}
	
	public synchronized List list(boolean refresh, boolean showHidden) {
		List files = this.cache();
		session.addPathToHistory(this);
		if(refresh || files.size() == 0) {
			files.clear();
			session.log("Listing " + this.getAbsolute(), Message.PROGRESS);
			try {
				session.check();
				session.FTP.setTransferType(FTPTransferType.ASCII);
				session.FTP.chdir(this.getAbsolute());

				FTPFileEntryParser parser =  new DefaultFTPFileEntryParserFactory().createFileEntryParser(session.host.getIdentification());
				
				String[] lines = session.FTP.dir();
				for (int i = 0; i < lines.length; i++) {
					Path p = parser.parseFTPEntry(this, lines[i]);
					if(p != null) {
						String filename = p.getName();
						if (!(filename.equals(".") || filename.equals(".."))) {
							if (!(filename.charAt(0) == '.') || showHidden) {
								files.add(p);
							}
						}
					}
				}
				
				this.setCache(files);
			}
			catch (FTPException e) {
				session.log("FTP Error: " + e.getMessage(), Message.ERROR);
			}
			catch (IOException e) {
				session.log("IO Error: " + e.getMessage(), Message.ERROR);
			}
			finally {
				session.log("Idle", Message.STOP);
			}
		}
		session.callObservers(this);
		return files;
	}
	
	public void delete() {
		log.debug("delete:" + this.toString());
		try {
			session.check();
			if (this.isFile()) {
				session.log("Deleting " + this.getName(), Message.PROGRESS);
				session.FTP.delete(this.getName());
			}
			else if (this.isDirectory()) {
				session.FTP.chdir(this.getAbsolute());
				List files = this.list(true, true);
				java.util.Iterator iterator = files.iterator();
				Path file = null;
				while (iterator.hasNext()) {
					file = (Path) iterator.next();
					if (file.isDirectory()) {
						file.delete();
					}
					if (file.isFile()) {
						session.log("Deleting " + this.getName(), Message.PROGRESS);
						session.FTP.delete(file.getName());
					}
				}
				session.FTP.cdup();
				session.log("Deleting " + this.getName(), Message.PROGRESS);
				session.FTP.rmdir(this.getName());
			}
		}
		catch (FTPException e) {
			session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}

	public void rename(String filename) {
		log.debug("rename:" + filename);
		try {
			session.check();
			session.FTP.chdir(this.getParent().getAbsolute());
			session.log("Renaming " + this.getName() + " to " + filename, Message.PROGRESS);
			session.FTP.rename(this.getName(), filename);
			this.setPath(this.getParent().getAbsolute(), filename);
			this.getParent().list(true);
		}
		catch (FTPException e) {
			session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}

	public Path mkdir(String name) {
		log.debug("mkdir:" + name);
		try {
			session.check();
			session.log("Make directory " + name, Message.PROGRESS);
			session.FTP.mkdir(name);
			this.list(true);
		}
		catch (FTPException e) {
			session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		return PathFactory.createPath(session, this.getAbsolute(), name);
	}

	public void changePermissions(Permission perm, boolean recursive) {
//		log.debug("changePermissions:" + permissions);
		String command = recursive ? "chmod -R" : "chmod";
		try {
			session.check();
//			session.FTP.site(command+" "+perm.getOctalCode()+" \""+this.getAbsolute()+"\""); //some server support it (proftpd), others don't (lukemftpd)
			session.FTP.site(command+" "+perm.getOctalCode()+" "+this.getAbsolute());
		}
		catch (FTPException e) {
			session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}

	public void changeOwner(String owner, boolean recursive) {
		log.debug("changeOwner");
		String command = recursive ? "chown -R" : "chown";
		try {
			session.check();
			session.FTP.site(command+" "+owner+" "+this.getAbsolute());
		}
		catch(FTPException e) {
			session.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}
	
	public void changeGroup(String group, boolean recursive) {
		log.debug("changeGroup");
		String command = recursive ? "chgrp -R" : "chgrp";
		try {
			session.check();
			session.FTP.site(command+" "+group+" "+this.getAbsolute());
		}
		catch(FTPException e) {
			session.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}
	
	public List getChilds(int kind) {
		try {
			switch (kind) {
				case Queue.KIND_DOWNLOAD:
					return this.getDownloadQueue();
				case Queue.KIND_UPLOAD:
					return this.getUploadQueue();
			}
		}
		catch (FTPException e) {
			session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		return null;
	}
	
	private List getDownloadQueue() throws IOException {
		return this.getDownloadQueue(new ArrayList());
	}
	
	private List getDownloadQueue(List queue) throws IOException {
		if (this.isDirectory()) {
			this.session.check();
			List files = this.list(false, true);
			java.util.Iterator i = files.iterator();
			while (i.hasNext()) {
				FTPPath p = (FTPPath) i.next();
				p.setLocal(new Local(this.getLocal(), p.getName()));
				p.status.setResume(this.status.isResume());
				((FTPPath)p).getDownloadQueue(queue);
			}
		}
		else if (this.isFile()) {
//			try {
//				this.status.setSize(this.session.FTP.size(this.getAbsolute()));
//			}
//			catch(FTPException e) {
//				log.error(e.getMessage());
//			}
			queue.add(this);
		}
		else
			throw new IOException("Cannot determine file type");
		return queue;
	}

	public void download() {
		try {
			log.debug("download:" + this.toString());
			if (!this.isFile())
				throw new IOException("Download must be a file.");
			this.session.check();
			this.status.setSize(this.session.FTP.size(this.getAbsolute()));
			if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
				this.session.FTP.setTransferType(FTPTransferType.BINARY);
				this.getLocal().getParentFile().mkdirs();
				OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
				if (out == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.InputStream in = this.session.FTP.getBinary(this.getAbsolute(), this.status.isResume() ? this.getLocal().length() : 0);
				if (in == null) {
					throw new IOException("Unable opening data stream");
				}
				this.download(in, out);
				if (this.status.isComplete())
					this.session.FTP.validateTransfer();
				if(status.isCanceled()) {
					this.session.FTP.abor();
				}
			}
			else if (Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
				this.session.FTP.setTransferType(FTPTransferType.ASCII);
				this.getLocal().getParentFile().mkdir();
				java.io.Writer out = new FileWriter(this.getLocal(), this.status.isResume());
				if (out == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.Reader in = this.session.FTP.getASCII(this.getName(), this.status.isResume() ? this.getLocal().length() : 0);
				if (in == null) {
					throw new IOException("Unable opening data stream");
				}
				this.download(in, out);
				if (this.status.isComplete())
					this.session.FTP.validateTransfer();
				if(status.isCanceled()) {
					this.session.FTP.abor();
				}
			}
			else {
				throw new FTPException("Transfer type not set");
			}
		}
		catch (FTPException e) {
			this.session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}

	private List getUploadQueue() throws IOException {
		return this.getUploadQueue(new ArrayList());
	}
	
	private List getUploadQueue(List queue) throws IOException {
		if (this.getLocal().isDirectory()) {
			this.session.check();
			session.FTP.mkdir(this.getAbsolute());
			File[] files = this.getLocal().listFiles();
			for (int i = 0; i < files.length; i++) {
				Path p = PathFactory.createPath(this.session, this.getAbsolute(), new Local(files[i].getAbsolutePath()));
				p.status.setResume(this.status.isResume());
				((FTPPath)p).getUploadQueue(queue);
			}
		}
		else if (this.getLocal().isFile()) {
			this.status.setSize(this.getLocal().length());
			queue.add(this);
		}
		else
			throw new IOException("Cannot determine file type");
		return queue;
	}

	public void upload() {
		try {
			log.debug("upload:" + this.toString());
			this.session.check();
			this.status.setSize(this.getLocal().length());
			if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
				this.session.FTP.setTransferType(FTPTransferType.BINARY);
				java.io.InputStream in = new FileInputStream(this.getLocal());
				if (in == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.OutputStream out = this.session.FTP.putBinary(this.getAbsolute(), false);
				if (out == null) {
					throw new IOException("Unable opening data stream");
				}
				this.upload(out, in);
				if (this.status.isComplete())
					this.session.FTP.validateTransfer();
				if(status.isCanceled()) {
					this.session.FTP.abor();
				}				
				if(Preferences.instance().getProperty("queue.upload.changePermissions").equals("true")) {
					this.changePermissions(this.getLocal().getPermission(), false);
				}
			}
			else if (Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
				this.session.FTP.setTransferType(FTPTransferType.ASCII);
				java.io.Reader in = new FileReader(this.getLocal());
				if (in == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.Writer out = this.session.FTP.putASCII(this.getAbsolute(), false);
				if (out == null) {
					throw new IOException("Unable opening data stream");
				}
				this.upload(out, in);
				if (this.status.isComplete())
					this.session.FTP.validateTransfer();
				if(status.isCanceled()) {
					this.session.FTP.abor();
				}				
				if(Preferences.instance().getProperty("queue.upload.changePermissions").equals("true")) {
					this.changePermissions(this.getLocal().getPermission(), false);
				}
			}
			else {
				throw new FTPException("Transfer mode not set");
			}
		}
		catch (FTPException e) {
			this.session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}
}
