package ch.cyberduck.core;

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
import java.util.List;

import ch.cyberduck.ui.cocoa.growl.Growl;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;

/**
 * A path is a remote directory or file.
 *
 * @version $Id$
 */
public abstract class Path {
	private static Logger log = Logger.getLogger(Path.class);

	private String path = null;
	private Local local = null;

	public Status status = new Status();
	public Attributes attributes = new Attributes();

	public static final int FILE_TYPE = 1;
	public static final int DIRECTORY_TYPE = 2;
	public static final int SYMBOLIC_LINK_TYPE = 4;

	public static final String HOME = "~";

	/**
	 * Deep copies the current path with its attributes but without the status information
	 *
	 * @param s The session this path will use to fullfill its tasks
	 * @return A copy of me with a new session
	 */
	public Path copy(Session s) {
		Path copy = PathFactory.createPath(s, this.getAbsolute());
		copy.local = this.local;
		copy.attributes = this.attributes;
		copy.status = this.status;
		return copy;
	}

	public Path(NSDictionary dict) {
		Object pathObj = dict.objectForKey("Remote");
		if(pathObj != null) {
			this.setPath((String)pathObj);
		}
		Object localObj = dict.objectForKey("Local");
		if(localObj != null) {
			this.setLocal(new Local((String)localObj));
		}
		Object attributesObj = dict.objectForKey("Attributes");
		if(attributesObj != null) {
			this.attributes = new Attributes((NSDictionary)attributesObj);
		}
	}

	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.getAbsolute(), "Remote");
		dict.setObjectForKey(this.getLocal().toString(), "Local");
		dict.setObjectForKey(this.attributes.getAsDictionary(), "Attributes");
		return dict;
	}

	protected Path() {
		super();
	}

	/**
	 * A remote path where nothing is known about a local equivalent.
	 *
	 * @param parent the absolute directory
	 * @param name the file relative to param path
	 */
	protected Path(String parent, String name) {
		this.setPath(parent, name);
	}

	/**
	 * A remote path where nothing is known about a local equivalent.
	 *
	 * @param path The absolute path of the remote file
	 */
	protected Path(String path) {
		this.setPath(path);
	}

	/**
	 * Create a new path where you know the local file already exists
	 * and the remote equivalent might be created later.
	 * The remote filename will be extracted from the local file.
	 *
	 * @param parent The absolute path to the parent directory on the remote host
	 * @param local   The associated local file
	 */
	protected Path(String parent, Local local) {
		this.setPath(parent, local);
	}

	public void setPath(String parent, Local file) {
		this.setPath(parent, file.getName());
		this.setLocal(file);
		if(this.getLocal().exists()) {
			this.attributes.setType(this.getLocal().isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
		}
	}

	/**
	 * @param parent The parent directory
	 * @param name   The relative filename
	 */
	public void setPath(String parent, String name) {
		if(parent.charAt(parent.length()-1) == '/') {
			this.setPath(parent+name);
		}
		else {
			this.setPath(parent+"/"+name);
		}
	}

	public void setPath(String p) {
		this.path = p;
	}

	public abstract void reset();

	private Path parent;

	/*
	 * @return My parent directory
	 */
	public Path getParent() {
		if(null == parent) {
			int index = this.getAbsolute().lastIndexOf('/');
			String absolute = null;
			if(index > 0) {
				absolute = this.getAbsolute().substring(0, index);
			}
			else {//if (index == 0) //parent is root
				absolute = "/";
			}
			this.parent = PathFactory.createPath(this.getSession(), absolute);
		}
		return this.parent;
	}

	/**
	 * @throws NullPointerException if session is not initialized
	 */
	public Host getHost() {
		return this.getSession().getHost();
	}

	public void invalidate() {
		this.getSession().cache().remove(this.getAbsolute());
	}

	public List list() {
		return this.list(false);
	}

	public List list(boolean refresh) {
		return this.list(Preferences.instance().getProperty("browser.charset.encoding"),
		    refresh,
		    Preferences.instance().getBoolean("browser.showHidden"));
	}

	public List list(boolean refresh, boolean showHidden) {
		return this.list(Preferences.instance().getProperty("browser.charset.encoding"), refresh, showHidden);
	}

	public List list(boolean refresh, boolean showHidden, boolean notifyObservers) {
		return this.list(Preferences.instance().getProperty("browser.charset.encoding"), refresh, showHidden, notifyObservers);
	}

	/**
	 * Request a file listing from the server. Has to be a directory.
	 */
	public List list(String encoding, boolean refresh, boolean showHidden) {
		return this.list(encoding, refresh, showHidden, true);
	}

	public abstract List list(String encoding, boolean refresh, boolean showHidden, boolean notifyObservers);

	/**
	 * Remove this file from the remote host. Does not affect
	 * any corresponding local file
	 */
	public abstract void delete();

	/**
	 * Changes the session's working directory to this path
	 */
	public abstract void cwdir() throws IOException;

	public void mkdir() {
		this.mkdir(false);
	}

	/**
	 * @param recursive Create intermediate directories as required.  If this option is
	 *                  not specified, the full path prefix of each operand must already exist
	 */
	public abstract void mkdir(boolean recursive);

	/**
	 * @param newFilename Should be an absolute path
	 */
	public abstract void rename(String newFilename);

	/**
	 * @param recursive Include subdirectories and files
	 */
	public abstract void changePermissions(Permission perm, boolean recursive);

	/**
	 * @return true if this paths points to '/'
	 */
	public boolean isRoot() {
		return this.getAbsolute().equals("/") || this.getAbsolute().indexOf('/') == -1;
	}

	/**
	 * @return the path relative to its parent directory
	 */
	public String getName() {
		String abs = this.getAbsolute();
		int index = abs.lastIndexOf('/');
		return (index > 0) ? abs.substring(index+1) : abs.substring(1);
	}

	/**
	 * @return the absolute path name, e.g. /home/user/filename
	 */
	public String getAbsolute() {
		return this.path;
	}

	public void setLocal(Local file) {
		log.debug("setLocal:"+file);
		this.local = file;
	}

	/**
	 * @return The local alias of this path
	 */
	public Local getLocal() {
		//default value if not set explicitly, i.e. with drag and drop
		if(null == this.local) {
			return new Local(Preferences.instance().getProperty("queue.download.folder"), this.getName());
		}
		return this.local;
	}

	public Path getRemote() {
		return this;
	}

	/**
	 * @return the extension if any
	 */
	public String getExtension() {
		String name = this.getName();
		int index = name.lastIndexOf(".");
		if(index != -1) {
			return name.substring(index+1, name.length());
		}
		return null;
	}

	public abstract Session getSession();

	public abstract void download();

	public abstract void upload();

	// ----------------------------------------------------------
	// Transfer methods
	// ----------------------------------------------------------

	/**
	 * ascii upload
	 *
	 * @param reader The stream to read from
	 * @param writer The stream to write to
	 */
	public void upload(java.io.Writer writer, java.io.Reader reader) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug("upload("+writer.toString()+", "+reader.toString());
		}
		this.getSession().log("Uploading "+this.getName()+" (ASCII)", Message.PROGRESS);
		if(this.status.isResume()) {
			long skipped = reader.skip(this.status.getCurrent());
			log.info("Skipping "+skipped+" bytes");
			if(skipped < this.status.getCurrent()) {
				throw new IOException("Resume failed: Skipped "+skipped+" bytes instead of "+this.status.getCurrent());
			}
		}
		this.transfer(reader, writer);
		Growl.instance().notify(NSBundle.localizedString("Upload complete",
														 "Growl Notification"),
								this.getName());
	}

	/**
	 * binary upload
	 *
	 * @param i The stream to read from
	 * @param o The stream to write to
	 */
	public void upload(java.io.OutputStream o, java.io.InputStream i) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug("upload("+o.toString()+", "+i.toString());
		}
		this.getSession().log("Uploading "+this.getName(), Message.PROGRESS);
		if(this.status.isResume()) {
			long skipped = i.skip(this.status.getCurrent());
			log.info("Skipping "+skipped+" bytes");
			if(skipped < this.status.getCurrent()) {
				throw new IOException("Resume failed: Skipped "+skipped+" bytes instead of "+this.status.getCurrent());
			}
		}
		this.transfer(i, o);
		Growl.instance().notify(NSBundle.localizedString("Upload complete",
														 "Growl Notification"),
								this.getName());
	}

	/**
	 * ascii download
	 *
	 * @param reader The stream to read from
	 * @param writer The stream to write to
	 */
	public void download(java.io.Reader reader, java.io.Writer writer) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug("transfer("+reader.toString()+", "+writer.toString());
		}
		this.getSession().log("Downloading "+this.getName()+" (ASCII)", Message.PROGRESS);
		this.transfer(reader, writer);
		//this.getLocal().getTemp().renameTo(this.getLocal());
		Growl.instance().notify(NSBundle.localizedString("Download complete",
														 "Growl Notification"),
								this.getName());
	}

	/**
	 * binary download
	 *
	 * @param i The stream to read from
	 * @param o The stream to write to
	 */
	public void download(java.io.InputStream i, java.io.OutputStream o) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug("transfer("+i.toString()+", "+o.toString());
		}
		this.getSession().log("Downloading "+this.getName(), Message.PROGRESS);
		this.transfer(i, o);
		//this.getLocal().getTemp().renameTo(this.getLocal());
		Growl.instance().notify(NSBundle.localizedString("Download complete",
														 "Growl Notification"),
								this.getName());
	}

	/**
	 * @param reader The stream to read from
	 * @param writer The stream to write to
	 */
	private void transfer(java.io.Reader reader, java.io.Writer writer) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug("transfer("+reader.toString()+", "+writer.toString());
		}
		LineNumberReader in = new LineNumberReader(reader);
		BufferedWriter out = new BufferedWriter(writer);

		long current = this.status.getCurrent();
		boolean complete = false;
		// read/write a line at a time
		String line = null;
		while(!complete && !status.isCanceled()) {
			line = in.readLine();
			if(line == null) {
				complete = true;
			}
			else {
				out.write(line, 0, line.length());
				out.newLine();
				out.flush();
				this.status.setCurrent(current += line.getBytes().length);
			}
		}
		this.status.setComplete(complete);
	}

	/**
	 * @param i The stream to read from
	 * @param o The stream to write to
	 */
	private void transfer(java.io.InputStream i, java.io.OutputStream o) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug("transfer("+i.toString()+", "+o.toString());
		}
		BufferedInputStream in = new BufferedInputStream(i);
		BufferedOutputStream out = new BufferedOutputStream(o);
		int chunksize = Preferences.instance().getInteger("connection.buffer");
		byte[] chunk = new byte[chunksize];
		int amount = 0;
		long current = this.status.getCurrent();
		boolean complete = false;
		// read from socket (bytes) & write to file in chunks
		while(!complete && !status.isCanceled()) {
			amount = in.read(chunk, 0, chunksize);
			if(-1 == amount) {
				complete = true;
			}
			else {
				out.write(chunk, 0, amount);
				out.flush();
				this.status.setCurrent(current += amount);
			}
		}
		this.status.setComplete(complete);
	}

	public void sync() {
		if(this.getRemote().exists() && this.getLocal().exists()) {
			if(this.getLocal().getTimestamp().before(this.attributes.getTimestamp())) {
				this.download();
			}
			if(this.getLocal().getTimestamp().after(this.attributes.getTimestamp())) {
				this.upload();
			}
		}
		else if(this.getRemote().exists()) {
			this.download();
		}
		else if(this.getLocal().exists()) {
			this.upload();
		}
	}

	public boolean exists() {
		return this.getParent().list(false, true, false).contains(this);
	}

	public boolean equals(Object other) {
		if(other instanceof Path) {
			return this.getAbsolute().equals(((Path)other).getAbsolute());
		}
		return false;
	}

	public String toString() {
		return this.getAbsolute();
	}
}
