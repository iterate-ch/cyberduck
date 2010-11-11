package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.log4j.Logger;
import org.soyatec.windows.azure.error.StorageException;

import com.dropbox.client.ListEntryResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @version $Id: AzurePath.java 7308 2010-10-13 12:24:21Z dkocher $
 */
public class DropboxPath extends Path {
    private static Logger log = Logger.getLogger(DropboxPath.class);

    private static class Factory extends PathFactory<DropboxSession> {
        @Override
        protected Path create(DropboxSession session, String path, int type) {
            return new DropboxPath(session, path, type);
        }

        @Override
        protected Path create(DropboxSession session, String parent, String name, int type) {
            return new DropboxPath(session, parent, name, type);
        }

        @Override
        protected Path create(DropboxSession session, String parent, Local file) {
            return new DropboxPath(session, parent, file);
        }

        @Override
        protected <T> Path create(DropboxSession session, T dict) {
            return new DropboxPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
    }

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT
            = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

    private final DropboxSession session;

    protected DropboxPath(DropboxSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected DropboxPath(DropboxSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected DropboxPath(DropboxSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected <T> DropboxPath(DropboxSession s, T dict) {
        super(dict);
        this.session = s;
    }

    @Override
    public DropboxSession getSession() {
        return session;
    }

    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> children = new AttributedList<Path>();
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            for(ListEntryResponse entry : this.getSession().getClient().list(this.getAbsolute()).getContents()) {
                final Path file = PathFactory.createPath(this.getSession(), entry.getPath(),
                        entry.isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                file.setParent(this);
                file.attributes().setSize(entry.getLength());
                file.attributes().setChecksum(entry.getHash());
                try {
                    file.attributes().setModificationDate(SIMPLE_DATE_FORMAT.parse(entry.getModified()).getTime());
                }
                catch(ParseException e) {
                    log.warn("Failed parsing modification date:" + e.getMessage());
                }
                file.attributes().setRevision(entry.getRevision());
                file.attributes().setChecksum(entry.getHash());
                children.add(file);
            }
            this.getSession().setWorkdir(this);
        }
        catch(IOException e) {
            log.warn("Listing directory failed:" + e.getMessage());
            children.attributes().setReadable(false);
            if(this.cache().isEmpty()) {
                this.error(e.getMessage(), e);
            }
        }
        return children;
    }

    private ListEntryResponse readMetadata() throws IOException {
        return this.getSession().getClient().metadata(this.getAbsolute());
    }

    @Override
    public void readSize() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                    this.getName()));

            ListEntryResponse response = this.readMetadata();
            this.attributes().setSize(response.getLength());
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void readChecksum() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                    this.getName()));

            ListEntryResponse response = this.readMetadata();
            this.attributes().setChecksum(response.getHash());
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    protected void download(BandwidthThrottle throttle, StreamListener listener, boolean check) {
        if(attributes().isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    this.getSession().check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                        this.getName()));

                in = this.getSession().getClient().get(this.getAbsolute(), this.attributes().getChecksum());
                out = this.getLocal().getOutputStream(this.status().isResume());

                this.download(in, out, throttle, listener);
            }
            catch(StorageException e) {
                this.error("Download failed", e);
            }
            catch(IOException e) {
                this.error("Download failed", e);
            }
            finally {
                // Closing the input stream will trigger connection release
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    @Override
    protected void upload(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        if(attributes().isFile()) {
            try {
                if(check) {
                    this.getSession().check();
                }

                final Status status = this.status();
                status.setResume(false);

                final Local local = this.getLocal();
                final InputStream in = local.getInputStream();
                try {
                    this.getSession().getClient().put(this.getParent().getAbsolute(), new InputStreamBody(in, local.getMimeType(),
                            this.getName()) {
                        @Override
                        public void writeTo(OutputStream out) throws IOException {
                            upload(out, in, throttle, listener);
                        }

                        @Override
                        public long getContentLength() {
                            return local.attributes().getSize();
                        }
                    });
                }
                catch(IOException e) {
                    this.status().setComplete(false);
                    throw e;
                }
                finally {
                    IOUtils.closeQuietly(in);
                }
            }
            catch(IOException e) {
                this.error("Upload failed", e);
            }
        }
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        this.getName()));

                this.getSession().getClient().create(this.getAbsolute());

                this.cache().put(this.getReference(), AttributedList.<Path>emptyList());
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot create folder", e);
            }
        }
    }

    @Override
    public void delete() {
        try {
            this.getSession().check();

            this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));

            this.getSession().getClient().delete(this.getAbsolute());
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            if(this.attributes().isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes().isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    @Override
    public void rename(AbstractPath renamed) {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            this.getSession().setWorkdir(this.getParent());
            this.getSession().getClient().move(this.getAbsolute(), renamed.getAbsolute());
            // The directory listing is no more current
            renamed.getParent().invalidate();
            this.getParent().invalidate();
        }
        catch(IOException e) {
            if(attributes().isFile()) {
                this.error("Cannot rename file", e);
            }
            if(attributes().isDirectory()) {
                this.error("Cannot rename folder", e);
            }
        }
    }

    @Override
    public void copy(AbstractPath copy) {
        if(((Path) copy).getSession().equals(this.getSession())) {
            // Copy on same server
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Copying {0} to {1}", "Status"),
                        this.getName(), copy));

                this.getSession().getClient().copy(this.getAbsolute(), copy.getAbsolute());
            }
            catch(IOException e) {
                this.error(this.attributes().isFile() ? "Cannot copy file" : "Cannot copy folder", e);
            }
        }
        else {
            // Copy to different host
            super.copy(copy);
        }
    }

    @Override
    public void writeTimestamp(long created, long modified, long accessed) {
        ;
    }

    @Override
    public void readTimestamp() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                    this.getName()));

            ListEntryResponse response = this.readMetadata();
            try {
                this.attributes().setModificationDate(SIMPLE_DATE_FORMAT.parse(response.getModified()).getTime());
            }
            catch(ParseException e) {
                log.warn("Failed parsing modification date:" + e.getMessage());
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void writeUnixPermission(Permission perm, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readUnixPermission() {
        throw new UnsupportedOperationException();
    }
}
