package ch.cyberduck.core;

/*
 *  ch.cyberduck.core.Path.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import java.text.DateFormat;
import java.util.List;
import java.util.Date;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

/**
 * A path is a remote directory or file.
 * @version $Id$
 */
public abstract class Path implements Serializable, Transferable {

    private static Logger log = Logger.getLogger(Path.class);

    private String name = null;
    private String path = null;
    protected Path parent = null;
    
    private int size;
    private long modified;

    private String owner;
    private String group;
    private String access;
    private Permission permission;

    private boolean visible = true;

    public static final DataFlavor pathFlavor
        = new DataFlavor(ch.cyberduck.core.Path.class, "ch.cyberduck.core.Path");

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{pathFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(pathFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if(flavor.equals(pathFlavor))
            return this;
        throw new UnsupportedFlavorException(flavor);
    }

    /**
     * @param path the absolute directory
     * @param name the file relative to param path
     */
    public Path(String path, String name) {
        //log.debug("[Path] Path(" + path+","+name+")");
        if(path.charAt(path.length() -1) == '/')
            this.init(path + name);
        else
            this.init(path + "/" + name);
    }

    /**
     * @param path The absolute path of the file
     */
    public Path(String path) {
        this.init(path);
    }

    private void init(String pathname) {
	log.debug("init:"+pathname);
        this.path = pathname.trim();
	/*
        if(this.isDirectory()) {
           // if(!this.isRoot()) {
          //      this.parent = this.getPathFragment(this.getPathDepth() - 1);
          //  }
            if(this.getPathDepth() > 1) {
                this.name = p.substring(parent.toString().length(), p.lastIndexOf('/'));
            }
            else if(this.getPathDepth() > 0) {// must always be true if this.isDirectory()
                this.name = p.substring(1, p.lastIndexOf('/'));
            }
        }
        else if(this.isFile()) {
            this.parent = this.getPathFragment(this.getPathDepth());
            this.name = p.substring(p.lastIndexOf('/') + 1);
        }
//        log.debug("Path:" + this.path + ", Name:" + this.getName() + ", Parent:" + this.getParent());
	 */
    }

    /**
     * Only use this if you know what you do! Use constructor by default.
     * @see #Path(String, String)
     */
    /*
    private void setPath(String p) {
        log.debug("[Path] setPath(" + p + ")");
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < p.length(); i++) {
            if(! Character.isWhitespace(p.charAt(i)))
                buffer.append(p.charAt(i));
        }
        this.path = buffer.toString();
    }
     */

    /**
	* @return my parent directory
     */
    public abstract Path getParent();
    
    /**
	* @return Path[] The children
     * Return the content of this directory.
     */
    public abstract List list();

    public abstract void delete();

    public abstract void mkdir();

    public abstract void rename(String n);
    
    /**
	* Overwrite this is in the implementation to determine the file type uppon the
     * server response.
     * @return true even if the file doesn't exist on the local filesystem
     * but seems to be a file because there isn't a '/' at
     * the end of the path.
     */
    public boolean isFile() {
        String path = this.toString();
        if(path.lastIndexOf('/') == path.length() - 1) {
            return false;
        }
        return true;
    }

    /**
	* Overwrite this is in the implementation to determine the file type uppon the
     * server response.
     * @return true  even if the directory doesn't exist on the local filesystem
     * but seems to be a directory because it ends with '/'
     */
    public boolean isDirectory() {
//        log.debug("[Path] isDirectory()");
        String path = this.toString();
        if(path.lastIndexOf('/') == path.length() - 1) {
            return true;
        }
        return false;
    }

    /**
     * @return The filename if the path is a file
     * or the full path if it is a directory
     */
    public String getName() {
	if(name == null) {
	    String abs = getAbsolute();
	    int index = abs.lastIndexOf("/");
	    name = (index > 0) ? abs.substring(index + 1) : abs;
	}
	return name;
    }

    /**
     * @return the absolute path name
     */
    public String getAbsolute() {
//	log.debug("getAbsolute:"+this.path);
	return this.path;
    }

    
    /**
	* @return Returns the number of '/' characters in a path
     */
    /*
    public int getPathDepth() {
        int depth = 0;
        int length = 0;
        while((length = this.toString().indexOf('/', length + 1)) != -1) {
            depth++;
        }
        return depth;
    }
     */
    
    /*
    * Returns a path relative the parameter
    * @param relative 
    */

    /*@todo
    public Path getRelativePath(Path relative) {
    	int index = this.getAbsolute().indexOf(relative.getAbsolute());
    	if(index == -1) {
    		throw new IllegalArgumentException("The argument must be part of this path");
    	}
    	else {
    		return new Path(this.getAbsolute().substring(index + relative.getAbsolute().length()));
    	}
    }
*/
    /**
     * @ param depth the '/'
     * @ return a new Path cut to the length of parameter <code>depth</code>
     */

    /*@todo
	
    public Path getPathFragment(int depth) throws IllegalArgumentException {
//        log.debug("[Path] getPathFragment(" + depth + ")");
        if(depth > this.getPathDepth())
            throw new IllegalArgumentException("Path is not that long: " + depth + " > " + this.getPathDepth());
        if(depth > 0) {
            int length = 1; //@modification
            for (int n = 0; n < depth; n++) {
                if((length = this.toString().indexOf('/', length + 1)) < 0) {
                    break;
                }
            }
            if(length > 0)
                return new Path(this.toString().substring(0, length + 1));
            else {
                return new Path(this.toString());
            }
        }
        else {
            return new Path("/");
        }
    }
    */

    /*
    public static String encode(String path) {
        return java.net.URLEncoder.encode(path);
    }
     */

    /**
     * @param size The size of the file
     */
    public void setSize(int size) {
	log.debug("setSize:"+size);
        this.size = size;
    }


    private static final int KILO = 1024; //2^10
    private static final int MEGA = 1048576; // 2^20
    private static final int GIGA = 1073741824; // 2^30
    
    /**
     * @return The size of the file
     */
    public String getSize() {
        if(size < KILO) {
            return size + " B";
        }
        else if(size < MEGA) {
            return new Double(size/KILO).intValue() + " KB";
        }
        else if(size < GIGA) {
            return new Double(size/MEGA).intValue() + " MB";
        }
        else {
            return new Double(size/GIGA).intValue() + " GB";
        }
    }

    /**
        * Set the modfication returned by ftp directory listings
     */
    public void setModified(long m) {
        this.modified = m;
    }

    /**
     * @return the modification date of this file
     */
    public String getModified() {
        return (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)).format(new Date(this.modified));
    }

    /**
     * @param access unix access permitions, i.e. -rwxrwxrwx
     */
    public void setMode(String access) {
        this.access = access;
    }

    /**
        * @return The unix access permissions
     */
    public String getMode() {
        return this.access;
    }

    public void setPermission(Permission p) {
        this.permission = p;
    }

    public Permission getPermission() {
        return this.permission;
    }
    
    public void setOwner(String o) {
        this.owner = o;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setGroup(String g) {
        this.group = g;
    }

    public String getGroup() {
        return this.group;
    }
    
    /**
     * @param visible If this path should be shown in the directory listing
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
        * @return If this path is shown in the directory listing
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
        * @return true if this paths points to '/'
     */
    public boolean isRoot() {
        return this.getAbsolute().equals("/");
    }
    
    public String toString() {
        return this.getAbsolute().toString();
    }
}
