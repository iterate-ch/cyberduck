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
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class SFTPPath extends Path {
    private static Logger log = Logger.getLogger(SFTPPath.class);
	
    private SFTPSession session;
	
    public SFTPPath(SFTPSession session, String parent, String name) {
		super(parent, name);
		this.session = session;
    }
	
    public SFTPPath(SFTPSession session, String path) {
		super(path);
		this.session = session;
    }
	
    public SFTPPath(SFTPSession session, String parent, Local file) {
		super(parent, file);
		this.session = session;
    }
	
    public Path copy(Session s) {
		SFTPPath copy = new SFTPPath((SFTPSession)s, this.getAbsolute());
//		SFTPPath copy = new SFTPPath((SFTPSession)s, this.getParent().getAbsolute(), this.getLocal());
		copy.attributes = this.attributes;
		//	copy.status = this.status;
		return copy;
    }
    
    public Path getParent() {
		String abs = this.getAbsolute();
		if((null == parent)) {
			int index = abs.lastIndexOf('/');
			String dirname = abs;
			if(index > 0)
				dirname = abs.substring(0, index);
			else if(index == 0) //parent is root
				dirname = "/";
			else if(index < 0)
				dirname = session.workdir().getAbsolute();
			parent = new SFTPPath(session, dirname);
		}
		log.debug("getParent:"+parent);
		return parent;
    }
	
    public Session getSession() {
		return this.session;
    }
    
    public List list() {
		return this.list(true, Preferences.instance().getProperty("browser.showHidden").equals("true"));
    }
	
    public List list(boolean notifyobservers, boolean showHidden) {
		SftpFile workingDirectory = null;
		session.log("Listing "+this.getAbsolute(), Message.PROGRESS);
		session.addPathToHistory(this);
		try {
			session.check();
			workingDirectory = session.SFTP.openDirectory(this.getAbsolute());
			List children = new ArrayList();
			int read = 1;
			while(read > 0) {
				read = session.SFTP.listChildren(workingDirectory, children);
			}
			java.util.Iterator i = children.iterator();
			List files = new java.util.ArrayList();
			while(i.hasNext()) {
				SftpFile x = (SftpFile)i.next();
				if(!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
					SFTPPath p = new SFTPPath(session, this.getAbsolute(), x.getFilename());
					if(p.getName().charAt(0) == '.' && !showHidden) {
						p.attributes.setVisible(false);
					}
					else {
						p.attributes.setOwner(x.getAttributes().getUID().toString());
						p.attributes.setGroup(x.getAttributes().getGID().toString());
						p.status.setSize(x.getAttributes().getSize().longValue());
						p.attributes.setModified(Long.parseLong(x.getAttributes().getModifiedTime().toString())*1000L);
						p.attributes.setMode(x.getAttributes().getPermissionsString());
						p.attributes.setPermission(new Permission(x.getAttributes().getPermissionsString()));
						files.add(p);
					}
				}
			}
			this.setCache(files);
			if(notifyobservers) {
				session.callObservers(this);
			}
		}
		catch(SshException e) {
			session.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
			if(workingDirectory != null) {
				try {
					workingDirectory.close();
				}
				catch(SshException e) {
					session.log("SSH Error: "+e.getMessage(), Message.ERROR);
				}
				catch(IOException e) {
					session.log("IO Error: "+e.getMessage(), Message.ERROR);
				}
			}
		}
		return this.cache();
    }
	
    public void delete() {
		log.debug("delete:"+this.toString());
		try {
			session.check();
			if(this.isDirectory()) {
				List files = this.list(false, true);
				java.util.Iterator iterator = files.iterator();
				Path file = null;
				while(iterator.hasNext()) {
					file = (Path)iterator.next();
					if(file.isDirectory()) {
						file.delete();
					}
					if(file.isFile()) {
						session.log("Deleting "+this.getName(), Message.PROGRESS);
						session.SFTP.removeFile(file.getAbsolute());
					}
				}
				session.SFTP.openDirectory(this.getParent().getAbsolute());
				session.log("Deleting "+this.getName(), Message.PROGRESS);
				session.SFTP.removeDirectory(this.getAbsolute());
			}
			if(this.isFile()) {
				session.log("Deleting "+this.getName(), Message.PROGRESS);
				session.SFTP.removeFile(this.getAbsolute());
			}
			//	    this.getParent().list();
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
	
    public void rename(String filename) {
		log.debug("rename:"+filename);
		try {
			session.check();
			session.log("Renaming "+this.getName()+" to "+filename, Message.PROGRESS);
			session.SFTP.renameFile(this.getAbsolute(), this.getParent().getAbsolute()+"/"+filename);
			this.setPath(this.getParent().getAbsolute(), filename);
			this.getParent().list();
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
	
    public Path mkdir(String name) {
		log.debug("mkdir:"+name);
		try {
			session.check();
			session.log("Make directory "+name, Message.PROGRESS);
			session.SFTP.makeDirectory(this.getAbsolute()+"/"+name);
			this.list();
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
		return new SFTPPath(session, this.getAbsolute(), name);
    }
	
	//	public void changeOwner(int uid) {
 //		log.debug("changeOwner:"+uid);
 //		try {
 //			session.check();
 //			FileAttributes attrs = sftp.getAttributes(this.getAbsolute());
 //			attrs.setUID(new UnsignedInteger32(uid));
 //			session.SFTP.setAttributes(actual, attrs);
 //		}
 //		catch(SshException e) {
 //			session.log("SSH Error: "+e.getMessage(), Message.ERROR);
 //		}
 //		catch(IOException e) {
 //			session.log("IO Error: "+e.getMessage(), Message.ERROR);
 //		}
 //		finally {
 //			session.log("Idle", Message.STOP);
 //		}
 //	}

	//	public void changeGroup(int gid) {
//		log.debug("changeGroup:"+gid);
//		try {
//			session.check();
//			FileAttributes attrs = sftp.getAttributes(this.getAbsolute());
//			attrs.setGID(new UnsignedInteger32(gid));
//			session.SFTP.setAttributes(actual, attrs);
//		}
//		catch(SshException e) {
//			session.log("SSH Error: "+e.getMessage(), Message.ERROR);
//		}
//		catch(IOException e) {
//			session.log("IO Error: "+e.getMessage(), Message.ERROR);
//		}
//		finally {
//			session.log("Idle", Message.STOP);
//		}
//	}
	
    public void changePermissions(int permissions) {
		log.debug("changePermissions");
		try {
			session.check();
			session.SFTP.changePermissions(this.getAbsolute(), permissions);
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
	
    public void fillQueue(List queue, int kind) {
		log.debug("fillQueue:"+kind+","+kind);
		try {
			this.session.check();
			switch(kind) {
				case Queue.KIND_DOWNLOAD:
					this.fillDownloadQueue(queue);
					break;
				case Queue.KIND_UPLOAD:
					this.fillUploadQueue(queue);
					break;
			}
		}
		catch(SshException e) {
			session.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
    }
    
    private void fillDownloadQueue(List queue) {
		try {
			this.session.check();
			if(isDirectory()) {
				List files = this.list(false, true);
				java.util.Iterator i = files.iterator();
				while(i.hasNext()) {
					SFTPPath p = (SFTPPath)i.next();
					p.setLocal(new Local(this.getLocal(), p.getName()));
					p.fillDownloadQueue(queue);
				}
			}
			else if(isFile()) {
				SftpFile p = this.session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
				this.status.setSize(p.getAttributes().getSize().longValue());
				queue.add(this);
			}
			else
				throw new IOException("Cannot determine file type");
		}
		catch(SshException e) {
			this.session.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			this.session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
    }
    
    public void download() {
		try {
			log.debug("download:"+this.toString());
			if(!this.isFile())
				throw new IOException("Download must be a file.");
			this.session.check();
			this.getLocal().getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
			if(out == null) {
				throw new IOException("Unable to buffer data");
			}
			SftpFile p = this.session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
			this.status.setCurrent(0); // sftp resume not possible
			SftpFileInputStream in = new SftpFileInputStream(p);
			if(in == null) {
				throw new IOException("Unable opening data stream");
			}
			this.download(in, out);
		}
		catch(SshException e) {
			this.session.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			this.session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
    }
	
    public void fillUploadQueue(List queue) throws IOException {
		if(this.getLocal().isDirectory()) {
			this.session.SFTP.makeDirectory(this.getAbsolute());//@todo do it here rather than in upload() ?
			File[] files = this.getLocal().listFiles();
			for(int i = 0; i < files.length; i++) {
				SFTPPath p = new SFTPPath(this.session, this.getAbsolute(), new Local(files[i].getAbsolutePath()));
				p.fillUploadQueue(queue);
			}
		}
		else if(this.getLocal().isFile()) {
			this.status.setSize(this.getLocal().length());
			queue.add(this);
		}
		else
			throw new IOException("Cannot determine file type");
    }
    
    public void upload() {
		try {
			log.debug("upload:"+this.toString());
			this.session.check();
			java.io.InputStream in = new FileInputStream(this.getLocal());
			if(in == null) {
				throw new IOException("Unable to buffer data");
			}
			SftpFile remoteFile = this.session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_CREATE | SftpSubsystemClient.OPEN_WRITE);
			SftpFileOutputStream out = new SftpFileOutputStream(remoteFile);
			if(out == null) {
				throw new IOException("Unable opening data stream");
			}
			this.upload(out, in);
			this.changePermissions(this.getLocal().getPermission().getDecimalCode());
		}
		catch(SshException e) {
			this.session.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			this.session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
    }
}
