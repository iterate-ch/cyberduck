package ch.cyberduck.core.sftp;

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

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;

import com.apple.cocoa.foundation.NSDictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

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
			return new SFTPPath((SFTPSession)session, parent, name);
		}

		protected Path create(Session session, String path) {
			return new SFTPPath((SFTPSession)session, path);
		}

		protected Path create(Session session) {
			return new SFTPPath((SFTPSession)session);
		}

		protected Path create(Session session, String path, Local file) {
			return new SFTPPath((SFTPSession)session, path, file);
		}

		protected Path create(Session session, NSDictionary dict) {
			return new SFTPPath((SFTPSession)session, dict);
		}
	}

	private SFTPSession session;

	private SFTPPath(SFTPSession s) {
		super();
		this.session = s;
	}

	private SFTPPath(SFTPSession s, String parent, String name) {
		super(parent, name);
		this.session = s;
	}

	private SFTPPath(SFTPSession s, String path) {
		super(path);
		this.session = s;
	}

	private SFTPPath(SFTPSession s, String parent, Local file) {
		super(parent, file);
		this.session = s;
	}

	private SFTPPath(SFTPSession s, NSDictionary dict) {
		super(dict);
		this.session = s;
	}

	public Session getSession() {
		return this.session;
	}

	public List list(String encoding, boolean refresh, boolean showHidden, boolean notifyObservers) {
		synchronized(session) {
			List files = session.cache().get(this.getAbsolute());
			if(notifyObservers) {
				session.addPathToHistory(this);
			}
			if(refresh || null == files) {
				files = new ArrayList();
				session.log("Listing "+this.getAbsolute(), Message.PROGRESS);
				try {
					session.check();
					SftpFile workingDirectory = session.SFTP.openDirectory(this.getAbsolute());
					List children = new ArrayList();
					int read = 1;
					while(read > 0) {
						read = session.SFTP.listChildren(workingDirectory, children);
					}
					workingDirectory.close();
					java.util.Iterator i = children.iterator();
					while(i.hasNext()) {
						SftpFile x = (SftpFile)i.next();
						if(!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
							if(!(x.getFilename().charAt(0) == '.') || showHidden) {
								Path p = PathFactory.createPath(session, this.getAbsolute(), x.getFilename());
								p.attributes.setOwner(x.getAttributes().getUID().toString());
								p.attributes.setGroup(x.getAttributes().getGID().toString());
								p.attributes.setSize(x.getAttributes().getSize().doubleValue());
								p.attributes.setTimestamp(Long.parseLong(x.getAttributes().getModifiedTime().toString())*1000L);
								String permStr = x.getAttributes().getPermissionsString();
								if(permStr.charAt(0) == 'd') {
									p.attributes.setType(Path.DIRECTORY_TYPE);
								}
								else if(permStr.charAt(0) == 'l') {
									try {
										p.cwdir();
										p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
									}
									catch(java.io.IOException e) {
										p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
									}
								}
								else {
									p.attributes.setType(Path.FILE_TYPE);
								}
								p.attributes.setPermission(new Permission(permStr.substring(1, permStr.length())));
								files.add(p);
							}
						}
					}
					session.cache().put(this.getAbsolute(), files);
					session.log("Idle", Message.STOP);
				}
				catch(SshException e) {
					session.log("SSH Error: "+e.getMessage(), Message.ERROR);
					return null;
				}
				catch(IOException e) {
					session.log("IO Error: "+e.getMessage(), Message.ERROR);
					session.close();
					return null;
				}
			}
			if(notifyObservers) {
				session.callObservers(this);
			}
			return files;
		}
	}

	public void cwdir() throws IOException {
		synchronized(session) {
			session.check();
			session.SFTP.openDirectory(this.getAbsolute());
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
				session.SFTP.makeDirectory(this.getAbsolute());
				session.cache().put(this.getAbsolute(), new ArrayList());
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(SshException e) {
				session.log("SSH Error: "+e.getMessage(), Message.ERROR);
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
				session.SFTP.renameFile(this.getAbsolute(), filename);
				this.setPath(filename);
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(SshException e) {
				session.log("SSH Error: "+e.getMessage(), Message.ERROR);
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
						SftpFile f = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
						this.attributes.setTimestamp(Long.parseLong(f.getAttributes().getModifiedTime().toString())*1000L);
						session.log("Getting size of "+this.getName(), Message.PROGRESS);
						this.attributes.setSize(f.getAttributes().getSize().doubleValue());
						f.close();
					}
					catch(SshException e) {
						session.log("SSH Error: "+e.getMessage(), Message.ERROR);
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
				if(this.attributes.isFile()) {
					session.check();
					session.log("Deleting "+this.getName(), Message.PROGRESS);
					session.SFTP.removeFile(this.getAbsolute());
				}
				else if(this.attributes.isDirectory()) {
					List files = this.list(true, true, false);
					java.util.Iterator iterator = files.iterator();
					Path file = null;
					while(iterator.hasNext()) {
						file = (Path)iterator.next();
						if(file.attributes.isFile()) {
							session.log("Deleting "+this.getName(), Message.PROGRESS);
							session.SFTP.removeFile(file.getAbsolute());
						}
						if(file.attributes.isDirectory()) {
							file.delete();
						}
					}
					session.log("Deleting "+this.getName(), Message.PROGRESS);
					session.SFTP.removeDirectory(this.getAbsolute());
				}
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(SshException e) {
				session.log("SSH Error: "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
			}
		}
	}

	public void changePermissions(Permission perm, boolean recursive) {
		synchronized(session) {
			log.debug("changePermissions");
			try {
				session.check();
				if(this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
					session.log("Changing permission to "+perm.getOctalCode()+" on "+this.getName(), Message.PROGRESS);
					session.SFTP.changePermissions(this.getAbsolute(), perm.getDecimalCode());
				}
				else if(this.attributes.isDirectory()) {
					session.log("Changing permission to "+perm.getOctalCode()+" on "+this.getName(), Message.PROGRESS);
					session.SFTP.changePermissions(this.getAbsolute(), perm.getDecimalCode());
					if(recursive) {
						List files = this.list(false, true, false);
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
			catch(SshException e) {
				session.log("SSH Error: "+e.getMessage(), Message.ERROR);
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
			InputStream in = null;
			OutputStream out = null;
			try {
				if(this.attributes.isFile()) {
					session.check();
					out = new FileOutputStream(this.getLocal(), this.status.isResume());
					if(out == null) {
						throw new IOException("Unable to buffer data");
					}
					SftpFile f = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
					in = new SftpFileInputStream(f);
					if(in == null) {
						throw new IOException("Unable opening data stream");
					}
					if(this.status.isResume()) {
						this.status.setCurrent(this.getLocal().getSize());
						long skipped = in.skip(this.status.getCurrent());
						log.info("Skipping "+skipped+" bytes");
						if(skipped < this.status.getCurrent()) {
							throw new IOException("Resume failed: Skipped "+skipped+" bytes instead of "+this.status.getCurrent());
						}
					}
					this.download(in, out);
					if(this.status.isComplete()) {
						if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
							log.info("Updating permissions");
							Permission perm = null;
							if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")) {
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
					if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
						if(!this.attributes.isUndefined()) {
							this.getLocal().setLastModified(this.attributes.getTimestamp().getTime());
						}
					}
				}
				if(this.attributes.isDirectory()) {
					this.getLocal().mkdirs();
				}
				session.log("Idle", Message.STOP);
			}
			catch(SshException e) {
				session.log("SSH Error: ("+this.getName()+") "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
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
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		}
	}

	public void upload() {
		synchronized(session) {
			log.debug("upload:"+this.toString());
			InputStream in = null;
			SftpFileOutputStream out = null;
			try {
				if(this.attributes.isFile()) {
					session.check();
					in = new FileInputStream(this.getLocal());
					if(in == null) {
						throw new IOException("Unable to buffer data");
					}
					SftpFile f = null;
					if(this.status.isResume()) {
						f = session.SFTP.openFile(this.getAbsolute(),
						    SftpSubsystemClient.OPEN_WRITE | //File open flag, opens the file for writing.
						    SftpSubsystemClient.OPEN_APPEND); //File open flag, forces all writes to append data at the end of the file.
					}
					else {
						f = session.SFTP.openFile(this.getAbsolute(),
						    SftpSubsystemClient.OPEN_CREATE | //File open flag, if specified a new file will be created if one does not already exist.
						    SftpSubsystemClient.OPEN_WRITE | //File open flag, opens the file for writing.
						    SftpSubsystemClient.OPEN_TRUNCATE); //File open flag, forces an existing file with the same name to be truncated to zero length when creating a file by specifying OPEN_CREATE.
					}
					if(this.status.isResume()) {
						this.status.setCurrent(f.getAttributes().getSize().intValue());
					}
					out = new SftpFileOutputStream(f);
					if(out == null) {
						throw new IOException("Unable opening data stream");
					}
					if(this.status.isResume()) {
						long skipped = out.skip(this.status.getCurrent());
						log.info("Skipping "+skipped+" bytes");
						if(skipped < this.status.getCurrent()) {
							throw new IOException("Resume failed: Skipped "+skipped+" bytes instead of "+this.status.getCurrent());
						}
					}
					this.upload(out, in);
					if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
						Permission perm = null;
						if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
							perm = new Permission(Preferences.instance().getProperty("queue.upload.permissions.default"));
						}
						else {
							perm = this.getLocal().getPermission();
						}
						if(!perm.isUndefined()) {
							session.SFTP.changePermissions(this.getAbsolute(), perm.getDecimalCode());
						}
					}
					if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
						f.getAttributes().setTimes(f.getAttributes().getAccessedTime(),
						    new UnsignedInteger32(this.getLocal().getTimestamp().getTime()/1000));
						session.SFTP.setAttributes(f, f.getAttributes());
					}
				}
				if(this.attributes.isDirectory()) {
					this.mkdir();
				}
				this.getParent().invalidate();
				session.log("Idle", Message.STOP);
			}
			catch(SshException e) {
				session.log("SSH Error: ("+this.getName()+") "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
				session.log("IO Error: "+e.getMessage(), Message.ERROR);
				session.close();
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
}
