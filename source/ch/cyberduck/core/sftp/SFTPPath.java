package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
* @version $Id$
 */
public class SFTPPath extends Path {
    private static Logger log = Logger.getLogger(SFTPPath.class);
//    private SftpFile file;
    private SFTPSession session;
	
    public SFTPPath(SFTPSession session, String parent, String name) {
	super(parent, name);
	this.session = session;
    }

    public SFTPPath(SFTPSession session, String path) {
	super(path);
	this.session = session;
    }

    public SFTPPath(SFTPSession session, String parent, java.io.File file) {
	super(parent, file);
	this.session = session;
    }
    
    public Path getParent() {
	String abs = this.getAbsolute();
	if((null == parent)) {// && !abs.equals("/")) {
	    int index = abs.lastIndexOf('/');
	    String dirname = abs;
	    if(index > 0)
		dirname = abs.substring(0, index);
	    if(index == 0) //parent is root
		dirname = "/";
	    parent = new SFTPPath(session, dirname);
	}
	log.debug("getParent:"+parent);
	return parent;
	}

    public synchronized List list() {
	return this.list(null == this.cache());
    }

    public synchronized List list(boolean refresh) {
	log.debug("list");
	if(refresh) {
	    List files = null;
	    SftpFile workingDirectory = null;
	    boolean showHidden = Preferences.instance().getProperty("listing.showHidden").equals("true");
			//@todo throw exception if we are not a directory
	    try {
		session.check();
		session.log("Listing "+this.getName(), Message.PROGRESS);
		workingDirectory = session.SFTP.openDirectory(this.getAbsolute());
		List children = new ArrayList();
		int read = 1;
		while(read > 0) {
		    read = session.SFTP.listChildren(workingDirectory, children);
		}
		java.util.Iterator i = children.iterator();
		files = new java.util.ArrayList();
		while(i.hasNext()) {
		    SftpFile x = (SftpFile)i.next();
		    if(!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
			SFTPPath p = new SFTPPath(session, SFTPPath.this.getAbsolute(), x.getFilename());
				    //log.debug(p.getName());
			if(p.getName().charAt(0) == '.' && !showHidden) {
					//p.attributes.setVisible(false);
			}
			else {
			    p.attributes.setOwner(x.getAttributes().getUID().toString());
			    p.attributes.setGroup(x.getAttributes().getGID().toString());
			    p.status.setSize(x.getAttributes().getSize().intValue());
			    p.attributes.setModified(x.getAttributes().getModifiedTime().longValue());
			    p.attributes.setMode(x.getAttributes().getPermissionsString());
			    p.attributes.setPermission(new Permission(x.getAttributes().getPermissionsString()));
			    files.add(p);
			}
		    }
		}
		this.setCache(files);
		session.callObservers(this);
		session.log("Listing complete", Message.PROGRESS);
	    }
	    catch(SshException e) {
		session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		session.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	    /*
	     finally {
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
	     */
	}
	else {
	    session.callObservers(SFTPPath.this);
	}
	return this.cache();
    }

    public synchronized void delete() {
	log.debug("delete");
	try {
	    session.check();
	    if(this.isDirectory()) {
		List files = this.list();
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
	    this.getParent().list(true);
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }

    public synchronized void rename(String filename) {
	log.debug("rename");
	try {
	    session.check();
	    session.log("Renaming "+this.getName()+" to "+filename, Message.PROGRESS);
	    session.SFTP.renameFile(this.getAbsolute(), this.getParent().getAbsolute()+"/"+filename);
	    this.getParent().list(true);
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }

    public synchronized Path mkdir(String name) {
	log.debug("mkdir");
	try {
	    session.check();
	    session.log("Make directory "+name, Message.PROGRESS);
//		session.SFTP.makeDirectory(this.getAbsolute());
	    session.SFTP.makeDirectory(name);
	    this.list(true);
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
	return new SFTPPath(session, this.getAbsolute(), name);
    }

    public synchronized void changePermissions(int permissions) {
	log.debug("changePermissions");
	try {
	    session.check();
//		session.SFTP.changePermissions(this.getAbsolute(), this.attributes.getPermission().getCode());
	    session.SFTP.changePermissions(this.getAbsolute(), this.attributes.getPermission().getString());
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }

    public void changeOwner(String owner) {
	session.log("Invalid Operation", Message.ERROR);
    }

    public synchronized void download() {
	log.debug("download");
	new Thread() {
	    SFTPSession downloadSession = null;
	    public void run() {
		status.fireActiveEvent();
		try {
		    downloadSession = (SFTPSession)session.copy();
		    downloadSession.connect();
		    if(isDirectory())
			this.downloadFolder(SFTPPath.this);
		    if(isFile())
			this.downloadFile(SFTPPath.this);
		}
		catch(SshException e) {
		    downloadSession.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
		    downloadSession.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
	    }

	    private void downloadFile(Path file) throws IOException {
		OutputStream out = new FileOutputStream(file.getLocal(), file.status.isResume());
		if(out == null) {
		    throw new IOException("Unable to buffer data");
		}
		SftpFile p = downloadSession.SFTP.openFile(file.getAbsolute(), SftpSubsystemClient.OPEN_READ);
		file.status.setSize(p.getAttributes().getSize().intValue());
		downloadSession.log("Opening data stream...", Message.PROGRESS);
		SftpFileInputStream in = new SftpFileInputStream(p);
		if(in == null) {
		    throw new IOException("Unable opening data stream");
		}
		downloadSession.log("Downloading "+file.getName()+"...", Message.PROGRESS);
		file.download(in, out);
	    }

	    private void downloadFolder(Path file) throws IOException {
		log.error("not implemented");
		downloadSession.log("Not implemented.", Message.ERROR);
		/*
		 java.util.List files = file.list(); //@todo
		 File dir = file.getLocal();
		 dir.mkdir();
		 java.util.Iterator i = files.iterator();
		 while(i.hasNext()) {
		     Path p = (Path)i.next();
		     if(p.isDirectory()) {
			 log.debug("changing directory: "+p.toString());
			 SFTP.openDirectory(p.getAbsolute());
		     }
		     log.debug("getting file:"+p.toString());
		     this.download(p);
		 }
		 SFTP.openDirectory("..");
		 */
	    }	    
	}.start();
    }

    public void upload() {
	new Thread() {
	    SFTPSession uploadSession = null;
	    public void run() {
		status.fireActiveEvent();
		try {
		    uploadSession = (SFTPSession)session.copy();
		    uploadSession.connect();
		    if(SFTPPath.this.getLocal().isDirectory())
			this.uploadFolder(SFTPPath.this);
		    if(SFTPPath.this.getLocal().isFile())
			this.uploadFile(SFTPPath.this);
		}
		catch(SshException e) {
		    uploadSession.log("SSH Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
		    uploadSession.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
	    }

	    private void uploadFile(Path file) throws IOException {
		java.io.InputStream in = new FileInputStream(file.getLocal());
		if(in == null) {
		    throw new IOException("Unable to buffer data");
		}
		uploadSession.log("Opening data stream...", Message.PROGRESS);
		SftpFile remoteFile = uploadSession.SFTP.openFile(file.getName(), SftpSubsystemClient.OPEN_CREATE | SftpSubsystemClient.OPEN_WRITE);
//	FileAttributes attrs = remoteFile.getAttributes();
//	attrs.setPermissions("rwxr-xr-x");
//	SFTP.setAttributes(remoteFile, attrs);
		SftpFileOutputStream out = new SftpFileOutputStream(remoteFile);
		if(out == null) {
		    throw new IOException("Unable opening data stream");
		}
		uploadSession.log("Uploading "+file.getName()+"...", Message.PROGRESS);
		file.upload(out, in);
	    }

	    private void uploadFolder(Path file) throws IOException {
		log.debug("not implemented");
		uploadSession.log("Not implemented.", Message.ERROR);
	    }	    
	}.start();
    }

    public Session getSession() {
	return this.session;
    }
}
