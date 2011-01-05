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

import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.icu.text.Normalizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public abstract class Path extends AbstractPath implements Serializable {
    private static Logger log = Logger.getLogger(Path.class);

    /**
     * The absolute remote path
     */
    private String path;

    /**
     * The local path to be used if file is copied
     */
    private Local local;

    /**
     *
     */
    private Status status;

    /**
     * A compiled representation of a regular expression.
     */
    private Pattern TEXT_FILETYPE_PATTERN = null;

    /**
     *
     */
    private static final int CHUNKSIZE = Preferences.instance().getInteger("connection.chunksize");

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

    protected <T> Path(T dict) {
        this.init(dict);
    }

    public <T> void init(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        this.init(dict);
    }

    protected void init(Deserializer dict) {
        String pathObj = dict.stringForKey("Remote");
        if(pathObj != null) {
            this.setPath(pathObj);
        }
        String localObj = dict.stringForKey("Local");
        if(localObj != null) {
            this.setLocal(LocalFactory.createLocal(localObj));
        }
        String symlinkObj = dict.stringForKey("Symlink");
        if(symlinkObj != null) {
            this.setSymlinkTarget(symlinkObj);
        }
        final Object attributesObj = dict.objectForKey("Attributes");
        if(attributesObj != null) {
            this.attributes = new PathAttributes(attributesObj);
        }
        if(dict.stringForKey("Complete") != null) {
            this.status().setComplete(true);
        }
    }

    public <S> S getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        return (S) this.getAsDictionary(dict);
    }

    protected <S> S getAsDictionary(Serializer dict) {
        dict.setStringForKey(this.getAbsolute(), "Remote");
        if(local != null) {
            dict.setStringForKey(local.toString(), "Local");
        }
        if(StringUtils.isNotBlank(this.getSymlinkTarget())) {
            dict.setStringForKey(this.getSymlinkTarget(), "Symlink");
        }
        dict.setObjectForKey(attributes, "Attributes");
        if(this.status().isComplete()) {
            dict.setStringForKey(String.valueOf(true), "Complete");
        }
        return dict.<S>getSerialized();
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param parent the absolute directory
     * @param name   the file relative to param path
     */
    protected Path(String parent, String name, int type) {
        this.setPath(parent, name);
        this.attributes().setType(type);
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param path The absolute path of the remote file
     */
    protected Path(String path, int type) {
        this.setPath(path);
        this.attributes().setType(type);
    }

    /**
     * Create a new path where you know the local file already exists
     * and the remote equivalent might be created later.
     * The remote filename will be extracted from the local file.
     *
     * @param parent The absolute path to the parent directory on the remote host
     * @param local  The associated local file
     */
    protected Path(String parent, final Local local) {
        this.setPath(parent, local);
        this.attributes().setType(
                local.attributes().isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
    }

    /**
     * @param parent The parent directory
     * @param file   The local file corresponding with this remote path
     */
    protected void setPath(String parent, final Local file) {
        this.setPath(parent, file.getName());
        this.setLocal(file);
    }

    /**
     * @param parent
     * @param name
     */
    protected void setPath(Path parent, String name) {
        super.setPath(parent.getAbsolute(), name);
        this.setParent(parent);
    }

    /**
     * Normalizes the name before updatings this path. Resets its parent directory
     *
     * @param name Must be an absolute pathname
     */
    @Override
    protected void setPath(String name) {
        this.path = Path.normalize(name);
        this.parent = null;
        this.reference = null;
    }

    public void setParent(Path parent) {
        if(this.isChild(parent)) {
            this.parent = parent;
        }
        else {
            log.warn("Attempt to set invalid parent directory:" + parent);
        }
    }

    /**
     * The path delimiter for remote paths
     */
    public static final char DELIMITER = '/';

    @Override
    public char getPathDelimiter() {
        return String.valueOf(DELIMITER).charAt(0);
    }

    public static String normalize(final String path) {
        return normalize(path, true);
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.
     *
     * @param path     The path to parse
     * @param absolute If the path is absolute
     * @return the normalized path.
     * @author Adapted from org.apache.webdav
     * @license http://www.apache.org/licenses/LICENSE-2.0
     */
    public static String normalize(final String path, final boolean absolute) {
        if(null == path) {
            return String.valueOf(DELIMITER);
        }
        String normalized = path;
        if(Preferences.instance().getBoolean("path.normalize")) {
            if(absolute) {
                while(!normalized.startsWith("\\\\") && !normalized.startsWith(String.valueOf(DELIMITER))) {
                    normalized = DELIMITER + normalized;
                }
            }
            while(!normalized.endsWith(String.valueOf(DELIMITER))) {
                normalized += DELIMITER;
            }
            // Resolve occurrences of "/./" in the normalized path
            while(true) {
                int index = normalized.indexOf("/./");
                if(index < 0) {
                    break;
                }
                normalized = normalized.substring(0, index) +
                        normalized.substring(index + 2);
            }
            // Resolve occurrences of "/../" in the normalized path
            while(true) {
                int index = normalized.indexOf("/../");
                if(index < 0) {
                    break;
                }
                if(index == 0) {
                    // The only left path is the root.
                    return String.valueOf(DELIMITER);
                }
                normalized = normalized.substring(0, normalized.lastIndexOf(DELIMITER, index - 1)) +
                        normalized.substring(index + 3);
            }
            StringBuilder n = new StringBuilder();
            if(normalized.startsWith("//")) {
                // see #972. Omit leading delimiter
                n.append(DELIMITER);
                n.append(DELIMITER);
            }
            else if(normalized.startsWith("\\\\")) {
                ;
            }
            else if(absolute) {
                // convert to absolute path
                n.append(DELIMITER);
            }
            else if(normalized.startsWith(String.valueOf(DELIMITER))) {
                // Keep absolute path
                n.append(DELIMITER);
            }
            // Remove duplicated delimiters
            String[] segments = normalized.split(String.valueOf(DELIMITER));
            for(String segment : segments) {
                if(segment.equals(StringUtils.EMPTY)) {
                    continue;
                }
                n.append(segment);
                n.append(DELIMITER);
            }
            normalized = n.toString();
            while(normalized.endsWith(String.valueOf(DELIMITER)) && normalized.length() > 1) {
                //Strip any redundant delimiter at the end of the path
                normalized = normalized.substring(0, normalized.length() - 1);
            }
        }
        if(Preferences.instance().getBoolean("path.normalize.unicode")) {
            if(!Normalizer.isNormalized(normalized, Normalizer.NFC, Normalizer.UNICODE_3_2)) {
                // Canonical decomposition followed by canonical composition (default)
                normalized = Normalizer.normalize(normalized, Normalizer.NFC, Normalizer.UNICODE_3_2);
            }
        }
        // Return the normalized path that we have completed
        return normalized;
    }

    /**
     * @return
     */
    public boolean isContainer() {
        return this.equals(this.getContainer());
    }

    /**
     * @return
     */
    public String getContainerName() {
        if(StringUtils.isNotBlank(this.getHost().getDefaultPath())) {
            return Path.normalize(this.getHost().getDefaultPath(), true);
        }
        return String.valueOf(DELIMITER);
    }

    /**
     * @return
     */
    public Path getContainer() {
        return PathFactory.createPath(this.getSession(), this.getContainerName(),
                Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
    }

    /**
     * Reference to the parent created lazily if needed
     */
    private Path parent;

    /**
     * @return My parent directory
     */
    @Override
    public Path getParent() {
        if(null == parent) {
            if(this.isRoot()) {
                return this;
            }
            String name = this.getParent(this.getAbsolute());
            if(String.valueOf(DELIMITER).equals(name)) {
                parent = PathFactory.createPath(this.getSession(), String.valueOf(DELIMITER),
                        Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
            }
            else {
                parent = PathFactory.createPath(this.getSession(), name,
                        Path.DIRECTORY_TYPE);
            }
        }
        return parent;
    }

    /**
     * Attributes denoting this path
     */
    private PathAttributes attributes;

    @Override
    public PathAttributes attributes() {
        if(null == attributes) {
            attributes = new PathAttributes();
        }
        return attributes;
    }

    /**
     * @return
     */
    public Status status() {
        if(null == status) {
            status = new Status();
        }
        return status;
    }

    /**
     * @return Null if the connection has been closed
     */
    public Host getHost() {
        return this.getSession().getHost();
    }

    @Override
    public void invalidate() {
        if(this.attributes().isDirectory()) {
            this.getSession().cdn().clear();
        }
        super.invalidate();
    }

    @Override
    public AttributedList<Path> list() {
        return this.list(new AttributedList<Path>() {
            @Override
            public boolean add(Path path) {
                if(!path.isChild(Path.this)) {
                    log.warn("Skip adding child to directory listing:" + path);
                    return false;
                }
                return super.add(path);
            }

            @Override
            public boolean addAll(Collection<? extends Path> c) {
                for(Path path : c) {
                    this.add(path);
                }
                return true;
            }
        });
    }

    protected abstract AttributedList<Path> list(AttributedList<Path> children);

    /**
     * Accessability for #getSession.cache()
     *
     * @return
     */
    @Override
    public Cache<Path> cache() {
        return this.getSession().cache();
    }

    public void writeOwner(String owner, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    public void writeGroup(String group, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    /**
     * No checksum calculation by default. Might be supported by specific
     * provider implementation.
     */
    public void readChecksum() {
        ;
    }

    /**
     * Read the size of the file
     *
     * @see Attributes#getSize()
     */
    public abstract void readSize();

    /**
     * Read the modification date of the file
     *
     * @see Attributes#getModificationDate()
     * @see ch.cyberduck.core.Session#isTimestampSupported()
     */
    public abstract void readTimestamp();

    /**
     * Read the file permission of the file
     *
     * @see Attributes#getPermission()
     * @see Session#isUnixPermissionsSupported()
     */
    public abstract void readUnixPermission();

    /**
     * @param acl       The permissions to apply
     * @param recursive Include subdirectories and files
     */
    public void writeAcl(Acl acl, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void readAcl() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the path relative to its parent directory
     */
    @Override
    public String getName() {
        if(this.isRoot()) {
            return String.valueOf(DELIMITER);
        }
        final String abs = this.getAbsolute();
        int index = abs.lastIndexOf(DELIMITER);
        return abs.substring(index + 1);
    }

    public String getKey() {
        return this.getWebPath(this.getAbsolute());
    }

    /**
     * @return the absolute path name, e.g. /home/user/filename
     */
    @Override
    public String getAbsolute() {
        return this.path;
    }

    /**
     * Set the local equivalent of this path
     *
     * @param file Send <code>null</code> to reset the local path to the default value
     */
    public void setLocal(Local file) {
        this.local = file;
    }

    /**
     * @return The local alias of this path
     */
    public Local getLocal() {
        if(null == this.local) {
            return getDefaultLocal();
        }
        return this.local;
    }

    private Local getDefaultLocal() {
        return LocalFactory.createLocal(this.getHost().getDownloadFolder(), this.getName());
    }

    /**
     * @param parent Absolute path to the symbolic link
     * @param name   Target of the symbolic link name. Absolute or relative pathname
     */
    public void setSymlinkTarget(String parent, String name) {
        if(name.startsWith(String.valueOf(DELIMITER))) {
            // Symbolic link target may be an absolute path
            this.setSymlinkTarget(name);
        }
        else {
            if(parent.endsWith(String.valueOf(DELIMITER))) {
                this.setSymlinkTarget(parent + name);
            }
            else {
                this.setSymlinkTarget(parent + DELIMITER + name);
            }
        }
    }

    /**
     * An absolute reference here the symbolic link is pointing to
     */
    private String symbolic = null;

    public void setSymlinkTarget(String p) {
        this.symbolic = Path.normalize(p);
    }

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link
     * @see ch.cyberduck.core.PathAttributes#isSymbolicLink
     */
    @Override
    public String getSymlinkTarget() {
        if(this.attributes().isSymbolicLink()) {
            return this.symbolic;
        }
        return null;
    }

    /**
     * @return the file type for the extension of this file provided by launch services
     */
    @Override
    public String kind() {
        if(this.attributes().isSymbolicLink()) {
            if(this.attributes().isFile()) {
                return Locale.localizedString("Symbolic Link (File)");
            }
            if(this.attributes().isDirectory()) {
                return Locale.localizedString("Symbolic Link (Folder)");
            }
        }
        if(this.attributes().isFile()) {
            return this.getLocal().kind();
        }
        if(this.attributes().isDirectory()) {
            return Locale.localizedString("Folder");
        }
        return Locale.localizedString("Unknown");
    }

    /**
     * @return The session this path uses to send commands
     * @throws ConnectionCanceledException If the connection has been closed already
     */
    public abstract Session getSession();

    /**
     * Upload an empty file.
     */
    @Override
    public void touch() {
        if(this.attributes().isFile()) {
            int no = 0;
            final String filename = this.getLocal().getName();
            this.setLocal(LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"), filename));
            while(this.getLocal().exists()) {
                no++;
                String proposal = FilenameUtils.getBaseName(filename) + "-" + no;
                if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                    proposal += "." + FilenameUtils.getExtension(filename);
                }
                this.setLocal(LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"), proposal));
            }
            this.getLocal().touch(true);
            TransferOptions options = new TransferOptions();
            options.closeSession = false;
            try {
                UploadTransfer upload = new UploadTransfer(this);
                upload.start(new TransferPrompt() {
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
                this.getParent().invalidate();
            }
            finally {
                this.getLocal().delete(false);
                this.setLocal(null);
            }
        }
    }

    /**
     * Versioning support.
     */
    public void revert() {
        throw new UnsupportedOperationException();
    }

    /**
     * Download with no bandwidth limit
     */
    protected void download() {
        this.download(new AbstractStreamListener());
    }

    /**
     * @param check Check for open connection and open if needed before transfer
     */
    protected void download(final boolean check) {
        this.download(new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), check);
    }

    /**
     * @param listener The stream listener to notify about bytes received and sent
     */
    protected void download(StreamListener listener) {
        this.download(new BandwidthThrottle(BandwidthThrottle.UNLIMITED), listener);
    }

    /**
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     */
    protected void download(BandwidthThrottle throttle, StreamListener listener) {
        this.download(throttle, listener, false);
    }

    /**
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     * @param check    Check for open connection and open if needed before transfer
     */
    protected abstract void download(BandwidthThrottle throttle, StreamListener listener, boolean check);

    /**
     *
     */
    protected void upload() {
        this.upload(new AbstractStreamListener());
    }

    /**
     * @param listener The stream listener to notify about bytes received and sent
     */
    protected void upload(StreamListener listener) {
        this.upload(new BandwidthThrottle(BandwidthThrottle.UNLIMITED), listener);
    }

    /**
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     */
    protected void upload(BandwidthThrottle throttle, StreamListener listener) {
        this.upload(throttle, listener, false);
    }

    /**
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     * @param check    Check for open connection and open if needed before transfer
     */
    protected abstract void upload(BandwidthThrottle throttle, StreamListener listener, boolean check);

    protected void upload(OutputStream out, InputStream in, BandwidthThrottle throttle, final StreamListener l) throws IOException {
        this.upload(out, in, throttle, l, status().getCurrent(), -1);
    }

    /**
     * Will copy from in to out. Will attempt to skip Status#getCurrent
     * from the inputstream but not from the outputstream. The outputstream
     * is asssumed to append to a already existing file if
     * Status#getCurrent > 0
     *
     * @param out      The stream to write to
     * @param in       The stream to read from
     * @param throttle The bandwidth limit
     * @param l        The stream listener to notify about bytes received and sent
     * @param offset
     * @throws IOResumeException           If the input stream fails to skip the appropriate
     *                                     number of bytes
     * @throws IOException                 Write not completed due to a I/O problem
     * @throws ConnectionCanceledException When transfer is interrupted by user setting the
     *                                     status flag to cancel.
     */
    protected void upload(OutputStream out, InputStream in, BandwidthThrottle throttle, final StreamListener l, long offset, final long limit) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("upload(" + out.toString() + ", " + in.toString());
        }
        this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                this.getName()));

        if(offset > 0) {
            long skipped = in.skip(offset);
            log.info("Skipping " + skipped + " bytes");
            if(skipped < status().getCurrent()) {
                throw new IOResumeException("Skipped " + skipped + " bytes instead of " + status().getCurrent());
            }
        }
        this.transfer(in, new ThrottledOutputStream(out, throttle), l, limit);
    }

    /**
     * Will copy from in to out. Does not attempt to skip any bytes from the streams.
     *
     * @param in       The stream to read from
     * @param out      The stream to write to
     * @param throttle The bandwidth limit
     * @param l        The stream listener to notify about bytes received and sent
     * @throws IOException                 Write not completed due to a I/O problem
     * @throws ConnectionCanceledException When transfer is interrupted by user setting the
     *                                     status flag to cancel.
     */
    protected void download(InputStream in, OutputStream out, BandwidthThrottle throttle, final StreamListener l) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("download(" + in.toString() + ", " + out.toString());
        }
        this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                this.getName()));

        // Only update the file custom icon if the size is > 5MB. Otherwise creating too much
        // overhead when transferring a large amount of files
        final boolean updateIcon = attributes().getSize() > Status.MEGA * 5;

        final Local local = this.getLocal();
        // Set the first progress icon
        local.setIcon(0);

        if(Preferences.instance().getBoolean("queue.download.quarantine")) {
            // Set quarantine attributes
            local.setQuarantine(this.getHost().toURL(), this.toURL());
        }
        if(Preferences.instance().getBoolean("queue.download.wherefrom")) {
            // Set quarantine attributes
            local.setWhereFrom(this.toURL());
        }

        final StreamListener listener = new StreamListener() {
            int step = 0;

            public void bytesSent(long bytes) {
                l.bytesSent(bytes);
            }

            public void bytesReceived(long bytes) {
                if(-1 == bytes) {
                    // Remove custom icon if complete. The Finder will display the default
                    // icon for this filetype
                    local.setIcon(-1);
                }
                else {
                    l.bytesReceived(bytes);
                    if(updateIcon) {
                        int fraction = (int) (status().getCurrent() / attributes().getSize() * 10);
                        // An integer between 0 and 9
                        if(fraction > step) {
                            // Another 10 percent of the file has been transferred
                            local.setIcon(++step);
                        }
                    }
                }
            }
        };
        this.transfer(new ThrottledInputStream(in, throttle), out, listener, -1);
    }

    /**
     * Updates the current number of bytes transferred in the status reference.
     *
     * @param in       The stream to read from
     * @param out      The stream to write to
     * @param listener The stream listener to notify about bytes received and sent
     * @param limit
     * @throws IOException                 Write not completed due to a I/O problem
     * @throws ConnectionCanceledException When transfer is interrupted by user setting the
     *                                     status flag to cancel.
     */
    private void transfer(InputStream in, OutputStream out, StreamListener listener, final long limit) throws IOException {
        byte[] chunk = new byte[CHUNKSIZE];
        long bytesTransferred = 0;
        while(!status().isCanceled()) {
            int read = in.read(chunk, 0, CHUNKSIZE);
            listener.bytesReceived(read);
            if(-1 == read) {
                log.debug("End of file reached");
                // End of file
                status().setComplete(true);
                break;
            }
            out.write(chunk, 0, read);
            listener.bytesSent(read);
            status().addCurrent(read);
            bytesTransferred += read;
            if(limit == bytesTransferred) {
                log.debug("Limit reached reading from stream:" + limit);
                // Part reached
                if(0 == in.available()) {
                    // End of file
                    status().setComplete(true);
                }
                break;
            }
        }
        out.flush();
        if(status().isCanceled()) {
            throw new ConnectionCanceledException("Interrupted transfer");
        }
    }

    /**
     * Default implementation using a temporary file on localhost as an intermediary
     * with a download and upload transfer.
     *
     * @param copy Destination
     */
    @Override
    public void copy(final AbstractPath copy) {
        final Local local = LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"),
                copy.getName());
        TransferOptions options = new TransferOptions();
        options.closeSession = false;
        try {
            this.setLocal(local);
            DownloadTransfer download = new DownloadTransfer(this);
            download.addListener(new TransferAdapter() {
                @Override
                public void transferDidEnd() {
                    Path.this.getSession().message(Locale.localizedString("Download complete", "Growl"));
                }
            });
            download.start(new TransferPrompt() {
                public TransferAction prompt() {
                    return TransferAction.ACTION_OVERWRITE;
                }
            }, options);
            if(download.isComplete()) {
                ((Path) copy).setLocal(local);
                UploadTransfer upload = new UploadTransfer(((Path) copy));
                upload.addListener(new TransferAdapter() {
                    @Override
                    public void transferDidEnd() {
                        Path.this.getSession().message(Locale.localizedString("Upload complete", "Growl"));
                    }
                });
                upload.start(new TransferPrompt() {
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
                copy.getParent().invalidate();
            }
            else {
                this.error("Cannot copy {0}");
            }
        }
        finally {
            this.setLocal(null);
            local.delete();
        }
    }

    /**
     * @return true if the path exists (or is cached!)
     */
    @Override
    public boolean exists() {
        if(this.isRoot()) {
            return true;
        }
        return this.getParent().children().contains(this.getReference());
    }

    /**
     * @return The hashcode of #getAbsolute()
     * @see #getAbsolute()
     */
    @Override
    public int hashCode() {
        return this.getReference().hashCode();
    }

    /**
     * @param other
     * @return true if the other path has the same absolute path name
     */
    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Path) {
            return this.getReference().equals(((Path) other).<Object>getReference());
        }
        return false;
    }

    /**
     * @return The absolute path name
     */
    @Override
    public String toString() {
        return this.getAbsolute();
    }

    /**
     * URL encode a path
     *
     * @param p
     * @return
     * @see URLEncoder#encode(String, String)
     */
    public static String encode(final String p) {
        try {
            StringBuilder b = new StringBuilder();
            StringTokenizer t = new StringTokenizer(p, "/");
            if(!t.hasMoreTokens()) {
                return p;
            }
            while(t.hasMoreTokens()) {
                b.append(DELIMITER).append(URLEncoder.encode(t.nextToken(), "UTF-8"));
            }
            // Becuase URLEncoder uses <code>application/x-www-form-urlencoded</code> we have to replace these
            // for proper URI percented encoding.
            return b.toString().replaceAll("\\+", "%20");
        }
        catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * URL pointing to the resource using the protocol of the current session.
     *
     * @return Null if there is a encoding failure
     */
    @Override
    public String toURL() {
        // Do not use java.net.URL because it doesn't know about custom protocols!
        return this.getHost().toURL() + encode(this.getAbsolute());
    }

    /**
     * @return The URL accessible with HTTP using the
     *         hostname configuration from the bookmark
     */
    public String toHttpURL() {
        return this.toHttpURL(this.getHost().getWebURL());
    }

    /**
     * @param host The hostname to prepend to the path
     * @return The HTTP accessible URL of this path including the default path
     *         prepended from the bookmark
     */
    protected String toHttpURL(String host) {
        try {
            return new URI(host + this.getWebPath(this.getAbsolute())).normalize().toString();
        }
        catch(URISyntaxException e) {
            log.error("Failure parsing URI:" + e.getMessage());
        }
        return null;
    }

    /**
     * Remove the document root from the path
     *
     * @param path
     * @return Without any document root path component
     */
    private String getWebPath(String path) {
        String documentRoot = this.getHost().getDefaultPath();
        if(StringUtils.isNotBlank(documentRoot)) {
            if(path.contains(documentRoot)) {
                return encode(normalize(path.substring(path.indexOf(documentRoot) + documentRoot.length()), true));
            }
        }
        return encode(normalize(path, true));
    }

    /**
     * URL that requires authentication in the web browser.
     *
     * @return Empty.
     */
    public DescriptiveUrl toAuthenticatedUrl() {
        return new DescriptiveUrl(null, null);
    }

    /**
     * Includes both native protocol and HTTP URLs
     *
     * @return A list of URLs pointing to the resource.
     * @see #getHttpURLs()
     */
    public Set<DescriptiveUrl> getURLs() {
        Set<DescriptiveUrl> list = new LinkedHashSet<DescriptiveUrl>();
        list.add(new DescriptiveUrl(this.toURL(), MessageFormat.format(Locale.localizedString("{0} URL"),
                this.getHost().getProtocol().getScheme().toUpperCase())));
        list.addAll(this.getHttpURLs());
        return list;
    }

    /**
     * URLs to open in web browser.
     * Including URLs to CDN.
     *
     * @return All possible URLs to the same resource that can be opened in a web browser.
     */
    public Set<DescriptiveUrl> getHttpURLs() {
        Set<DescriptiveUrl> urls = new LinkedHashSet<DescriptiveUrl>();
        String http = this.toHttpURL();
        if(StringUtils.isNotBlank(http)) {
            urls.add(new DescriptiveUrl(http, MessageFormat.format(Locale.localizedString("{0} URL"), "HTTP")));
        }
        Session session = this.getSession();
        for(Distribution.Method method : session.cdn().getMethods()) {
            if(session.cdn().isConfigured(method)) {
                String container = this.getContainerName();
                if(null == container) {
                    continue;
                }
                Distribution distribution = session.cdn().read(session.cdn().getOrigin(method, container), method);
                if(distribution.isDeployed()) {
                    urls.addAll(distribution.getURLs(this));
                }
            }
        }
        return urls;
    }


    /**
     * Append an error message without any stacktrace information
     *
     * @param message Failure description
     */
    protected void error(String message) {
        this.error(message, null);
    }

    /**
     * @param message   Failure description
     * @param throwable The cause of the message
     * @see Session#error(Path, String, Throwable)
     */
    protected void error(String message, Throwable throwable) {
        this.getSession().error(this, message, throwable);
    }
}
