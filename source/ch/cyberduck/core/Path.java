package ch.cyberduck.core;

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

import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public abstract class Path extends AbstractPath {
    private static Logger log = Logger.getLogger(Path.class);

    /**
     * The absolute remote path
     */
    private String path = null;
    /**
     * The local path to be used if file is copied
     */
    private Local local = null;

    public Status status = new Status();

    /**
     * A compiled representation of a regular expression.
     */
    private Pattern TEXT_FILETYPE_PATTERN = null;

    public Pattern getTextFiletypePattern() {
        final String regex = Preferences.instance().getProperty("filetype.text.regex");
        if(null == TEXT_FILETYPE_PATTERN ||
                !TEXT_FILETYPE_PATTERN.pattern().equals(regex)) {
            try {
                TEXT_FILETYPE_PATTERN = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            }
            catch(PatternSyntaxException e) {
                log.warn(e.getMessage());
            }
        }
        return TEXT_FILETYPE_PATTERN;
    }

    /**
     * A compiled representation of a regular expression.
     */
    private Pattern BINARY_FILETYPE_PATTERN;

    public Pattern getBinaryFiletypePattern() {
        final String regex = Preferences.instance().getProperty("filetype.binary.regex");
        if(null == BINARY_FILETYPE_PATTERN ||
                !BINARY_FILETYPE_PATTERN.pattern().equals(regex)) {
            try {
                BINARY_FILETYPE_PATTERN = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            }
            catch(PatternSyntaxException e) {
                log.warn(e.getMessage());
            }
        }
        return BINARY_FILETYPE_PATTERN;
    }

    private static final String REMOTE = "Remote";
    private static final String LOCAL = "Local";
    private static final String SYMLINK = "Symlink";
    private static final String ATTRIBUTES = "Attributes";

    public Path(NSDictionary dict) {
        Object pathObj = dict.objectForKey(REMOTE);
        if(pathObj != null) {
            this.setPath((String) pathObj);
        }
        Object localObj = dict.objectForKey(LOCAL);
        if(localObj != null) {
            this.setLocal(new Local((String) localObj));
        }
        Object symlinkObj = dict.objectForKey(SYMLINK);
        if(symlinkObj != null) {
            this.setSymbolicLinkPath((String) symlinkObj);
        }
        Object attributesObj = dict.objectForKey(ATTRIBUTES);
        if(attributesObj != null) {
            this.attributes = new PathAttributes((NSDictionary) attributesObj);
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.getAbsolute(), REMOTE);
        dict.setObjectForKey(this.getLocal().toString(), LOCAL);
        if(null != this.getSymbolicLinkPath()) {
            dict.setObjectForKey(this.getSymbolicLinkPath(), SYMLINK);
        }
        dict.setObjectForKey(((PathAttributes) this.attributes).getAsDictionary(), ATTRIBUTES);
        return dict;
    }

    public Object clone() {
        return this.clone(this.getSession());
    }

    public Object clone(Session session) {
        Path copy = PathFactory.createPath(session, this.getAsDictionary());
        copy.attributes = (Attributes) ((PathAttributes) this.attributes).clone();
        return copy;
    }

    {
        attributes = new PathAttributes();
    }

    protected Path() {
        ;
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param parent the absolute directory
     * @param name   the file relative to param path
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
     * @param local  The associated local file
     */
    protected Path(String parent, Local local) {
        this.setPath(parent, local);
    }

    /**
     * @param parent The parent directory
     * @param file   The local file corresponding with this remote path
     */
    public void setPath(String parent, Local file) {
        this.setPath(parent, file.getName());
        this.setLocal(file);
        if(this.getLocal().exists()) {
            this.attributes.setType(this.getLocal().attributes.isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
        }
    }

    /**
     * Normalizes the name before updatings this path. Resets its parent directory
     *
     * @param name Must be an absolute pathname
     */
    public void setPath(String name) {
        this.path = Path.normalize(name);
        this.parent = null;
    }

    public void setParent(Path parent) {
        this.parent = parent;
    }

    /**
     * Reference to the parent created lazily if needed
     */
    private Path parent;

    /**
     * @return My parent directory
     */
    public AbstractPath getParent() {
        if(null == parent) {
            int index = this.getAbsolute().length() - 1;
            if(this.getAbsolute().charAt(index) == '/') {
                if(index > 0)
                    index--;
            }
            int cut = this.getAbsolute().lastIndexOf('/', index);
            if(cut > 0) {
                parent = PathFactory.createPath(this.getSession(), this.getAbsolute().substring(0, cut));
                parent.attributes.setType(Path.DIRECTORY_TYPE);
            }
            else {//if (index == 0) //parent is root
                parent = PathFactory.createPath(this.getSession(), DELIMITER);
                parent.attributes.setType(Path.DIRECTORY_TYPE);
            }
        }
        return this.parent;
    }

    /**
     * @throws NullPointerException if session is not initialized
     */
    public Host getHost() {
        return this.getSession().getHost();
    }

    public Cache cache() {
        return this.getSession().cache();
    }

    public abstract void writeOwner(String owner, boolean recursive);

    public abstract void writeGroup(String group, boolean recursive);

    /**
     * Read the size of the file
     * @see Attributes#getSize()
     */
    public abstract void readSize();

    /**
     * Read the modification date of the file
     * @see Attributes#getModificationDate()
     */
    public abstract void readTimestamp();

    /**
     * Read the file permission of the file
     * @see Attributes#getPermission()
     */
    public abstract void readPermission();

    /**
     * @param p
     * @return true if p is a child of me in the path hierarchy
     */
    public boolean isChild(Path p) {
        for(AbstractPath parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            if(parent.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the path relative to its parent directory
     */
    public String getName() {
        if(this.isRoot()) {
            return DELIMITER;
        }
        String abs = this.getAbsolute();
        int index = abs.lastIndexOf('/');
        return (index > 0) ? abs.substring(index + 1) : abs.substring(1);
    }

    /**
     * @return the absolute path name, e.g. /home/user/filename
     */
    public String getAbsolute() {
        return this.path;
    }

    /**
     * Set the local equivalent of this path
     *
     * @param file Send <code>null</code> to reset the local path to the default value
     */
    public void setLocal(Local file) {
        if(null != file) {
            if(file.attributes.isSymbolicLink()) {
                if(null != file.getSymbolicLinkPath()) {
                    /**
                     * A canonical pathname is both absolute and unique.  The precise
                     * definition of canonical form is system-dependent.  This method first
                     * converts this pathname to absolute form if necessary, as if by invoking the
                     * {@link #getAbsolutePath} method, and then maps it to its unique form in a
                     * system-dependent way.  This typically involves removing redundant names
                     * such as <tt>"."</tt> and <tt>".."</tt> from the pathname, resolving
                     * symbolic links
                     */
                    this.local = new Local(file.getSymbolicLinkPath());
                    return;
                }
            }
        }
        this.local = file;
    }

    /**
     * @return The local alias of this path
     */
    public Local getLocal() {
        //default value if not set explicitly, i.e. with drag and drop
        if(null == this.local) {
            this.local = new Local(this.getHost().getDownloadFolder(), this.getName());
        }
        return this.local;
    }

    /**
     * @return the file type for the extension of this file provided by launch services
     */
    public String kind() {
        if(this.attributes.isSymbolicLink()) {
            if(this.attributes.isFile()) {
                return NSBundle.localizedString("Symbolic Link (File)", "");
            }
            if(this.attributes.isDirectory()) {
                return NSBundle.localizedString("Symbolic Link (Folder)", "");
            }
        }
        if(this.attributes.isFile()) {
            return this.getLocal().kind();
        }
        if(this.attributes.isDirectory()) {
            return NSBundle.localizedString("Folder", "");
        }
        return NSBundle.localizedString("Unknown", "");
    }

    /**
     * @return The session this path uses to send commands
     */
    public abstract Session getSession();

    /**
     * Download with no bandwidth limit
     */
    public void download() {
        this.download(new AbstractStreamListener());
    }

    /**
     *
     * @param listener The stream listener to notify about bytes received and sent
     */
    public void download(StreamListener listener) {
        this.download(new BandwidthThrottle(0) {
            synchronized public int request(int desired) {
                return desired;
            }
        }, listener);
    }

    /**
     * 
     * @param throttle The bandwidth limit
     * @param listener
     */
    public abstract void download(BandwidthThrottle throttle, StreamListener listener);

    /**
     *
     */
    public void upload() {
        this.upload(new AbstractStreamListener());
    }

    /**
     *
     * @param listener The stream listener to notify about bytes received and sent
     */
    public void upload(StreamListener listener) {
        this.upload(new BandwidthThrottle(0) {
            synchronized public int request(int desired) {
                return desired;
            }
        }, listener);
    }

    /**
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     */
    public abstract void upload(BandwidthThrottle throttle, StreamListener listener);

    /**
     * Will copy from in to out. Will attempt to skip Status#getCurrent
     * from the inputstream but not from the outputstream. The outputstream
     * is asssumed to append to a already existing file if
     * Status#getCurrent > 0
     *
     * @param in  The stream to read from
     * @param out The stream to write to
     * @param throttle The bandwidth limit
     * @param l The stream listener to notify about bytes received and sent
     * @throws IOResumeException If the input stream fails to skip the appropriate
     *                           number of bytes
     */
    public void upload(OutputStream out, InputStream in, BandwidthThrottle throttle, final StreamListener l) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("upload(" + out.toString() + ", " + in.toString());
        }
        this.getSession().message(NSBundle.localizedString("Uploading", "Status", "") + " " + this.getName());
        if(status.isResume()) {
            long skipped = in.skip(status.getCurrent());
            log.info("Skipping " + skipped + " bytes");
            if(skipped < status.getCurrent()) {
                throw new IOResumeException("Skipped " + skipped + " bytes instead of " + status.getCurrent());
            }
        }
        this.transfer(in, new ThrottledOutputStream(out, throttle), l);
    }

    /**
     * Will copy from in to out. Does not attempt to skip any bytes from the streams.
     *
     * @param in  The stream to read from
     * @param out The stream to write to
     * @param throttle The bandwidth limit
     * @param l The stream listener to notify about bytes received and sent
     * @throws IOException
     */
    public void download(InputStream in, OutputStream out, BandwidthThrottle throttle, final StreamListener l) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("download(" + in.toString() + ", " + out.toString());
        }
        this.getSession().message(NSBundle.localizedString("Downloading", "Status", "") + " " + this.getName());
        // Only update the file custom icon if the size is > 5MB. Otherwise creating too much
        // overhead when transferring a large amount of files
        final boolean updateIcon = attributes.getSize() > Status.MEGA * 5;
        // Set the first progress icon
        this.getLocal().setIcon(0);
        final StreamListener listener = new StreamListener() {
            int step = 0;

            public void bytesSent(int bytes) {
                l.bytesSent(bytes);
            }

            public void bytesReceived(int bytes) {
                if(-1 == bytes) {
                    // Remove custom icon if complete. The Finder will display the default
                    // icon for this filetype
                    getLocal().setIcon(-1);
                }
                else {
                    l.bytesReceived(bytes);
                    if(updateIcon) {
                        int fraction = (int) (status.getCurrent() / attributes.getSize() * 10);
                        // An integer between 0 and 9
                        if(fraction > step) {
                            // Another 10 percent of the file has been transferred
                            getLocal().setIcon(++step);
                        }
                    }
                }
            }
        };
        this.transfer(new ThrottledInputStream(in, throttle), out, listener);
    }

    /**
     *
     * @param in  The stream to read from
     * @param out The stream to write to
     * @param listener The stream listener to notify about bytes received and sent
     * @throws IOException
     */
    private void transfer(InputStream in, OutputStream out, StreamListener listener) throws IOException {
        final int chunksize = 32768;
        byte[] chunk = new byte[chunksize];
        long bytesTransferred = status.getCurrent();
        while(!status.isCanceled()) {
            int read = in.read(chunk, 0, chunksize);
            listener.bytesReceived(read);
            if(-1 == read) {
                // End of file
                status.setComplete(true);
                break;
            }
            out.write(chunk, 0, read);
            listener.bytesSent(read);
            bytesTransferred += read;
            status.setCurrent(bytesTransferred);
        }
        out.flush();
    }

    /**
     * @return true if the path exists (or is cached!)
     */
    public boolean exists() {
        if(this.isRoot()) {
            return true;
        }
        return this.getParent().childs().contains(this);
    }

    /**
     * @return The hashcode of #getAbsolute()
     * @see #getAbsolute()
     */
    public int hashCode() {
        return this.getAbsolute().hashCode();
    }

    /**
     * @param other
     * @return true if the other path has the same absolute path name
     */
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Path) {
            //BUG: returns the wrong result on case-insensitive systems, e.g. NT!
            return this.getAbsolute().equals(((AbstractPath) other).getAbsolute());
        }
        return false;
    }

    /**
     * @return The absolute path name
     */
    public String toString() {
        return this.getAbsolute();
    }

    /**
     * @return Null if there is a encoding failure
     */
    public String toURL() {
        try {
            StringBuffer b = new StringBuffer();
            StringTokenizer t = new StringTokenizer(this.getAbsolute(), "/");
            while(t.hasMoreTokens()) {
                b.append(DELIMITER + URLEncoder.encode(t.nextToken(), "UTF-8"));
            }
            // Do not use java.net.URL because it doesn't know about SFTP!
            return this.getHost().getURL() + b.toString();
        }
        catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + super.toString());
        super.finalize();
    }

    /**
     * @see Session#error(Path,String,Throwable)
     */
    protected void error(String message, Throwable e) {
        this.getSession().error(this, message, e);
    }
}