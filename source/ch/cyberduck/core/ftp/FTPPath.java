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
import java.util.Iterator;
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
		List files = this.getSession().cache().get(this.getAbsolute());
//		List files = this.cache();
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

	public void rename(String absolute) {
		log.debug("rename:" + absolute);
		try {
			session.check();
			session.FTP.chdir(this.getParent().getAbsolute());
			session.log("Renaming " + this.getName() + " to " + absolute, Message.PROGRESS);
			session.FTP.rename(this.getName(), absolute);
			this.setPath(absolute);
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
		List childs = new ArrayList();
		try {
			switch (kind) {
				case Queue.KIND_DOWNLOAD:
					childs = this.getDownloadQueue(childs);
					break;
				case Queue.KIND_UPLOAD:
					childs = this.getUploadQueue(childs);
					break;
			}
		}
		catch (FTPException e) {
			session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		return childs;
	}
	
	private List getDownloadQueue(List queue) throws IOException {
		if (this.isDirectory()) {
			for (Iterator i = this.list(false, true).iterator() ; i.hasNext() ;) {
				FTPPath p = (FTPPath) i.next();
				p.setLocal(new Local(this.getLocal(), p.getName()));
				((FTPPath)p).getDownloadQueue(queue);
			}
		}
		else if (this.isFile()) {
			queue.add(this);
		}
		return queue;
	}

	public void download() {
		try {
			log.debug("download:"+this.toString());
			this.session.check();
			if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
				this.downloadBinary();
			}
			else if (Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
				this.downloadASCII();
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
	
	private void downloadBinary() {
		InputStream in = null;
		OutputStream out = null;
		try {
			this.session.FTP.setTransferType(FTPTransferType.BINARY);
			this.status.setSize(this.session.FTP.size(this.getAbsolute()));
			if(this.status.isResume()) {
				//				this.status.setCurrent(this.getLocal().length());
				this.status.setCurrent(this.getLocal().getTemp().length());
			}
			this.getLocal().getParentFile().mkdirs();
			//				out = new FileOutputStream(this.getLocal(), this.status.isResume());
			out = new FileOutputStream(this.getLocal().getTemp(), this.status.isResume());
			if (out == null) {
				throw new IOException("Unable to buffer data");
			}
			//				in = this.session.FTP.getBinary(this.getAbsolute(), this.status.isResume() ? this.getLocal().length() : 0);
			in = this.session.FTP.getBinary(this.getAbsolute(), this.status.isResume() ? this.getLocal().getTemp().length() : 0);
			if (in == null) {
				throw new IOException("Unable opening data stream");
			}
			this.download(in, out);
			if (this.status.isComplete()) {
				this.session.FTP.validateTransfer();
				if (Preferences.instance().getProperty("queue.download.changePermissions").equals("true")) {
					this.getLocal().setPermission(this.attributes.getPermission());
				}
			}
			if(status.isCanceled()) {
				this.session.FTP.abor();
			}
		}
		catch (FTPException e) {
			this.session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	private void downloadASCII() {
		java.io.Reader in = null;
		java.io.Writer out = null;
		try {
			this.session.FTP.setTransferType(FTPTransferType.ASCII);
			this.status.setSize(this.session.FTP.size(this.getAbsolute()));
			if(this.status.isResume()) {
				//				this.status.setCurrent(this.getLocal().length());
				this.status.setCurrent(this.getLocal().getTemp().length());
			}
			this.getLocal().getParentFile().mkdirs();
			//				out = new FileWriter(this.getLocal(), this.status.isResume());
			out = new FileWriter(this.getLocal().getTemp(), this.status.isResume());
			if (out == null) {
				throw new IOException("Unable to buffer data");
			}
			//				in = this.session.FTP.getASCII(this.getName(), this.status.isResume() ? this.getLocal().length() : 0);
			in = this.session.FTP.getASCII(this.getName(), this.status.isResume() ? this.getLocal().getTemp().length() : 0);
			if (in == null) {
				throw new IOException("Unable opening data stream");
			}
			this.download(in, out);
			if (this.status.isComplete()) {
				this.session.FTP.validateTransfer();
				if (Preferences.instance().getProperty("queue.download.changePermissions").equals("true")) {
					this.getLocal().setPermission(this.attributes.getPermission());
				}
			}
			if(status.isCanceled()) {
				this.session.FTP.abor();
			}
		}
		catch (FTPException e) {
			this.session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	private List getUploadQueue(List queue) throws IOException {
		if (this.getLocal().isDirectory()) {
			if(!this.exists())
				this.session.check();
				session.FTP.mkdir(this.getAbsolute());
			File[] files = this.getLocal().listFiles();
			for (int i = 0; i < files.length; i++) {
				Path p = PathFactory.createPath(this.session, this.getAbsolute(), new Local(files[i].getAbsolutePath()));
				((FTPPath)p).getUploadQueue(queue);
			}
		}
		else if (this.getLocal().isFile()) {
			this.status.setSize(this.getLocal().length()); //setting the file size to the known size of the local file
			queue.add(this);
		}
		return queue;
	}

	public void upload() {
		try {
			log.debug("upload:"+this.toString());
			this.session.check();
			if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
				this.uploadBinary();
			}
			else if (Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
				this.uploadASCII();
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
	
	private void uploadBinary() {
		InputStream in = null;
		OutputStream out = null;
		try {
			this.session.FTP.setTransferType(FTPTransferType.BINARY);
			this.status.setSize(this.getLocal().length());
			if(this.status.isResume()) {
				this.status.setCurrent(this.session.FTP.size(this.getAbsolute()));
			}
			in = new FileInputStream(this.getLocal());
			if (in == null) {
				throw new IOException("Unable to buffer data");
			}
			out = this.session.FTP.putBinary(this.getAbsolute(), this.status.isResume());
			if (out == null) {
				throw new IOException("Unable opening data stream");
			}
			this.upload(out, in);
			if (this.status.isComplete()) {
				this.session.FTP.validateTransfer();
			}
			if (status.isCanceled()) {
				this.session.FTP.abor();
			}				
			if (Preferences.instance().getProperty("queue.upload.changePermissions").equals("true")) {
				this.changePermissions(this.getLocal().getPermission(), false);
			}
		}
		catch (FTPException e) {
			this.session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	private void uploadASCII() {
		java.io.Reader in = null;
		java.io.Writer out = null;
		try {
			this.session.FTP.setTransferType(FTPTransferType.ASCII);
			this.status.setSize(this.getLocal().length());
			if(this.status.isResume()) {
				this.status.setCurrent(this.session.FTP.size(this.getAbsolute()));
			}
			in = new FileReader(this.getLocal());
			if (in == null) {
				throw new IOException("Unable to buffer data");
			}
			out = this.session.FTP.putASCII(this.getAbsolute(), this.status.isResume());
			if (out == null) {
				throw new IOException("Unable opening data stream");
			}
			this.upload(out, in);
			if (this.status.isComplete())
				this.session.FTP.validateTransfer();
			if (status.isCanceled()) {
				this.session.FTP.abor();
			}
			if(Preferences.instance().getProperty("queue.upload.changePermissions").equals("true")) {
				this.changePermissions(this.getLocal().getPermission(), false);
			}
		}
		catch (FTPException e) {
			this.session.log("FTP Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			}
			catch(IOException e) {
				log.error(e.getMessage());
			}
		}		
	}
}
