package ch.cyberduck.core.sftp;

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

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.io.UnsignedInteger32;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class SFTPPath extends Path {
	private static Logger log = Logger.getLogger(SFTPPath.class);

	static {
		PathFactory.addFactory(Session.SFTP, new Factory());
	}

	private static class Factory extends PathFactory {
		protected Path create(Session session, String parent, String name) {
			return new SFTPPath((SFTPSession) session, parent, name);
		}

		protected Path create(Session session, String path) {
			return new SFTPPath((SFTPSession) session, path);
		}
		
		protected Path create(Session session) {
			return new SFTPPath((SFTPSession) session);
		}
				
		protected Path create(Session session, String path, Local file) {
			return new SFTPPath((SFTPSession) session, path, file);
		}

		protected Path create(Session session, NSDictionary dict) {
			return new SFTPPath((SFTPSession) session, dict);
		}
	}

	private SFTPSession session;

	private SFTPPath(SFTPSession session) {
		super();
		this.session = session;
	}
	
	private SFTPPath(SFTPSession session, String parent, String name) {
		super(parent, name);
		this.session = session;
	}

	private SFTPPath(SFTPSession session, String path) {
		super(path);
		this.session = session;
	}

	private SFTPPath(SFTPSession session, String parent, Local file) {
		super(parent, file);
		this.session = session;
	}

	private SFTPPath(SFTPSession session, NSDictionary dict) {
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
			SftpFile workingDirectory = null;
			try {
				session.check();
				workingDirectory = session.SFTP.openDirectory(this.getAbsolute());
				List children = new ArrayList();
				int read = 1;
				while (read > 0) {
					read = session.SFTP.listChildren(workingDirectory, children);
				}
				java.util.Iterator i = children.iterator();
				while (i.hasNext()) {
					SftpFile x = (SftpFile) i.next();
					if (!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
						if (!(x.getFilename().charAt(0) == '.') || showHidden) {
							Path p = PathFactory.createPath(session, this.getAbsolute(), x.getFilename());
							p.attributes.setOwner(x.getAttributes().getUID().toString());
							p.attributes.setGroup(x.getAttributes().getGID().toString());
							p.status.setSize(x.getAttributes().getSize().intValue());
							p.attributes.setTimestamp(Long.parseLong(x.getAttributes().getModifiedTime().toString()) * 1000L);
//							if(x.getAttributes().isFile()) {
//								p.attributes.setType(Path.FILE_TYPE);
//							}
//							else if(x.getAttributes().isDirectory()) {
//								p.attributes.setType(Path.DIRECTORY_TYPE);
//							}
//							else if(x.getAttributes().isLink()) {
//								p.attributes.setType(Path.SYMBOLIC_LINK_TYPE);
//							}
//							else {
//								p.attributes.setType(Path.FILE_TYPE);
//							}
							//hack
							String permStr = x.getAttributes().getPermissionsString();
							if(permStr.charAt(0) == 'd')
								p.attributes.setType(Path.DIRECTORY_TYPE);
							else if(permStr.charAt(0) == 'l')
								p.attributes.setType(Path.SYMBOLIC_LINK_TYPE);
							else
								p.attributes.setType(Path.FILE_TYPE);
							p.attributes.setPermission(new Permission(permStr));
							files.add(p);
						}
					}
				}
				this.setCache(files);
			}
			catch (SshException e) {
				session.log("SSH Error: " + e.getMessage(), Message.ERROR);
			}
			catch (IOException e) {
				session.log("IO Error: " + e.getMessage(), Message.ERROR);
			}
			finally {
				session.log("Idle", Message.STOP);
				if (workingDirectory != null) {
					try {
						workingDirectory.close();
					}
					catch (SshException e) {
						session.log("SSH Error: " + e.getMessage(), Message.ERROR);
					}
					catch (IOException e) {
						session.log("IO Error: " + e.getMessage(), Message.ERROR);
					}
				}
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
				session.SFTP.removeFile(this.getAbsolute());
			}
			else if (this.isDirectory()) {
				List files = this.list(false, true);
				java.util.Iterator iterator = files.iterator();
				Path file = null;
				while (iterator.hasNext()) {
					file = (Path) iterator.next();
					if (file.isDirectory()) {
						file.delete();
					}
					if (file.isFile()) {
						session.log("Deleting " + this.getName(), Message.PROGRESS);
						session.SFTP.removeFile(file.getAbsolute());
					}
				}
				session.SFTP.openDirectory(this.getParent().getAbsolute());
				session.log("Deleting " + this.getName(), Message.PROGRESS);
				session.SFTP.removeDirectory(this.getAbsolute());
			}
		}
		catch (SshException e) {
			session.log("SSH Error: " + e.getMessage(), Message.ERROR);
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
			session.log("Renaming " + this.getName() + " to " + filename, Message.PROGRESS);
			session.SFTP.renameFile(this.getAbsolute(), this.getParent().getAbsolute() + "/" + filename);
			this.setPath(this.getParent().getAbsolute(), filename);
			this.getParent().list(true);
		}
		catch (SshException e) {
			session.log("SSH Error: " + e.getMessage(), Message.ERROR);
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
//			if(session.SFTP.getAttributes(this.getAbsolute()))
			session.SFTP.makeDirectory(this.getAbsolute() + "/" + name);
			this.list(true);
		}
		catch (SshException e) {
			session.log("SSH Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
		return PathFactory.createPath(session, this.getAbsolute(), name);
	}
	
	public void changeOwner(String uid, boolean recursive) {
		//@todo assert uid is a number
		log.debug("changeOwner:"+uid);
		try {
			session.check();
			FileAttributes attrs = session.SFTP.getAttributes(this.getAbsolute());
			attrs.setUID(new UnsignedInteger32(uid));
			session.SFTP.setAttributes(this.getAbsolute(), attrs);
		}
		catch(SshException e) {
			session.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}
	
	public void changeGroup(String gid, boolean recursive) {
		//@todo assert gid is a number
		log.debug("changeGroup:"+gid);
		try {
			session.check();
			FileAttributes attrs = session.SFTP.getAttributes(this.getAbsolute());
			attrs.setGID(new UnsignedInteger32(gid));
			session.SFTP.setAttributes(this.getAbsolute(), attrs);
		}
		catch(SshException e) {
			session.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}

	public void changePermissions(Permission perm, boolean recursive) {
		log.debug("changePermissions");
		try {
			session.check();
			session.SFTP.changePermissions(this.getAbsolute(), perm.getDecimalCode());
		}
		catch (SshException e) {
			session.log("SSH Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			session.log("IO Error: " + e.getMessage(), Message.ERROR);
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
		catch (SshException e) {
			session.log("SSH Error: " + e.getMessage(), Message.ERROR);
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
		try {
			if (isDirectory()) {
				this.session.check();
				List files = this.list(false, true);
				java.util.Iterator i = files.iterator();
				while (i.hasNext()) {
					SFTPPath p = (SFTPPath) i.next();
					p.setLocal(new Local(this.getLocal(), p.getName()));
					p.status.setResume(this.status.isResume());
					p.getDownloadQueue(queue);
				}
			}
			else if (isFile()) {
//				SftpFile p = this.session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
//				this.status.setSize(p.getAttributes().getSize().intValue());
				queue.add(this);
			}
			else
				throw new IOException("Cannot determine file type");
		}
		catch (SshException e) {
			this.session.log("SSH Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		return queue;
	}

	public void download() {
		try {
			log.debug("download:" + this.toString());
			if (!this.isFile())
				throw new IOException("Download must be a file.");
			this.session.check();
			this.getLocal().getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
			if (out == null) {
				throw new IOException("Unable to buffer data");
			}
			SftpFile p = this.session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
			this.status.setCurrent(0); // sftp resume not possible
			this.status.setSize(p.getAttributes().getSize().intValue());
			SftpFileInputStream in = new SftpFileInputStream(p);
			if (in == null) {
				throw new IOException("Unable opening data stream");
			}
			this.download(in, out);
			//p.close();
		}
		catch (SshException e) {
			this.session.log("SSH Error: " + e.getMessage(), Message.ERROR);
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
			this.session.SFTP.makeDirectory(this.getAbsolute());
			File[] files = this.getLocal().listFiles();
			for (int i = 0; i < files.length; i++) {
				Path p = PathFactory.createPath(this.session, this.getAbsolute(), new Local(files[i].getAbsolutePath()));
				p.status.setResume(this.status.isResume());
				((SFTPPath)p).getUploadQueue(queue);
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
			java.io.InputStream in = new FileInputStream(this.getLocal());
			if (in == null) {
				throw new IOException("Unable to buffer data");
			}
			SftpFile p = this.session.SFTP.openFile(this.getAbsolute(), 
													SftpSubsystemClient.OPEN_CREATE | 
													SftpSubsystemClient.OPEN_WRITE |
													SftpSubsystemClient.OPEN_TRUNCATE);
			this.changePermissions(this.getLocal().getPermission(), false);
			SftpFileOutputStream out = new SftpFileOutputStream(p);
			if (out == null) {
				throw new IOException("Unable opening data stream");
			}
			this.upload(out, in);
			//p.close();
		}
		catch (SshException e) {
			this.session.log("SSH Error: " + e.getMessage(), Message.ERROR);
		}
		catch (IOException e) {
			this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
	}
}
