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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A path is a remote directory or file.
 *
 * @version $Id$
 */
public abstract class Path {
    private static Logger log = Logger.getLogger(Path.class);

    private String path = null;
	
    public Local local = null;
	public Path remote = this;
	
    public Status status = new Status();
    public Attributes attributes = new Attributes();

    public static final int FILE_TYPE = 1;
    public static final int DIRECTORY_TYPE = 2;
    public static final int SYMBOLIC_LINK_TYPE = 4;

    public static final String HOME = "~";

    /**
     * Deep copies the current path with its attributes but without the status information
     *
     * @param session The session this path will use to fullfill its tasks
     * @return A copy of me with a new session
     */
    public Path copy(Session s) {
        Path copy = PathFactory.createPath(s, this.getAbsolute()); //@todo session.copy()
        copy.attributes = this.attributes;
        copy.status.setSize(this.status.getSize());
        return copy;
    }

    public Path(NSDictionary dict) {
        Object pathObj = dict.objectForKey("Remote");
        if (pathObj != null) {
            this.setPath((String)pathObj);
        }
        Object localObj = dict.objectForKey("Local");
        if (localObj != null) {
            this.setLocal(new Local((String)localObj));
        }
        Object attributesObj = dict.objectForKey("Attributes");
        if (attributesObj != null) {
            this.attributes = new Attributes((NSDictionary)attributesObj);
        }
        Object statusObj = dict.objectForKey("Status");
        if (statusObj != null) {
            this.status = new Status((NSDictionary)statusObj);
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.getAbsolute(), "Remote");
        dict.setObjectForKey(this.getLocal().toString(), "Local");
        dict.setObjectForKey(this.attributes.getAsDictionary(), "Attributes");
        dict.setObjectForKey(this.status.getAsDictionary(), "Status");
        return dict;
    }

    public Path() {
        super();
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param path the absolute directory
     * @param name the file relative to param path
     */
    public Path(String parent, String name) {
        this.setPath(parent, name);
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param path The absolute path of the remote file
     */
    public Path(String path) {
        this.setPath(path);
    }

    /**
     * Create a new path where you know the local file already exists
     * and the remote equivalent might be created later.
     * The remote filename will be extracted from the local file.
     *
     * @param parent The absolute path to the parent directory on the remote host
     * @param file   The associated local file
     */
    public Path(String parent, Local file) {
        this.setPath(parent, file);
    }

    public void setPath(String parent, Local file) {
        this.setPath(parent, file.getName());
        this.setLocal(file);
    }

    /**
     * @param parent The parent directory
     * @param name   The relative filename
     */
    public void setPath(String parent, String name) {
        if (parent.charAt(parent.length() - 1) == '/') {
            this.setPath(parent + name);
        }
        else {
            this.setPath(parent + "/" + name);
        }
    }

    public void setPath(String p) {
        this.path = p;
    }

    /**
     * @return My parent directory
     */
    public Path getParent() {
        int index = this.getAbsolute().lastIndexOf('/');
        String parent = null;
        if (index > 0) {
            parent = this.getAbsolute().substring(0, index);
        }
        else {//if (index == 0) //parent is root
            parent = "/";
        }
        return PathFactory.createPath(this.getSession(), parent);
    }

    /**
     * @throws NullPointerException if session is not initialized
     */
    public Host getHost() {
        return this.getSession().getHost();
    }

    /**
     * @return My directory listing
     * @throws NullPointerException if session is not initialized
     */
    public List cache() {
        return this.getSession().cache().get(this.remote.getAbsolute());
    }

    protected void setCache(List files) {
        this.getSession().cache().put(this.remote.getAbsolute(), files);
    }

    public void invalidate() {
        this.getSession().cache().remove(this.remote.getAbsolute());
    }

    /**
     * Request a file listing from the server. Has to be a directory
     */
    public abstract List list(boolean refresh, boolean showHidden);

    public abstract List list(boolean refresh);

    public abstract List list();

    /**
     * Remove this file from the remote host. Does not affect
     * any corresponding local file
     */
    public abstract void delete();

    /**
     * Changes the session's working directory to this path
     */
    public abstract void cwdir() throws IOException;

    /**
     * @param recursive Create intermediate directories as required.  If this option is
     *                  not specified, the full path prefix of each operand must already exist
     */
    public abstract void mkdir(boolean recursive);

    /**
     * @param newFilename Should be an absolute path
     */
    public abstract void rename(String newFilename);

	public abstract java.util.Date modificationDate();

	public abstract long size();

    /**
     * @param recursive Include subdirectories and files
     */
    public abstract void changePermissions(Permission perm, boolean recursive);

    public boolean exists() {
        boolean exists;
        if (this.remote.isRoot()) {
            return true;
        }
        return this.getParent().list(false, true).contains(this);
    }
		
    /**
     * @return true if this paths points to '/'
     */
    public boolean isRoot() {
        return this.remote.getAbsolute().equals("/") || this.remote.getAbsolute().indexOf('/') == -1;
    }

    /**
     * @return the path relative to its parent directory
     */
    public String getName() {
        String abs = this.remote.getAbsolute();
        int index = abs.lastIndexOf('/');
        return (index > 0) ? abs.substring(index + 1) : abs.substring(1);
    }

    /**
     * @return the absolute path name, e.g. /home/user/filename
     */
    public String getAbsolute() {
        return this.path;
    }

    public void setLocal(Local file) {
        this.local = file;
    }

    /**
     * @return The local alias of this path
     */
    public Local getLocal() {
        //default value if not set explicitly, i.e. with drag and drop
        if (null == this.local) {
            return new Local(Preferences.instance().getProperty("queue.download.folder"), this.remote.getName());
        }
        return this.local;
    }

    /**
     * @return the extension if any
     */
    public String getExtension() {
        String name = this.getName();
        int index = name.lastIndexOf(".");
        if (index != -1) {
            return name.substring(index + 1, name.length());
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
        if (log.isDebugEnabled()) {
            log.debug("upload(" + writer.toString() + ", " + reader.toString());
        }
        this.getSession().log("Uploading " + this.getName() + " (ASCII)", Message.PROGRESS);
        if (this.status.isResume()) {
            long skipped = reader.skip(this.status.getCurrent());
            log.info("Skipping " + skipped + " bytes");
            if (skipped < this.status.getCurrent()) {
                throw new IOException("Resume failed: Skipped " + skipped + " bytes instead of " + this.status.getCurrent());
            }
        }
        this.transfer(reader, writer);
    }

    /**
     * binary upload
     *
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void upload(java.io.OutputStream o, java.io.InputStream i) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("upload(" + o.toString() + ", " + i.toString());
        }
        this.getSession().log("Uploading " + this.getName(), Message.PROGRESS);
        if (this.status.isResume()) {
            long skipped = i.skip(this.status.getCurrent());
            log.info("Skipping " + skipped + " bytes");
            if (skipped < this.status.getCurrent()) {
                throw new IOException("Resume failed: Skipped " + skipped + " bytes instead of " + this.status.getCurrent());
            }
        }
        this.transfer(i, o);
    }

    /**
     * ascii download
     *
     * @param reader The stream to read from
     * @param writer The stream to write to
     */
    public void download(java.io.Reader reader, java.io.Writer writer) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("transfer(" + reader.toString() + ", " + writer.toString());
        }
        this.getSession().log("Downloading " + this.getName() + " (ASCII)", Message.PROGRESS);
        this.transfer(reader, writer);
        //this.getLocal().getTemp().renameTo(this.getLocal());
    }

    /**
     * binary download
     *
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void download(java.io.InputStream i, java.io.OutputStream o) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("transfer(" + i.toString() + ", " + o.toString());
        }
        this.getSession().log("Downloading " + this.getName(), Message.PROGRESS);
        this.transfer(i, o);
        //this.getLocal().getTemp().renameTo(this.getLocal());
    }

    /**
     * @param reader The stream to read from
     * @param writer The stream to write to
     */
    private void transfer(java.io.Reader reader, java.io.Writer writer) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("transfer(" + reader.toString() + ", " + writer.toString());
        }
        LineNumberReader in = new LineNumberReader(reader);
        BufferedWriter out = new BufferedWriter(writer);

        long current = this.status.getCurrent();
        boolean complete = false;
        // read/write a line at a time
        String line = null;
        while (!complete && !status.isCanceled()) {
            line = in.readLine();
            if (line == null) {
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
        if (log.isDebugEnabled()) {
            log.debug("transfer(" + i.toString() + ", " + o.toString());
        }
        BufferedInputStream in = new BufferedInputStream(i);
        BufferedOutputStream out = new BufferedOutputStream(o);
        int chunksize = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
        byte[] chunk = new byte[chunksize];
        int amount = 0;
        long current = this.status.getCurrent();
        boolean complete = false;
        // read from socket (bytes) & write to file in chunks
        while (!complete && !status.isCanceled()) {
            amount = in.read(chunk, 0, chunksize);
            if (-1 == amount) {
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
        try {
			this.getSession().check();
			if(this.remote.exists() && this.local.exists()) {
				if(this.local.getTimestamp().before(this.attributes.getTimestamp())) {
					this.download();
				}
				if(this.local.getTimestamp().after(this.attributes.getTimestamp())) {
					this.upload();
				}
			}
			else if(this.remote.exists()) {
				this.download();
			}
			else if(this.local.exists()) {
				this.upload();
			}
			this.getSession().log("Idle", Message.STOP);
		}
        catch (IOException e) {
            this.getSession().log("IO Error: " + e.getMessage(), Message.ERROR);
        }
	}
	
    public boolean equals(Object other) {
        if (other instanceof Path) {
            return this.getAbsolute().equals(((Path)other).getAbsolute());
        }
        return false;
    }

    public String toString() {
        return this.getAbsolute();
    }
}
