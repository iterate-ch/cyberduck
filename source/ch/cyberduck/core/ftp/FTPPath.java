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
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import java.io.*;
import java.util.List;
import com.apple.cocoa.foundation.NSDictionary;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class FTPPath extends Path {
    private static Logger log = Logger.getLogger(FTPPath.class);
	
    private FTPSession session;
	
    /**
		* @param session The connection to work with for regular file operations
     * @param parent The parent directory relative to this file
     * @param name The filename of this path
     */
    public FTPPath(FTPSession session, String parent, String name) {
		super(parent, name);
		this.session = session;
    }
	
    public FTPPath(FTPSession session, String path) {
		super(path);
		this.session = session;
    }
	
    /**
		* @param session The connection to work with for regular file operations
     * @param parent The parent directory relative to this file
	 * @param file The corresponding local file to the remote path
     */
    public FTPPath(FTPSession session, String parent, Local file) {
		super(parent, file);
		this.session = session;
    }
	
	public FTPPath(FTPSession session, NSDictionary dict) {
		super(dict);
		this.session = session;
    }
	
    public Path copy(Session s) {
		FTPPath copy = new FTPPath((FTPSession)s, this.getAbsolute());
//		FTPPath copy = new FTPPath((FTPSession)s, this.getParent().getAbsolute(), this.getLocal());
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
			parent = new FTPPath(session, dirname);
		}
		log.debug("getParent:"+parent);
		return parent;
    }
	
    public List list() {
		return this.list(true, Preferences.instance().getProperty("browser.showHidden").equals("true"));
    }
	
    public Session getSession() {
		return this.session;
    }
    
    public List list(boolean notifyobservers, boolean showHidden) {
		session.log("Listing "+this.getAbsolute(), Message.PROGRESS);
		session.addPathToHistory(this);
		try {
			session.check();
			session.FTP.setTransferType(FTPTransferType.ASCII);
			session.FTP.chdir(this.getAbsolute());
			this.setCache(FTPParser.instance().parseList(this, session.FTP.dir(), showHidden));
			if(notifyobservers) {
				session.callObservers(this);
			}
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
		return this.cache();
    }
	
    public void delete() {
		log.debug("delete:"+this.toString());
		try {
			session.check();
			if(this.isDirectory()) {
				session.FTP.chdir(this.getAbsolute());
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
						session.FTP.delete(file.getName());
					}
				}
				session.FTP.cdup();
				session.log("Deleting "+this.getName(), Message.PROGRESS);
				session.FTP.rmdir(this.getName());
			}
			if(this.isFile()) {
				session.log("Deleting "+this.getName(), Message.PROGRESS);
				session.FTP.delete(this.getName());
			}
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
	
    public void rename(String filename) {
		log.debug("rename:"+filename);
		try {
			session.check();
			session.FTP.chdir(this.getParent().getAbsolute());
			session.log("Renaming "+this.getName()+" to "+filename, Message.PROGRESS);
			session.FTP.rename(this.getName(), filename);
			this.setPath(this.getParent().getAbsolute(), filename);
			this.getParent().list();
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
	
    public Path mkdir(String name) {
		log.debug("mkdir:"+name);
		try {
			session.check();
			session.log("Make directory "+name, Message.PROGRESS);
			session.FTP.mkdir(name);
			this.list();
		}
		catch(FTPException e) {
			session.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		return new FTPPath(session, this.getAbsolute(), name);
    }
	
    public void changePermissions(int permissions) {
		log.debug("changePermissions:"+permissions);
		try {
			session.check();
			session.FTP.site("chmod "+permissions+" "+this.getAbsolute());
//			session.FTP.site("chmod "+permissions+" \""+this.getAbsolute()+"\"");
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
	
	//  public void changeOwner(String owner) {
 //	log.debug("changeOwner");
 //	try {
 //	    session.check();
 //	    session.FTP.site("chown "+owner+" "+this.getAbsolute());
 //	}
 //	catch(FTPException e) {
 //	    session.log("FTP Error: "+e.getMessage(), Message.ERROR);
 //	}
 //	catch(IOException e) {
 //	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
 //	}
 //    }
	
	//    public void changeGroup(String group) {
 //	log.debug("changeGroup");
 //	try {
 //	    session.check();
 //	    session.FTP.site("chown :"+group+" "+this.getAbsolute());
 //	}
 //	catch(FTPException e) {
 //	    session.log("FTP Error: "+e.getMessage(), Message.ERROR);
 //	}
 //	catch(IOException e) {
 //	    session.log("IO Error: "+e.getMessage(), Message.ERROR);
 //	}
 //  }
	
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
		catch(FTPException e) {
			session.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
    }
	
    private void fillDownloadQueue(List queue)  throws IOException {
		if(this.isDirectory()) {
			List files = this.list(false, true);
			java.util.Iterator i = files.iterator();
			while(i.hasNext()) {
				FTPPath p = (FTPPath)i.next();
				p.setLocal(new Local(this.getLocal(), p.getName()));
				p.fillDownloadQueue(queue);
			}
		}
		else if(this.isFile()) {
			this.status.setSize(this.session.FTP.size(this.getAbsolute()));
			queue.add(this);
		}
		else
			throw new IOException("Cannot determine file type");
    }
	
    public void download() {
		try {
			log.debug("download:"+this.toString());
			if(!this.isFile())
				throw new IOException("Download must be a file.");
			this.session.check();
			if(Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
				this.session.FTP.setTransferType(FTPTransferType.BINARY);
				this.getLocal().getParentFile().mkdirs();
				OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
				if(out == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.InputStream in = this.session.FTP.getBinary(this.getAbsolute(), this.status.isResume() ? this.getLocal().length() : 0);
				if(in == null) {
					throw new IOException("Unable opening data stream");
				}
				this.download(in, out);
				if(this.status.isComplete())
					this.session.FTP.validateTransfer();
			}
			else if(Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
				this.session.FTP.setTransferType(FTPTransferType.ASCII);
				this.getLocal().getParentFile().mkdir();
				java.io.Writer out = new FileWriter(this.getLocal(), this.status.isResume());
				if(out == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.Reader in = this.session.FTP.getASCII(this.getName(), this.status.isResume() ? this.getLocal().length() : 0);
				if(in == null) {
					throw new IOException("Unable opening data stream");
				}
				this.download(in, out);
				if(this.status.isComplete())
					this.session.FTP.validateTransfer();
			}
			else {
				throw new FTPException("Transfer type not set");
			}
		}
		catch(FTPException e) {
			this.session.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			this.session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
    }
	
    private void fillUploadQueue(List queue) throws IOException {
		if(this.getLocal().isDirectory()) {
			session.FTP.mkdir(this.getAbsolute());
			File[] files = this.getLocal().listFiles();
			for(int i = 0; i < files.length; i++) {
				FTPPath p = new FTPPath(this.session, this.getAbsolute(), new Local(files[i].getAbsolutePath()));
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
			if(Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
				this.session.FTP.setTransferType(FTPTransferType.BINARY);
				java.io.InputStream in = new FileInputStream(this.getLocal());
				if(in == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.OutputStream out = this.session.FTP.putBinary(this.getAbsolute(), false);
				if(out == null) {
					throw new IOException("Unable opening data stream");
				}
				this.upload(out, in);
				this.session.FTP.validateTransfer();
				this.changePermissions(this.getLocal().getPermission().getOctalCode());
			}
			else if(Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
				this.session.FTP.setTransferType(FTPTransferType.ASCII);
				java.io.Reader in = new FileReader(this.getLocal());
				if(in == null) {
					throw new IOException("Unable to buffer data");
				}
				java.io.Writer out = this.session.FTP.putASCII(this.getAbsolute(), false);
				if(out == null) {
					throw new IOException("Unable opening data stream");
				}
				this.upload(out, in);
				this.session.FTP.validateTransfer();
				this.changePermissions(this.getLocal().getPermission().getOctalCode());
			}
			else {
				throw new FTPException("Transfer mode not set");
			}
		}
		catch(FTPException e) {
			this.session.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			this.session.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
		}
    }
}
