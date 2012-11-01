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
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadTransfer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.text.Normalizer;

/**
 * @version $Id$
 */
public abstract class Path extends AbstractPath implements Serializable {
    private static final Logger log = Logger.getLogger(Path.class);

    /**
     * To lookup a copy of the path in the cache.
     */
    private PathReference reference;

    /**
     * The absolute remote path
     */
    private String path;

    /**
     * Reference to the parent created lazily if needed
     */
    private Path parent;

    /**
     * The local path to be used if file is copied
     */
    private Local local;

    /**
     * Attributes denoting this path
     */
    private PathAttributes attributes;

    protected <T> Path(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        String pathObj = dict.stringForKey("Remote");
        if(pathObj != null) {
            this.path = pathObj;
        }
        String localObj = dict.stringForKey("Local");
        if(localObj != null) {
            this.local = LocalFactory.createLocal(localObj);
        }
        String symlinkObj = dict.stringForKey("Symlink");
        if(symlinkObj != null) {
            this.symlink = symlinkObj;
        }
        final Object attributesObj = dict.objectForKey("Attributes");
        if(attributesObj != null) {
            this.attributes = new PathAttributes(attributesObj);
        }
        else {
            this.attributes = new PathAttributes(Path.FILE_TYPE);
        }
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        return this.getAsDictionary(dict);
    }

    protected <S> S getAsDictionary(Serializer dict) {
        dict.setStringForKey(this.getAbsolute(), "Remote");
        if(local != null) {
            dict.setStringForKey(local.toString(), "Local");
        }
        if(StringUtils.isNotBlank(symlink)) {
            dict.setStringForKey(symlink, "Symlink");
        }
        dict.setObjectForKey(attributes, "Attributes");
        return dict.getSerialized();
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param parent the absolute directory
     * @param name   the file relative to param path
     * @param type   File type
     */
    public Path(final String parent, final String name, final int type) {
        this.setPath(parent, name);
        this.attributes = new PathAttributes(type);
    }

    /**
     * A remote path where nothing is known about a local equivalent.
     *
     * @param path The absolute path of the remote file
     * @param type File type
     */
    public Path(final String path, final int type) {
        this.setPath(path);
        this.attributes = new PathAttributes(type);
    }

    /**
     * Create a new path where you know the local file already exists
     * and the remote equivalent might be created later.
     * The remote filename will be extracted from the local file.
     *
     * @param parent The absolute path to the parent directory on the remote host
     * @param local  The associated local file
     */
    public Path(final String parent, final Local local) {
        this.setPath(parent, local);
        this.attributes = new PathAttributes(local.attributes().isDirectory() ? DIRECTORY_TYPE : FILE_TYPE);
    }

    /**
     * @param parent The parent directory
     * @param file   The local file corresponding with this remote path
     */
    public void setPath(final String parent, final Local file) {
        this.setPath(parent, file.getName());
        this.setLocal(file);
    }

    /**
     * @param parent The parent directory
     * @param name   The filename
     */
    public void setPath(final Path parent, final String name) {
        super.setPath(parent.getAbsolute(), name);
        this.setParent(parent);
    }

    /**
     * Normalizes the name before updatings this path. Resets its parent directory
     *
     * @param name Must be an absolute pathname
     */
    @Override
    public void setPath(final String name) {
        this.path = Path.normalize(name);
        this.parent = null;
        this.reference = null;
    }

    /**
     * Set reference to parent path.
     *
     * @param parent The parent directory with attributes already populated.
     */
    public void setParent(final Path parent) {
        if(this.isChild(parent)) {
            this.parent = parent;
        }
        else {
            log.warn(String.format("Attempt to set invalid parent directory %s", parent));
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
                //
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
     * @return True if this path denotes a container
     */
    public boolean isContainer() {
        return this.equals(this.getContainer());
    }

    /**
     * @return Default path in bookmark or root delimiter
     */
    public String getContainerName() {
        if(StringUtils.isNotBlank(this.getHost().getDefaultPath())) {
            return Path.normalize(this.getHost().getDefaultPath(), true);
        }
        return String.valueOf(DELIMITER);
    }

    /**
     * @return Default path or root with volume attributes set
     */
    public Path getContainer() {
        return PathFactory.createPath(this.getSession(), this.getContainerName(),
                VOLUME_TYPE | DIRECTORY_TYPE);
    }

    /**
     * Create a parent path with default attributes if it is not referenced yet.
     *
     * @return The parent directory
     */
    @Override
    public Path getParent() {
        if(null == parent) {
            if(this.isRoot()) {
                return this;
            }
            final String name = getParent(this.getAbsolute(), this.getPathDelimiter());
            if(String.valueOf(DELIMITER).equals(name)) {
                parent = PathFactory.createPath(this.getSession(), String.valueOf(DELIMITER),
                        VOLUME_TYPE | DIRECTORY_TYPE);
            }
            else {
                parent = PathFactory.createPath(this.getSession(), name,
                        DIRECTORY_TYPE);
            }
        }
        return parent;
    }

    /**
     * Default implementation returning a reference to self. You can override this
     * if you need a different strategy to compare hashcode and equality for caching
     * in a model.
     *
     * @return Reference to the path to be used in table models an file listing
     *         cache.
     * @see ch.cyberduck.core.Cache#lookup(PathReference)
     */
    @Override
    public PathReference getReference() {
        if(null == reference) {
            reference = PathReferenceFactory.createPathReference(this);
        }
        return reference;
    }

    public void setReference(final PathReference<Path> reference) {
        this.reference = reference;
    }

    @Override
    public PathAttributes attributes() {
        return attributes;
    }

    public void setAttributes(final PathAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     * @return Null if the connection has been closed
     */
    public Host getHost() {
        return this.getSession().getHost();
    }

    @Override
    public AttributedList<Path> children() {
        return this.children(null);
    }

    @Override
    public AttributedList<Path> children(final PathFilter<? extends AbstractPath> filter) {
        return this.children(null, filter);
    }

    @Override
    public AttributedList<Path> children(final Comparator<? extends AbstractPath> comparator,
                                         final PathFilter<? extends AbstractPath> filter) {
        final Cache cache = this.getSession().cache();
        if(!cache.isCached(this.getReference())) {
            cache.put(this.getReference(), this.list());
        }
        return cache.get(this.getReference()).filter(comparator, filter);
    }

    @Override
    public AttributedList<Path> list() {
        return this.list(new AttributedList<Path>() {
            @Override
            public boolean add(Path path) {
                if(!path.isChild(Path.this)) {
                    log.warn(String.format("Skip adding child %s to directory listing", path));
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

    public void writeOwner(String owner) {
        throw new UnsupportedOperationException();
    }

    public void writeGroup(String group) {
        throw new UnsupportedOperationException();
    }

    /**
     * Default implementation updating timestamp from directory listing.
     * <p/>
     * No checksum calculation by default. Might be supported by specific
     * provider implementation.
     */
    public void readChecksum() {
        //
    }

    /**
     * Default implementation updating size from directory listing
     */
    public void readSize() {
        //
    }

    @Override
    public void writeTimestamp(long created, long modified, long accessed) {
        throw new UnsupportedOperationException();
    }

    /**
     * Default implementation updating timestamp from directory listing.
     *
     * @see ch.cyberduck.core.Attributes#getModificationDate()
     */
    public void readTimestamp() {
        //
    }

    /**
     * Default implementation updating permissions from directory listing.
     *
     * @see Attributes#getPermission()
     * @see Session#isUnixPermissionsSupported()
     */
    public void readUnixPermission() {
        //
    }

    @Override
    public void writeUnixPermission(Permission permission) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param acl       The permissions to apply
     * @param recursive Include subdirectories and files
     */
    public void writeAcl(Acl acl, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    /**
     * Read the ACL of the bucket or object
     */
    public void readAcl() {
        //
    }

    /**
     * Read modifiable HTTP header metatdata key and values
     */
    public void readMetadata() {
        //
    }

    /**
     * @param meta Modifiable HTTP header metatdata key and values
     */
    public void writeMetadata(Map<String, String> meta) {
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
    public void setLocal(final Local file) {
        this.local = file;
    }

    /**
     * @return The local alias of this path
     */
    public Local getLocal() {
        return local;
    }

    /**
     * An absolute reference here the symbolic link is pointing to
     */
    protected String symlink;

    public void setSymlinkTarget(final String name) {
        this.symlink = name;
    }

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link
     * @see ch.cyberduck.core.PathAttributes#isSymbolicLink
     */
    @Override
    public AbstractPath getSymlinkTarget() {
        final PathAttributes attributes = this.attributes();
        if(attributes.isSymbolicLink()) {
            // Symbolic link target may be an absolute or relative path
            if(symlink.startsWith(String.valueOf(DELIMITER))) {
                return PathFactory.createPath(this.getSession(), symlink,
                        attributes.isDirectory() ? DIRECTORY_TYPE : FILE_TYPE);
            }
            else {
                return PathFactory.createPath(this.getSession(), this.getParent().getAbsolute(), symlink,
                        attributes.isDirectory() ? DIRECTORY_TYPE : FILE_TYPE);
            }
        }
        return null;
    }

    /**
     * Create a symbolic link on the server. Creates a link "src" that points
     * to "target".
     *
     * @param target Target file of symbolic link
     */
    @Override
    public void symlink(String target) {
        log.warn(String.format("Touching file instead of creating symbolic link for %s", this));
        this.touch();
    }

    /**
     * @return The session this path uses to send commands
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
                this.getLocal().setPath(Preferences.instance().getProperty("tmp.dir"), proposal);
            }
            this.getLocal().touch();
            TransferOptions options = new TransferOptions();
            options.closeSession = false;
            try {
                UploadTransfer upload = new UploadTransfer(this);
                upload.start(new TransferPrompt() {
                    @Override
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
            }
            finally {
                this.getLocal().delete();
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
     * @param status Transfer status
     * @return Stream to read from to download file
     * @throws IOException Read not completed due to a I/O problem
     */
    public abstract InputStream read(final TransferStatus status) throws IOException;

    /**
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     * @param status   Transfer status
     */
    public abstract void download(BandwidthThrottle throttle, StreamListener listener,
                                  TransferStatus status);

    /**
     * @param status Transfer status
     * @return Stream to write to for upload
     * @throws IOException Open file for writing fails
     */
    public abstract OutputStream write(TransferStatus status) throws IOException;

    /**
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     * @param status   Transfer status
     */
    public abstract void upload(BandwidthThrottle throttle, StreamListener listener,
                                TransferStatus status);

    /**
     * @param out      Remote stream
     * @param in       Local stream
     * @param throttle The bandwidth limit
     * @param l        Listener for bytes sent
     * @param status   Transfer status
     * @throws IOException Write not completed due to a I/O problem
     */
    protected void upload(final OutputStream out, final InputStream in,
                          final BandwidthThrottle throttle,
                          final StreamListener l, final TransferStatus status) throws IOException {
        this.upload(out, in, throttle, l, status.getCurrent(), -1, status);
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
     * @param offset   Start reading at offset in file
     * @param limit    Transfer only up to this length
     * @param status   Transfer status
     * @throws IOResumeException           If the input stream fails to skip the appropriate
     *                                     number of bytes
     * @throws IOException                 Write not completed due to a I/O problem
     * @throws ConnectionCanceledException When transfer is interrupted by user setting the
     *                                     status flag to cancel.
     */
    protected void upload(final OutputStream out, final InputStream in, final BandwidthThrottle throttle,
                          final StreamListener l, long offset, final long limit, final TransferStatus status) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("upload(" + out.toString() + ", " + in.toString());
        }
        this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                this.getName()));

        if(offset > 0) {
            long skipped = in.skip(offset);
            if(log.isInfoEnabled()) {
                log.info(String.format("Skipping %d bytes", skipped));
            }
            if(skipped < status.getCurrent()) {
                throw new IOResumeException(String.format("Skipped %d bytes instead of %d",
                        skipped, status.getCurrent()));
            }
        }
        this.transfer(in, new ThrottledOutputStream(out, throttle), l, limit, status);
    }

    /**
     * Will copy from in to out. Does not attempt to skip any bytes from the streams.
     *
     * @param in       The stream to read from
     * @param out      The stream to write to
     * @param throttle The bandwidth limit
     * @param l        The stream listener to notify about bytes received and sent
     * @param status   Transfer status
     * @throws IOException                 Write not completed due to a I/O problem
     * @throws ConnectionCanceledException When transfer is interrupted by user setting the
     *                                     status flag to cancel.
     */
    protected void download(final InputStream in, final OutputStream out, final BandwidthThrottle throttle,
                            final StreamListener l, final TransferStatus status) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("download(" + in.toString() + ", " + out.toString());
        }
        this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                this.getName()));

        this.transfer(new ThrottledInputStream(in, throttle), out, l, -1, status);
    }

    /**
     * Updates the current number of bytes transferred in the status reference.
     *
     * @param in       The stream to read from
     * @param out      The stream to write to
     * @param listener The stream listener to notify about bytes received and sent
     * @param limit    Transfer only up to this length
     * @param status   Transfer status
     * @throws IOException                 Write not completed due to a I/O problem
     * @throws ConnectionCanceledException When transfer is interrupted by user setting the
     *                                     status flag to cancel.
     */
    protected void transfer(final InputStream in, final OutputStream out,
                            final StreamListener listener, final long limit,
                            final TransferStatus status) throws IOException {
        final BufferedInputStream bi = new BufferedInputStream(in);
        final BufferedOutputStream bo = new BufferedOutputStream(out);
        try {
            final int chunksize = Preferences.instance().getInteger("connection.chunksize");
            final byte[] chunk = new byte[chunksize];
            long bytesTransferred = 0;
            while(!status.isCanceled()) {
                final int read = bi.read(chunk, 0, chunksize);
                if(-1 == read) {
                    log.debug("End of file reached");
                    // End of file
                    status.setComplete();
                    break;
                }
                else {
                    status.addCurrent(read);
                    listener.bytesReceived(read);
                    bo.write(chunk, 0, read);
                    listener.bytesSent(read);
                    bytesTransferred += read;
                    if(limit == bytesTransferred) {
                        log.debug("Limit reached reading from stream:" + limit);
                        // Part reached
                        if(0 == bi.available()) {
                            // End of file
                            status.setComplete();
                        }
                        break;
                    }
                }
            }
        }
        finally {
            bo.flush();
        }
        if(status.isCanceled()) {
            throw new ConnectionCanceledException("Interrupted transfer");
        }
    }

    public void copy(AbstractPath copy, final TransferStatus status) {
        this.copy(copy, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new AbstractStreamListener(), status);
    }

    /**
     * Default implementation using a temporary file on localhost as an intermediary
     * with a download and upload transfer.
     *
     * @param copy     Destination
     * @param throttle The bandwidth limit
     * @param listener Callback
     * @param status   Transfer status
     */
    public void copy(final AbstractPath copy, final BandwidthThrottle throttle,
                     final StreamListener listener, final TransferStatus status) {
        InputStream in = null;
        OutputStream out = null;
        try {
            this.getSession().message(MessageFormat.format(Locale.localizedString("Copying {0}", "Status"),
                    this.getName()));
            if(this.attributes().isFile()) {
                this.transfer(in = this.read(status), out = ((Path) copy).write(status), listener, -1, status);
            }
        }
        catch(IOException e) {
            this.error("Cannot copy {0}", e);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Check for file existence. The default implementation does a directory listing of the parent folder.
     *
     * @return True if the path exists or is cached.
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
     * @param other Path to compare with
     * @return true if the other path has the same absolute path name
     */
    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Path) {
            return this.getReference().equals(((Path) other).getReference());
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
     * URL pointing to the resource using the protocol of the current session.
     *
     * @return Null if there is a encoding failure
     */
    @Override
    public String toURL() {
        return this.toURL(true);
    }

    /**
     * @param credentials Include username
     * @return Null if there is a encoding failure
     */
    public String toURL(final boolean credentials) {
        return String.format("%s%s", this.getHost().toURL(credentials), URIEncoder.encode(this.getAbsolute()));
    }

    /**
     * @return The URL accessible with HTTP using the
     *         hostname configuration from the bookmark
     */
    public String toHttpURL() {
        return this.toHttpURL(this.getHost().getWebURL());
    }

    /**
     * @param uri The scheme and hostname to prepend to the path
     * @return The HTTP accessible URL of this path including the default path
     *         prepended from the bookmark
     */
    protected String toHttpURL(final String uri) {
        try {
            return new URI(uri + this.getWebPath(this.getAbsolute())).normalize().toString();
        }
        catch(URISyntaxException e) {
            log.error("Failure parsing URI:" + e.getMessage());
        }
        return null;
    }

    /**
     * Remove the document root from the path
     *
     * @param path Absolute path
     * @return Without any document root path component
     */
    private String getWebPath(String path) {
        String documentRoot = this.getHost().getDefaultPath();
        if(StringUtils.isNotBlank(documentRoot)) {
            if(path.contains(documentRoot)) {
                return URIEncoder.encode(normalize(path.substring(path.indexOf(documentRoot) + documentRoot.length()), true));
            }
        }
        return URIEncoder.encode(normalize(path, true));
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
                this.getHost().getProtocol().getScheme().toString().toUpperCase())));
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
        // Include all CDN URLs
        Session session = this.getSession();
        if(session.isCDNSupported()) {
            for(Distribution.Method method : session.cdn().getMethods(this.getContainerName())) {
                if(session.cdn().isCached(method)) {
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
        }
        // Include default Web URL
        String http = this.toHttpURL();
        if(StringUtils.isNotBlank(http)) {
            urls.add(new DescriptiveUrl(http, MessageFormat.format(Locale.localizedString("{0} URL"), "HTTP")));
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
