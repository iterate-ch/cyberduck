package ch.cyberduck.core.ftp;

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
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.date.MDTMSecondsDateFormatter;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FTPExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPCommand;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class FTPPath extends Path {
    private static final Logger log = Logger.getLogger(FTPPath.class);

    private final FTPSession session;

    public FTPPath(FTPSession s, Path parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    public FTPPath(FTPSession s, String path, int type) {
        super(s, path, type);
        this.session = s;
    }

    public FTPPath(FTPSession s, Path parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    public <T> FTPPath(FTPSession s, T dict) {
        super(s, dict);
        this.session = s;
    }

    @Override
    public FTPSession getSession() {
        return session;
    }

    protected abstract static class DataConnectionAction<T> {
        public abstract T run() throws IOException;
    }

    /**
     * @param action Action that needs to open a data connection
     * @return True if action was successful
     */
    protected <T> T data(final DataConnectionAction<T> action) throws BackgroundException {
        try {
            // Make sure to always configure data mode because connect event sets defaults.
            if(session.getConnectMode().equals(FTPConnectMode.PASV)) {
                session.getClient().enterLocalPassiveMode();
            }
            else if(session.getConnectMode().equals(FTPConnectMode.PORT)) {
                session.getClient().enterLocalActiveMode();
            }
            return action.run();
        }
        catch(SocketTimeoutException failure) {
            log.warn(String.format("Timeout opening data socket %s", failure.getMessage()));
            // Fallback handling
            if(Preferences.instance().getBoolean("ftp.connectmode.fallback")) {
                session.interrupt();
                session.open();
                session.login(new DisabledPasswordStore(), new DisabledLoginController());
                try {
                    return this.fallback(action);
                }
                catch(IOException e) {
                    session.interrupt();
                    log.warn("Connect mode fallback failed:" + e.getMessage());
                    // Throw original error message
                }
            }
            throw new DefaultIOExceptionMappingService().map(failure, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    /**
     * @param action Action that needs to open a data connection
     * @return True if action was successful
     */
    protected <T> T fallback(final DataConnectionAction<T> action) throws ConnectionCanceledException, IOException {
        // Fallback to other connect mode
        if(session.getClient().getDataConnectionMode() == FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to active data connection");
            session.getClient().enterLocalActiveMode();
        }
        else if(session.getClient().getDataConnectionMode() == FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to passive data connection");
            session.getClient().enterLocalPassiveMode();
        }
        return action.run();
    }

    @Override
    public AttributedList<Path> list() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            final AttributedList<Path> children = new AttributedList<Path>();

            // Cached file parser determined from SYST response with the timezone set from the bookmark
            final FTPFileEntryParser parser = session.getParser();
            boolean success = false;
            try {
                if(session.isStatListSupportedEnabled()) {
                    int response = session.getClient().stat(this.getAbsolute());
                    if(FTPReply.isPositiveCompletion(response)) {
                        final String[] reply = session.getClient().getReplyStrings();
                        final List<String> result = new ArrayList<String>(reply.length);
                        for(final String line : reply) {
                            //Some servers include the status code for every line.
                            if(line.startsWith(String.valueOf(response))) {
                                try {
                                    result.add(line.substring(line.indexOf(response) + line.length() + 1).trim());
                                }
                                catch(IndexOutOfBoundsException e) {
                                    log.error(String.format("Failed parsing line %s", line), e);
                                }
                            }
                            else {
                                result.add(StringUtils.stripStart(line, null));
                            }
                        }
                        success = new FTPListResponseReader().read(children, session, this, parser, result);
                    }
                    else {
                        session.setStatListSupportedEnabled(false);
                    }
                }
            }
            catch(IOException e) {
                log.warn("Command STAT failed with I/O error:" + e.getMessage());
                session.interrupt();
                session.open();
                session.login(new DisabledPasswordStore(), new DisabledLoginController());
            }
            if(!success || children.isEmpty()) {
                success = this.data(new DataConnectionAction<Boolean>() {
                    @Override
                    public Boolean run() throws IOException {
                        if(!session.getClient().changeWorkingDirectory(getAbsolute())) {
                            throw new FTPException(session.getClient().getReplyCode(),
                                    session.getClient().getReplyString());
                        }
                        if(!session.getClient().setFileType(FTPClient.ASCII_FILE_TYPE)) {
                            // Set transfer type for traditional data socket file listings. The data transfer is over the
                            // data connection in type ASCII or type EBCDIC.
                            throw new FTPException(session.getClient().getReplyCode(),
                                    session.getClient().getReplyString());
                        }
                        boolean success = false;
                        // STAT listing failed or empty
                        if(session.isMlsdListSupportedEnabled()
                                // Note that there is no distinct FEAT output for MLSD.
                                // The presence of the MLST feature indicates that both MLST and MLSD are supported.
                                && session.getClient().isFeatureSupported(FTPCommand.MLST)) {
                            success = new FTPMlsdListResponseReader().read(children, session, FTPPath.this,
                                    null, session.getClient().list(FTPCommand.MLSD));
                            if(!success) {
                                session.setMlsdListSupportedEnabled(false);
                            }
                        }
                        if(!success) {
                            // MLSD listing failed or not enabled
                            if(session.isExtendedListEnabled()) {
                                try {
                                    success = new FTPListResponseReader().read(children, session, FTPPath.this,
                                            parser, session.getClient().list(FTPCommand.LIST, "-a"));
                                }
                                catch(FTPException e) {
                                    session.setExtendedListEnabled(false);
                                }
                            }
                            if(!success) {
                                // LIST -a listing failed or not enabled
                                success = new FTPListResponseReader().read(children, session, FTPPath.this,
                                        parser, session.getClient().list(FTPCommand.LIST));
                            }
                        }
                        return success;
                    }
                });
            }
            for(Path child : children) {
                if(child.attributes().isSymbolicLink()) {
                    if(session.getClient().changeWorkingDirectory(child.getAbsolute())) {
                        child.attributes().setType(SYMBOLIC_LINK_TYPE | DIRECTORY_TYPE);
                    }
                    else {
                        // Try if CWD to symbolic link target succeeds
                        if(session.getClient().changeWorkingDirectory(child.getSymlinkTarget().getAbsolute())) {
                            // Workdir change succeeded
                            child.attributes().setType(SYMBOLIC_LINK_TYPE | DIRECTORY_TYPE);
                        }
                        else {
                            child.attributes().setType(SYMBOLIC_LINK_TYPE | FILE_TYPE);
                        }
                    }
                }
            }
            if(!success) {
                // LIST listing failed
                log.error("No compatible file listing method found");
            }
            return children;
        }
        catch(IOException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new FTPExceptionMappingService().map("Listing directory failed", e, this);
        }
    }

    @Override
    public void mkdir() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));

            if(!session.getClient().makeDirectory(this.getAbsolute())) {
                throw new FTPException(session.getClient().getReplyCode(),
                        session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot create folder {0}", e, this);
        }
    }

    @Override
    public void rename(final Path renamed) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            if(!session.getClient().rename(this.getAbsolute(), renamed.getAbsolute())) {
                throw new FTPException(session.getClient().getReplyCode(),
                        session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot rename {0}", e, this);
        }
    }

    @Override
    public void delete(final LoginController prompt) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));

            if(attributes().isFile() || attributes().isSymbolicLink()) {
                if(!session.getClient().deleteFile(this.getAbsolute())) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
            else if(attributes().isDirectory()) {
                for(Path child : this.list()) {
                    if(!session.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    child.delete(prompt);
                }
                session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                if(!session.getClient().removeDirectory(this.getAbsolute())) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot delete {0}", e, this);

        }
    }

    @Override
    public void writeUnixOwner(String owner) throws BackgroundException {
        String command = "chown";
        try {
            session.message(MessageFormat.format(Locale.localizedString("Changing owner of {0} to {1}", "Status"),
                    this.getName(), owner));

            if(attributes().isFile() && !attributes().isSymbolicLink()) {
                if(!session.getClient().sendSiteCommand(command + " " + owner + " " + this.getAbsolute())) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
            else if(attributes().isDirectory()) {
                if(!session.getClient().sendSiteCommand(command + " " + owner + " " + this.getAbsolute())) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change owner", e, this);
        }
    }

    @Override
    public void writeUnixGroup(String group) throws BackgroundException {
        String command = "chgrp";
        try {
            session.message(MessageFormat.format(Locale.localizedString("Changing group of {0} to {1}", "Status"),
                    this.getName(), group));

            if(attributes().isFile() && !attributes().isSymbolicLink()) {
                if(!session.getClient().sendSiteCommand(command + " " + group + " " + this.getAbsolute())) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
            else if(attributes().isDirectory()) {
                if(!session.getClient().sendSiteCommand(command + " " + group + " " + this.getAbsolute())) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change group", e, this);
        }
    }

    @Override
    public void writeUnixPermission(Permission permission) throws BackgroundException {
        try {
            this.writeUnixPermissionImpl(permission);
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change permissions", e, this);
        }
    }

    private boolean chmodSupported = true;

    private void writeUnixPermissionImpl(Permission permission) throws IOException {
        if(chmodSupported) {
            session.message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                    this.getName(), permission.getOctalString()));
            if(attributes().isFile() && !attributes().isSymbolicLink()) {
                if(session.getClient().sendSiteCommand("CHMOD " + permission.getOctalString() + " " + this.getAbsolute())) {
                    this.attributes().setPermission(permission);
                }
                else {
                    chmodSupported = false;
                }
            }
            else if(attributes().isDirectory()) {
                if(session.getClient().sendSiteCommand("CHMOD " + permission.getOctalString() + " " + this.getAbsolute())) {
                    this.attributes().setPermission(permission);
                }
                else {
                    chmodSupported = false;
                }
            }
        }
    }

    @Override
    public void writeTimestamp(long created, long modified, long accessed) throws BackgroundException {
        try {
            this.writeModificationDateImpl(created, modified);
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change timestamp", e, this);
        }
    }

    private void writeModificationDateImpl(long created, long modified) throws IOException {
        session.message(MessageFormat.format(Locale.localizedString("Changing timestamp of {0} to {1}", "Status"),
                this.getName(), UserDateFormatterFactory.get().getShortFormat(modified)));

        final MDTMSecondsDateFormatter formatter = new MDTMSecondsDateFormatter();
        if(session.getClient().isFeatureSupported(FTPCommand.MFMT)) {
            if(session.getClient().setModificationTime(this.getAbsolute(),
                    formatter.format(modified, TimeZone.getTimeZone("UTC")))) {
                this.attributes().setModificationDate(modified);
            }
        }
        else {
            if(session.isUtimeSupported()) {
                // The utime() function sets the access and modification times of the named
                // file from the structures in the argument array timep.
                // The access time is set to the value of the first element,
                // and the modification time is set to the value of the second element
                // Accessed date, modified date, created date
                if(session.getClient().sendSiteCommand("UTIME " + this.getAbsolute()
                        + " " + formatter.format(new Date(modified), TimeZone.getTimeZone("UTC"))
                        + " " + formatter.format(new Date(modified), TimeZone.getTimeZone("UTC"))
                        + " " + formatter.format(new Date(created), TimeZone.getTimeZone("UTC"))
                        + " UTC")) {
                    this.attributes().setModificationDate(modified);
                    this.attributes().setCreationDate(created);
                }
                else {
                    session.setUtimeSupported(false);
                    log.warn("UTIME not supported");
                }
            }

        }
    }

    @Override
    public void download(final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = read(status);
                out = getLocal().getOutputStream(status.isResume());
                this.download(in, out, throttle, listener, status);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Download failed", e, this);
        }
    }

    @Override
    public InputStream read(final TransferStatus status) throws BackgroundException {
        try {
            if(!session.getClient().setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new FTPException(session.getClient().getReplyCode(),
                        session.getClient().getReplyString());
            }
            if(status.isResume()) {
                // Where a server process supports RESTart in STREAM mode
                if(!session.getClient().isFeatureSupported("REST STREAM")) {
                    status.setResume(false);
                }
                else {
                    session.getClient().setRestartOffset(status.getCurrent());
                }
            }
            final InputStream in = this.data(new DataConnectionAction<InputStream>() {
                @Override
                public InputStream run() throws IOException {
                    return session.getClient().retrieveFileStream(getAbsolute());
                }
            });
            return new CountingInputStream(in) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    }
                    finally {
                        if(this.getByteCount() == status.getLength()) {
                            // Read 226 status
                            if(!session.getClient().completePendingCommand()) {
                                throw new FTPException(session.getClient().getReplyCode(),
                                        session.getClient().getReplyString());
                            }
                        }
                        else {
                            // Interrupted transfer
                            if(!session.getClient().abort()) {
                                log.error("Error closing data socket:" + session.getClient().getReplyString());
                            }
                        }
                    }
                }
            };
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Download failed", e, this);
        }
    }

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = getLocal().getInputStream();
                out = write(status);
                this.upload(out, in, throttle, listener, status);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Upload failed", e, this);
        }
    }

    @Override
    public OutputStream write(final TransferStatus status) throws BackgroundException {
        try {
            if(!session.getClient().setFileType(FTPClient.BINARY_FILE_TYPE)) {
                throw new FTPException(session.getClient().getReplyCode(),
                        session.getClient().getReplyString());
            }
            final OutputStream out = this.data(new DataConnectionAction<OutputStream>() {
                @Override
                public OutputStream run() throws IOException {
                    if(status.isResume()) {
                        return session.getClient().appendFileStream(getAbsolute());
                    }
                    else {
                        return session.getClient().storeFileStream(getAbsolute());
                    }
                }
            });
            return new CountingOutputStream(out) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    }
                    finally {
                        if(this.getByteCount() == status.getLength()) {
                            // Read 226 status
                            if(!session.getClient().completePendingCommand()) {
                                throw new FTPException(session.getClient().getReplyCode(),
                                        session.getClient().getReplyString());
                            }
                        }
                        else {
                            // Interrupted transfer
                            if(!session.getClient().abort()) {
                                log.error("Error closing data socket:" + session.getClient().getReplyString());
                            }
                        }
                    }
                }
            };
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Upload failed", e, this);
        }
    }
}
