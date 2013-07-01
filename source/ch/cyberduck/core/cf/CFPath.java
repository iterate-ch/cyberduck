package ch.cyberduck.core.cf;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FilesExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rackspacecloud.client.cloudfiles.FilesContainerMetaData;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFPath extends CloudPath {
    private static final Logger log = Logger.getLogger(CFPath.class);

    private final CFSession session;

    public CFPath(CFSession s, Path parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    public CFPath(CFSession session, String path, int type) {
        super(session, path, type);
        this.session = session;
    }

    public CFPath(CFSession s, Path parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    public <T> CFPath(CFSession s, T dict) {
        super(s, dict);
        this.session = s;
    }

    @Override
    public CFSession getSession() {
        return session;
    }

    @Override
    public boolean exists() throws BackgroundException {
        if(this.isContainer()) {
            try {
                return session.getClient().containerExists(session.getRegion(this.getContainer()),
                        this.getName());
            }
            catch(FilesException e) {
                throw new FilesExceptionMappingService().map("Cannot read file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, this);
            }
        }
        return super.exists();
    }

    @Override
    public void readSize() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                    this.getName()));

            if(this.isContainer()) {
                attributes().setSize(
                        session.getClient().getContainerInfo(session.getRegion(this.getContainer()),
                                this.getContainer().getName()).getTotalSize()
                );
            }
            else if(this.attributes().isFile()) {
                attributes().setSize(
                        Long.valueOf(session.getClient().getObjectMetaData(session.getRegion(this.getContainer()),
                                this.getContainer().getName(), this.getKey()).getContentLength())
                );
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot read file attributes", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, this);
        }
    }

    @Override
    public void readChecksum() throws BackgroundException {
        if(this.attributes().isFile()) {
            try {
                session.message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        this.getName()));

                final String checksum = session.getClient().getObjectMetaData(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), this.getKey()).getETag();
                attributes().setChecksum(checksum);
                attributes().setETag(checksum);
            }
            catch(FilesException e) {
                throw new FilesExceptionMappingService().map("Cannot read file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, this);
            }
        }
    }

    @Override
    public void readTimestamp() throws BackgroundException {
        if(this.attributes().isFile()) {
            try {
                session.message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                        this.getName()));

                try {
                    attributes().setModificationDate(
                            ServiceUtils.parseRfc822Date(session.getClient().getObjectMetaData(
                                    session.getRegion(this.getContainer()), this.getContainer().getName(),
                                    this.getKey()).getLastModified()).getTime()
                    );
                }
                catch(ParseException e) {
                    log.error("Failure parsing timestamp", e);
                }
            }
            catch(FilesException e) {
                throw new FilesExceptionMappingService().map("Cannot read file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, this);
            }
        }
    }

    @Override
    public AttributedList<Path> list() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            if(this.isRoot()) {
                return new AttributedList<Path>(new SwiftContainerListService().list(session));
            }
            else {
                final AttributedList<Path> children = new AttributedList<Path>();
                final int limit = Preferences.instance().getInteger("cf.list.limit");
                String marker = null;
                List<FilesObject> list;
                do {
                    final Path container = this.getContainer();
                    list = session.getClient().listObjectsStartingWith(session.getRegion(container), container.getName(),
                            this.isContainer() ? StringUtils.EMPTY : this.getKey() + Path.DELIMITER, null, limit, marker, Path.DELIMITER);
                    for(FilesObject object : list) {
                        final Path file = new CFPath(session, this,
                                Path.getName(PathNormalizer.normalize(object.getName())),
                                "application/directory".equals(object.getMimeType()) ? DIRECTORY_TYPE : FILE_TYPE);
                        if(file.attributes().isFile()) {
                            file.attributes().setSize(object.getSize());
                            file.attributes().setChecksum(object.getMd5sum());
                            try {
                                final Date modified = DateParser.parse(object.getLastModified());
                                if(null != modified) {
                                    file.attributes().setModificationDate(modified.getTime());
                                }
                            }
                            catch(InvalidDateException e) {
                                log.warn("Not ISO 8601 format:" + e.getMessage());
                            }
                        }
                        if(file.attributes().isDirectory()) {
                            file.attributes().setPlaceholder(true);
                            if(children.contains(file.getReference())) {
                                continue;
                            }
                        }
                        file.attributes().setOwner(this.attributes().getOwner());
                        children.add(file);
                        marker = object.getName();
                    }
                }
                while(list.size() == limit);
                return children;
            }
        }
        catch(FilesException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new FilesExceptionMappingService().map("Listing directory failed", e, this);
        }
        catch(IOException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    @Override
    public InputStream read(final TransferStatus status) throws BackgroundException {
        try {
            if(status.isResume()) {
                return session.getClient().getObject(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), this.getKey(),
                        status.getCurrent(), status.getLength());
            }
            return session.getClient().getObject(session.getRegion(this.getContainer()),
                    this.getContainer().getName(), this.getKey());
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Download failed", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    @Override
    public void download(final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        OutputStream out = null;
        InputStream in = null;
        try {
            in = this.read(status);
            out = this.getLocal().getOutputStream(status.isResume());
            this.download(in, out, throttle, listener, status);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, this);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) throws BackgroundException {
        try {
            String md5sum = null;
            if(Preferences.instance().getBoolean("cf.upload.metadata.md5")) {
                session.message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        this.getName()));
                md5sum = this.getLocal().attributes().getChecksum();
            }
            MessageDigest digest = null;
            if(!Preferences.instance().getBoolean("cf.upload.metadata.md5")) {
                try {
                    digest = MessageDigest.getInstance("MD5");
                }
                catch(NoSuchAlgorithmException e) {
                    log.error("Failure loading MD5 digest", e);
                }
            }
            InputStream in = null;
            ResponseOutputStream<String> out = null;
            try {
                if(null == digest) {
                    log.warn("MD5 calculation disabled");
                    in = this.getLocal().getInputStream();
                }
                else {
                    in = new DigestInputStream(this.getLocal().getInputStream(), digest);
                }
                out = this.write(status, md5sum);
                this.upload(out, in, throttle, listener, status);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            if(null != digest && null != out) {
                session.message(MessageFormat.format(
                        Locale.localizedString("Compute MD5 hash of {0}", "Status"), this.getName()));
                // Obtain locally-calculated MD5 hash.
                String expectedETag = ServiceUtils.toHex(digest.digest());
                // Compare our locally-calculated hash with the ETag returned.
                final String result = out.getResponse();
                if(!expectedETag.equals(result)) {
                    throw new IOException("Mismatch between MD5 hash of uploaded data ("
                            + expectedETag + ") and ETag returned ("
                            + result + ") for object key: "
                            + this.getKey());
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("Object upload was automatically verified, the calculated MD5 hash " +
                                "value matched the ETag returned: " + this.getKey());
                    }
                }
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Upload failed", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, this);
        }
    }

    @Override
    public OutputStream write(final TransferStatus status) throws BackgroundException {
        return this.write(status, null);
    }

    private ResponseOutputStream<String> write(final TransferStatus status, final String md5sum)
            throws BackgroundException {
        final HashMap<String, String> metadata = new HashMap<String, String>();
        // Default metadata for new files
        for(String m : Preferences.instance().getList("cf.metadata.default")) {
            if(StringUtils.isBlank(m)) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String name = m.substring(0, split);
            if(StringUtils.isBlank(name)) {
                log.warn(String.format("Missing key in %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in %s", m));
                continue;
            }
            metadata.put(name, value);
        }
        // Submit store run to background thread
        final DelayedHttpEntityCallable<String> command = new DelayedHttpEntityCallable<String>() {
            /**
             *
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public String call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    return session.getClient().storeObject(
                            session.getRegion(getContainer()), getContainer().getName(),
                            CFPath.this.getKey(), entity,
                            metadata, md5sum);
                }
                catch(FilesException e) {
                    throw new FilesExceptionMappingService().map("Upload failed", e, CFPath.this);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload failed", e, CFPath.this);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength() - status.getCurrent();
            }
        };
        return this.write(command);
    }

    @Override
    public void mkdir() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));

            if(this.isContainer()) {
                // Create container at top level
                session.getClient().createContainer(session.getRegion(this.getContainer()), this.getName());
            }
            else {
                // Create virtual directory
                session.getClient().createPath(session.getRegion(this.getContainer()), this.getContainer().getName(), this.getKey());
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot create folder {0}", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, this);
        }
    }

    @Override
    public void delete(final LoginController prompt) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));
            if(attributes().isFile()) {
                session.getClient().deleteObject(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), this.getKey());
            }
            else if(attributes().isDirectory()) {
                for(Path i : this.list()) {
                    if(!session.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    i.delete(prompt);
                }
                if(this.isContainer()) {
                    session.getClient().deleteContainer(session.getRegion(this.getContainer()),
                            this.getContainer().getName());
                }
                else {
                    try {
                        session.getClient().deleteObject(session.getRegion(this.getContainer()),
                                this.getContainer().getName(), this.getKey());
                    }
                    catch(FilesNotFoundException e) {
                        // No real placeholder but just a delimiter returned in the object listing.
                        log.warn(e.getMessage());
                    }
                }
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot delete {0}", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, this);
        }
    }

    @Override
    public void readMetadata() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                    this.getName()));

            if(this.attributes().isFile()) {
                final FilesObjectMetaData meta
                        = session.getClient().getObjectMetaData(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), this.getKey());
                this.attributes().setMetadata(meta.getMetaData());
            }
            if(this.attributes().isVolume()) {
                final FilesContainerMetaData meta
                        = session.getClient().getContainerMetaData(session.getRegion(this.getContainer()),
                        this.getContainer().getName());
                this.attributes().setMetadata(meta.getMetaData());
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot read file attributes", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, this);
        }
    }

    @Override
    public void writeMetadata(final Map<String, String> meta) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                    this.getName()));

            if(this.attributes().isFile()) {
                session.getClient().updateObjectMetadata(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), this.getKey(), meta);
            }
            else if(this.attributes().isVolume()) {
                for(Map.Entry<String, String> entry : this.attributes().getMetadata().entrySet()) {
                    // Choose metadata values to remove
                    if(!meta.containsKey(entry.getKey())) {
                        log.debug(String.format("Remove metadata with key %s", entry.getKey()));
                        meta.put(entry.getKey(), StringUtils.EMPTY);
                    }
                }
                session.getClient().updateContainerMetadata(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), meta);
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot read file attributes", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, this);
        }
        finally {
            this.attributes().clear(false, false, false, true);
        }
    }

    @Override
    public void rename(final Path renamed) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            if(this.attributes().isFile()) {
                session.getClient().copyObject(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), this.getKey(),
                        renamed.getContainer().getName(), renamed.getKey());
                session.getClient().deleteObject(session.getRegion(this.getContainer()),
                        this.getContainer().getName(), this.getKey());
            }
            else if(this.attributes().isDirectory()) {
                for(Path i : this.list()) {
                    if(!session.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    i.rename(new CFPath(session, renamed, i.getName(), i.attributes().getType()));
                }
                try {
                    session.getClient().deleteObject(session.getRegion(this.getContainer()),
                            this.getContainer().getName(), this.getKey());
                }
                catch(FilesNotFoundException e) {
                    // No real placeholder but just a delimiter returned in the object listing.
                    log.warn(e.getMessage());
                }
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot rename {0}", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, this);
        }
    }

    @Override
    public void copy(final Path copy, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) throws BackgroundException {
        if(copy.getSession().equals(session)) {
            // Copy on same server
            try {
                session.message(MessageFormat.format(Locale.localizedString("Copying {0} to {1}", "Status"),
                        this.getName(), copy));

                if(this.attributes().isFile()) {
                    session.getClient().copyObject(session.getRegion(this.getContainer()),
                            this.getContainer().getName(), this.getKey(),
                            copy.getContainer().getName(), copy.getKey());
                    listener.bytesSent(this.attributes().getSize());
                    status.setComplete();
                }
            }
            catch(FilesException e) {
                throw new FilesExceptionMappingService().map("Cannot copy {0}", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot copy {0}", e, this);
            }
        }
        else {
            // Copy to different host
            super.copy(copy, throttle, listener, status);
        }
    }
}
