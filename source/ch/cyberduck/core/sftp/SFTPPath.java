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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Session;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
* @version $Id$
 */
public class SFTPPath extends Path {
    private static Logger log = Logger.getLogger(SFTPPath.class);

    private SFTPSession session;
    private SFTPSession downloadSession;
    private SFTPSession uploadSession;
	
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
	if((null == parent)) {
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
    
    public Session getSession() {
	return this.session;
    }

    public Session getDownloadSession() {
	return this.downloadSession;
    }

    public Session getUploadSession() {
	return this.uploadSession;
    }
    
    public List list() {
	return this.list(true);
    }

    public List list(boolean notifyobservers) {
	boolean showHidden = Preferences.instance().getProperty("browser.showHidden").equals("true");
	SftpFile workingDirectory = null;
	session.log("Listing "+this.getName(), Message.PROGRESS);
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
//		log.debug(x.getAbsolutePath());

		if(!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
		    SFTPPath p = new SFTPPath(session, SFTPPath.this.getAbsolute(), x.getFilename());
		    if(p.getName().charAt(0) == '.' && !showHidden) {
			p.attributes.setVisible(false);
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

//    public int size() {
//	try {
//	    session.check();
//	    SftpFile p = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
//	    return p.getAttributes().getSize().intValue();
//	}
//	catch(SshException e) {
//	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
//	}
//	catch(IOException e) {
//	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
//	}
//	return -1;
//  }

    public void changePermissions(int permissions) {
	log.debug("changePermissions");
	try {
	    session.check();
	    session.SFTP.changePermissions(this.getAbsolute(), this.attributes.getPermission().getString());
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


    public void fillDownloadQueue(Queue queue, Session downloadSession) {
	this.session = (SFTPSession)downloadSession;
	try {
	    session.check();
	    if(isDirectory()) {
		List files = this.list(false);
		java.util.Iterator i = files.iterator();
		while(i.hasNext()) {
		    SFTPPath p = (SFTPPath)i.next();
		    p.setLocal(new File(this.getLocal(), p.getName()));
		    p.fillDownloadQueue(queue, session);
		}
	    }
	    else if(isFile()) {
		SftpFile p = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
		this.status.setSize(p.getAttributes().getSize().intValue());
		queue.add(this);
	    }
	    else
		throw new IOException("Cannot determine file type");
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }
    
    public void download() {
	try {
	    log.debug("download:"+this.toString());
	    if(!this.isFile())
		throw new IOException("Download must be a file.");
//	    status.fireActiveEvent();
	    session.check();
	    this.getLocal().getParentFile().mkdir();
	    OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
	    if(out == null) {
		throw new IOException("Unable to buffer data");
	    }
	    SftpFile p = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
	    this.status.setCurrent(0);//@todo implmenet resume for sftp
//	    this.status.setSize(p.getAttributes().getSize().intValue());
	    session.log("Opening data stream...", Message.PROGRESS);
	    SftpFileInputStream in = new SftpFileInputStream(p);
	    if(in == null) {
		throw new IOException("Unable opening data stream");
	    }
	    //session.log("Downloading "+this.getName(), Message.PROGRESS);
	    this.download(in, out);
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
	finally {
//	    session.close();
	}
    }

    public void fillUploadQueue(Queue queue, Session uploadSession) {
	this.session = (SFTPSession)uploadSession;
	try {
	    session.check();
	    if(this.getLocal().isDirectory()) {
		session.SFTP.makeDirectory(this.getAbsolute());//@todo do it here rather than in upload() ?
		File[] files = this.getLocal().listFiles();
		for(int i = 0; i < files.length; i++) {
		    SFTPPath p = new SFTPPath(session, this.getAbsolute(), files[i]);
		    p.fillUploadQueue(queue, session);
		}
	    }
	    else if(this.getLocal().isFile()) {
		this.status.setSize((int)this.getLocal().length());
		queue.add(this);
	    }
	    else
		throw new IOException("Cannot determine file type");
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
    }
    
    public void upload() {
	try {
	    log.debug("upload:"+this.toString());
//	    status.fireActiveEvent();
	    session.check();
//	    this.status.setSize((int)this.getLocal().length());
	    java.io.InputStream in = new FileInputStream(this.getLocal());
	    if(in == null) {
		throw new IOException("Unable to buffer data");
	    }
	    session.log("Opening data stream...", Message.PROGRESS);
	    SftpFile remoteFile = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_CREATE | SftpSubsystemClient.OPEN_WRITE);
	    FileAttributes attrs = remoteFile.getAttributes();
	    //@ todo default permissions
	    attrs.setPermissions("rw-r--r--");
	    session.SFTP.setAttributes(remoteFile, attrs);
	    SftpFileOutputStream out = new SftpFileOutputStream(remoteFile);
	    if(out == null) {
		throw new IOException("Unable opening data stream");
	    }
	    //session.log("Uploading "+this.getName(), Message.PROGRESS);
	    this.upload(out, in);
	}
	catch(SshException e) {
	    session.log("SSH Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
	finally {
//	    session.close();
	}
    }
}
