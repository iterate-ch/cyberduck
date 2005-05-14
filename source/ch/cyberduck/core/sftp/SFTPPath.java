package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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
import com.sshtools.j2ssh.sftp.*;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
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

	public List list(String encoding, boolean refresh, Filter filter, boolean notifyObservers) {
		synchronized(session) {
			if(notifyObservers) {
				session.addPathToHistory(this);
			}
			if(refresh || session.cache().get(this.getAbsolute()) == null) {
				List files = new ArrayList();
				session.log(Message.PROGRESS, NSBundle.localizedString("Listing directory", "")+" "+this.getAbsolute());
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
					session.cache().put(this.getAbsolute(), files);
                    session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
				}
				catch(SshException e) {
					session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
                    return files;
				}
				catch(IOException e) {
					session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
					session.close();
                    return files;
				}
			}
			if(notifyObservers) {
				session.callObservers(this);
			}
            List files = new ArrayList(session.cache().get(this.getAbsolute()));
			for(Iterator i = files.iterator(); i.hasNext(); ) {
				if(!filter.accept((Path)i.next())) {
					i.remove();
				}
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
				session.log(Message.PROGRESS, NSBundle.localizedString("Make directory", "")+" "+this.getName());
				session.SFTP.makeDirectory(this.getAbsolute());
				session.cache().put(this.getAbsolute(), new ArrayList());
				this.getParent().invalidate();
                session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
				session.close();
			}
		}
	}

	public void rename(String filename) {
		synchronized(session) {
			try {
				session.check();
				session.log(Message.PROGRESS, "Renaming "+this.getName()+" to "+filename);
				session.SFTP.renameFile(this.getAbsolute(), filename);
				this.setPath(filename);
				this.getParent().invalidate();
                session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
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
						session.log(Message.PROGRESS, NSBundle.localizedString("Getting timestamp of", "")+" "+this.getName());
						SftpFile f = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
						this.attributes.setTimestamp(Long.parseLong(f.getAttributes().getModifiedTime().toString())*1000L);
						session.log(Message.PROGRESS, NSBundle.localizedString("Getting size of", "")+" "+this.getName());
						this.attributes.setSize(f.getAttributes().getSize().doubleValue());
						f.close();
					}
					catch(SshException e) {
						session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
					}
					catch(IOException e) {
						session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
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
                    session.log(Message.PROGRESS, NSBundle.localizedString("Deleting", "")+" "+this.getName());
					session.SFTP.removeFile(this.getAbsolute());
				}
				else if(this.attributes.isDirectory()) {
					List files = this.list(true, new NullFilter(), false);
					java.util.Iterator iterator = files.iterator();
					Path file = null;
					while(iterator.hasNext()) {
						file = (Path)iterator.next();
						if(file.attributes.isFile()) {
                            session.log(Message.PROGRESS, NSBundle.localizedString("Deleting", "")+" "+this.getName());
							session.SFTP.removeFile(file.getAbsolute());
						}
						if(file.attributes.isDirectory()) {
							file.delete();
						}
					}
                    session.log(Message.PROGRESS, NSBundle.localizedString("Deleting", "")+" "+this.getName());
					session.SFTP.removeDirectory(this.getAbsolute());
				}
				this.getParent().invalidate();
                session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
				session.close();
			}
		}
	}

	public void changeOwner(String owner, boolean recursive) {
		synchronized(session) {
			log.debug("changeOwner");
			try {
				session.check();
				if(this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
					session.log(Message.PROGRESS, "Changing owner to "+owner+" on "+this.getName());
					session.SFTP.changeOwner(this.getAbsolute(), owner);
				}
				else if(this.attributes.isDirectory()) {
					session.log(Message.PROGRESS, "Changing owner to "+owner+" on "+this.getName());
					session.SFTP.changeOwner(this.getAbsolute(), owner);
					if(recursive) {
						List files = this.list(false, new NullFilter(), false);
						java.util.Iterator iterator = files.iterator();
						Path file = null;
						while(iterator.hasNext()) {
							file = (Path)iterator.next();
							file.changeOwner(owner, recursive);
						}
					}
				}
				this.getParent().invalidate();
                session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
				session.close();
			}
		}
	}
	
	public void changeGroup(String group, boolean recursive) {
		synchronized(session) {
			log.debug("changeGroup");
			try {
				session.check();
				if(this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
					session.log(Message.PROGRESS, "Changing group to "+group+" on "+this.getName());
					session.SFTP.changeGroup(this.getAbsolute(), group);
				}
				else if(this.attributes.isDirectory()) {
					session.log(Message.PROGRESS, "Changing group to "+group+" on "+this.getName());
					session.SFTP.changeGroup(this.getAbsolute(), group);
					if(recursive) {
						List files = this.list(false, new NullFilter(), false);
						java.util.Iterator iterator = files.iterator();
						Path file = null;
						while(iterator.hasNext()) {
							file = (Path)iterator.next();
							file.changeGroup(group, recursive);
						}
					}
				}
				this.getParent().invalidate();
                session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
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
					session.log(Message.PROGRESS, "Changing permission to "+perm.getOctalCode()+" on "+this.getName());
					session.SFTP.changePermissions(this.getAbsolute(), perm.getDecimalCode());
				}
				else if(this.attributes.isDirectory()) {
					session.log(Message.PROGRESS, "Changing permission to "+perm.getOctalCode()+" on "+this.getName());
					session.SFTP.changePermissions(this.getAbsolute(), perm.getDecimalCode());
					if(recursive) {
						List files = this.list(false, new NullFilter(), false);
						java.util.Iterator iterator = files.iterator();
						Path file = null;
						while(iterator.hasNext()) {
							file = (Path)iterator.next();
							file.changePermissions(perm, recursive);
						}
					}
				}
				this.getParent().invalidate();
				session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
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
				session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH Error: ("+this.getName()+") "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
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
                    if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                        FileAttributes attrs = new FileAttributes();
                        attrs.setTimes(f.getAttributes().getAccessedTime(),
                                                   new UnsignedInteger32(this.getLocal().getTimestamp().getTime()/1000));
                        session.SFTP.setAttributes(f, attrs);
                    }
				}
				if(this.attributes.isDirectory()) {
					this.mkdir();
				}
				this.getParent().invalidate();
				session.log(Message.STOP, NSBundle.localizedString("Idle", ""));
			}
			catch(SshException e) {
				session.log(Message.ERROR, "SSH Error: ("+this.getName()+") "+e.getMessage());
			}
			catch(IOException e) {
				session.log(Message.ERROR, "IO "+NSBundle.localizedString("Error", "")+": "+e.getMessage());
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
