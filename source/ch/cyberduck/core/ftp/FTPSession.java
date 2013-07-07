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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FTPExceptionMappingService;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.ftp.parser.LaxUnixFTPEntryParser;
import ch.cyberduck.core.ftp.parser.RumpusFTPEntryParser;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPCommand;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.parser.NetwareFTPEntryParser;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Opens a connection to the remote server via ftp protocol
 *
 * @version $Id$
 */
public class FTPSession extends SSLSession<FTPClient> {
    private static final Logger log = Logger.getLogger(FTPSession.class);

    /**
     * Directory listing parser depending on response for SYST command
     */
    private CompositeFileEntryParser parser;

    public FTPSession(Host h) {
        super(h);
    }

    @Override
    public Path mount() throws BackgroundException {
        final Path workdir = super.mount();
        if(Preferences.instance().getBoolean("ftp.timezone.auto")) {
            if(null == host.getTimezone()) {
                // No custom timezone set
                final List<TimeZone> matches = new FTPTimezoneCalculator().get(this, workdir);
                for(TimeZone tz : matches) {
                    // Save in bookmark. User should have the option to choose from determined zones.
                    host.setTimezone(tz);
                    break;
                }
            }
        }
        return workdir;
    }

    /**
     * @param p Parser
     * @return True if the parser will read the file permissions
     */
    protected boolean isPermissionSupported(final FTPFileEntryParser p) {
        FTPFileEntryParser delegate;
        if(p instanceof CompositeFileEntryParser) {
            // Get the actual parser
            delegate = ((CompositeFileEntryParser) p).getCurrent();
            if(null == delegate) {
                log.warn("Composite FTP parser has no cached delegate yet");
                return false;
            }
        }
        else {
            // Not a composite parser
            delegate = p;
        }
        return delegate instanceof UnixFTPEntryParser
                || delegate instanceof LaxUnixFTPEntryParser
                || delegate instanceof NetwareFTPEntryParser
                || delegate instanceof RumpusFTPEntryParser;
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.logout();
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
        finally {
            client.removeProtocolCommandListener(listener);
        }
    }

    @Override
    protected void disconnect() {
        try {
            client.disconnect();
            super.disconnect();
        }
        catch(IOException e) {
            log.warn(String.format("Ignore disconnect failure %s", e.getMessage()));
        }
        finally {
            client.removeProtocolCommandListener(listener);
        }
    }

    @Override
    public void noop() throws BackgroundException {
        try {
            client.sendNoOp();
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    private final ProtocolCommandListener listener = new LoggingProtocolCommandListener() {
        @Override
        public void log(boolean request, String event) {
            FTPSession.this.log(request, event);
        }
    };

    protected void configure(final FTPClient client) throws IOException {
        client.setControlEncoding(this.getEncoding());
        client.removeProtocolCommandListener(listener);
        client.addProtocolCommandListener(listener);
        client.setConnectTimeout(this.timeout());
        client.setDefaultTimeout(this.timeout());
        client.setDataTimeout(this.timeout());
        client.setDefaultPort(Protocol.FTP.getDefaultPort());
        client.setParserFactory(new FTPParserFactory());
        client.setRemoteVerificationEnabled(Preferences.instance().getBoolean("ftp.datachannel.verify"));
        if(host.getProtocol().isSecure()) {
            List<String> protocols = new ArrayList<String>();
            for(String protocol : Preferences.instance().getProperty("connection.ssl.protocols").split(",")) {
                protocols.add(protocol.trim());
            }
            client.setEnabledProtocols(protocols.toArray(new String[protocols.size()]));
        }
        final int buffer = Preferences.instance().getInteger("ftp.socket.buffer");
        client.setBufferSize(buffer);
        client.setReceiveBufferSize(buffer);
        client.setSendBufferSize(buffer);
        client.setReceieveDataSocketBufferSize(buffer);
        client.setSendDataSocketBufferSize(buffer);
    }

    /**
     * @return True if the feaatures AUTH TLS, PBSZ and PROT are supported.
     * @throws BackgroundException Error reading FEAT response
     */
    protected boolean isTLSSupported() throws BackgroundException {
        try {
            return client.isFeatureSupported("AUTH TLS")
                    && client.isFeatureSupported("PBSZ")
                    && client.isFeatureSupported("PROT");
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    @Override
    public FTPClient connect(final HostKeyController key) throws BackgroundException {
        final CustomTrustSSLProtocolSocketFactory f
                = new CustomTrustSSLProtocolSocketFactory(this.getTrustManager());

        final FTPClient client = new FTPClient(f, f.getSSLContext());
        try {
            this.configure(client);
            client.connect(host.getHostname(true), host.getPort());
            client.setTcpNoDelay(false);
            final TimeZone zone = host.getTimezone();
            if(log.isInfoEnabled()) {
                log.info(String.format("Reset parser to timezone %s", zone));
            }
            String system = null; //Unknown
            try {
                system = client.getSystemType();
                if(system.toUpperCase(java.util.Locale.ENGLISH).contains(FTPClientConfig.SYST_NT)) {
                    // Workaround for #5572.
                    this.setStatListSupportedEnabled(false);
                }
            }
            catch(IOException e) {
                log.warn(String.format("SYST command failed %s", e.getMessage()));
            }
            parser = new FTPParserSelector().getParser(system, zone);
            return client;
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    protected FTPConnectMode getConnectMode() {
        if(null == host.getFTPConnectMode()) {
            if(ProxyFactory.get().usePassiveFTP()) {
                return FTPConnectMode.PASV;
            }
            return FTPConnectMode.PORT;
        }
        return this.host.getFTPConnectMode();

    }

    @Override
    public boolean alert() throws BackgroundException {
        if(super.alert()) {
            return !this.isTLSSupported();
        }
        return false;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        try {
            if(!host.getProtocol().isSecure() && this.isTLSSupported()) {
                // Propose protocol change if AUTH TLS is available.
                try {
                    prompt.warn(MessageFormat.format(Locale.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                            MessageFormat.format(Locale.localizedString("The server supports encrypted connections. Do you want to switch to {0}?", "Credentials"), Protocol.FTP_TLS.getName()),
                            Locale.localizedString("Continue", "Credentials"),
                            Locale.localizedString("Change", "Credentials"),
                            "connection.unsecure." + host.getHostname());
                    // Continue choosen. Login using plain FTP.
                }
                catch(LoginCanceledException e) {
                    // Protocol switch
                    host.setProtocol(Protocol.FTP_TLS);
                    // Reconfigure client for TLS
                    this.configure(client);
                    client.execAUTH();
                    client.sslNegotiation();
                }
            }
            if(client.login(host.getCredentials().getUsername(), host.getCredentials().getPassword())) {
                if(host.getProtocol().isSecure()) {
                    client.execPBSZ(0);
                    // Negotiate data connection security
                    client.execPROT(Preferences.instance().getProperty("ftp.tls.datachannel"));
                }
                if("UTF-8".equals(this.getEncoding())) {
                    if(client.isFeatureSupported("UTF8")) {
                        if(!FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8 ON"))) {
                            log.warn(String.format("Failed to negotiate UTF-8 charset %s", client.getReplyString()));
                        }
                    }
                }
            }
            else {
                throw new LoginFailureException(client.getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    @Override
    public Path workdir() throws BackgroundException {
        final String directory;
        try {
            directory = client.printWorkingDirectory();
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
        return new FTPPath(directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
    }

    @Override
    public boolean isDownloadResumable() {
        return true;
    }

    @Override
    public boolean isUploadResumable() {
        return true;
    }

    /**
     * The sever supports STAT file listings
     */
    private boolean statListSupportedEnabled = Preferences.instance().getBoolean("ftp.command.stat");

    public void setStatListSupportedEnabled(boolean e) {
        this.statListSupportedEnabled = e;
    }

    public boolean isStatListSupportedEnabled() {
        return statListSupportedEnabled;
    }

    /**
     * The server supports MLSD
     */
    private boolean mlsdListSupportedEnabled = Preferences.instance().getBoolean("ftp.command.mlsd");

    public void setMlsdListSupportedEnabled(boolean e) {
        this.mlsdListSupportedEnabled = e;
    }

    public boolean isMlsdListSupportedEnabled() {
        return mlsdListSupportedEnabled;
    }

    /**
     * The server supports LIST -a
     */
    private boolean extendedListEnabled = Preferences.instance().getBoolean("ftp.command.lista");

    public void setExtendedListEnabled(boolean e) {
        this.extendedListEnabled = e;
    }

    public boolean isExtendedListEnabled() {
        return extendedListEnabled;
    }

    private boolean utimeSupported = Preferences.instance().getBoolean("ftp.command.utime");

    public boolean isUtimeSupported() {
        return utimeSupported;
    }

    public void setUtimeSupported(boolean utimeSupported) {
        this.utimeSupported = utimeSupported;
    }

    protected abstract static class DataConnectionAction<T> {
        public abstract T run() throws IOException;
    }

    /**
     * @param action Action that needs to open a data connection
     * @return True if action was successful
     */
    protected <T> T data(final Path file, final DataConnectionAction<T> action)
            throws IOException, BackgroundException {
        try {
            // Make sure to always configure data mode because connect event sets defaults.
            if(this.getConnectMode().equals(FTPConnectMode.PASV)) {
                this.getClient().enterLocalPassiveMode();
            }
            else if(this.getConnectMode().equals(FTPConnectMode.PORT)) {
                this.getClient().enterLocalActiveMode();
            }
            return action.run();
        }
        catch(SocketTimeoutException failure) {
            log.warn(String.format("Timeout opening data socket %s", failure.getMessage()));
            // Fallback handling
            if(Preferences.instance().getBoolean("ftp.connectmode.fallback")) {
                this.interrupt();
                this.open(new DefaultHostKeyController());
                this.login(new DisabledPasswordStore(), new DisabledLoginController());
                try {
                    return this.fallback(action);
                }
                catch(IOException e) {
                    this.interrupt();
                    log.warn("Connect mode fallback failed:" + e.getMessage());
                    // Throw original error message
                }
            }
            throw new DefaultIOExceptionMappingService().map(failure, file);
        }
    }

    /**
     * @param action Action that needs to open a data connection
     * @return True if action was successful
     */
    protected <T> T fallback(final DataConnectionAction<T> action) throws ConnectionCanceledException, IOException {
        // Fallback to other connect mode
        if(this.getClient().getDataConnectionMode() == FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to active data connection");
            this.getClient().enterLocalActiveMode();
        }
        else if(this.getClient().getDataConnectionMode() == FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to passive data connection");
            this.getClient().enterLocalPassiveMode();
        }
        return action.run();
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();

            // Cached file parser determined from SYST response with the timezone set from the bookmark
            boolean success = false;
            try {
                if(this.isStatListSupportedEnabled()) {
                    int response = this.getClient().stat(file.getAbsolute());
                    if(FTPReply.isPositiveCompletion(response)) {
                        final String[] reply = this.getClient().getReplyStrings();
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
                        success = new FTPListResponseReader().read(children, this, file, parser, result);
                    }
                    else {
                        this.setStatListSupportedEnabled(false);
                    }
                }
            }
            catch(IOException e) {
                log.warn("Command STAT failed with I/O error:" + e.getMessage());
                this.interrupt();
                this.open(new DefaultHostKeyController());
                this.login(new DisabledPasswordStore(), new DisabledLoginController());
            }
            if(!success || children.isEmpty()) {
                success = this.data(file, new DataConnectionAction<Boolean>() {
                    @Override
                    public Boolean run() throws IOException {
                        if(!getClient().changeWorkingDirectory(file.getAbsolute())) {
                            throw new FTPException(getClient().getReplyCode(),
                                    getClient().getReplyString());
                        }
                        if(!getClient().setFileType(FTPClient.ASCII_FILE_TYPE)) {
                            // Set transfer type for traditional data socket file listings. The data transfer is over the
                            // data connection in type ASCII or type EBCDIC.
                            throw new FTPException(getClient().getReplyCode(),
                                    getClient().getReplyString());
                        }
                        boolean success = false;
                        // STAT listing failed or empty
                        if(isMlsdListSupportedEnabled()
                                // Note that there is no distinct FEAT output for MLSD.
                                // The presence of the MLST feature indicates that both MLST and MLSD are supported.
                                && getClient().isFeatureSupported(FTPCommand.MLST)) {
                            success = new FTPMlsdListResponseReader().read(children, FTPSession.this, file,
                                    null, getClient().list(FTPCommand.MLSD));
                            if(!success) {
                                setMlsdListSupportedEnabled(false);
                            }
                        }
                        if(!success) {
                            // MLSD listing failed or not enabled
                            if(isExtendedListEnabled()) {
                                try {
                                    success = new FTPListResponseReader().read(children, FTPSession.this, file,
                                            parser, getClient().list(FTPCommand.LIST, "-a"));
                                }
                                catch(FTPException e) {
                                    setExtendedListEnabled(false);
                                }
                            }
                            if(!success) {
                                // LIST -a listing failed or not enabled
                                success = new FTPListResponseReader().read(children, FTPSession.this, file,
                                        parser, getClient().list(FTPCommand.LIST));
                            }
                        }
                        return success;
                    }
                });
            }
            for(Path child : children) {
                if(child.attributes().isSymbolicLink()) {
                    if(this.getClient().changeWorkingDirectory(child.getAbsolute())) {
                        child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                    }
                    else {
                        // Try if CWD to symbolic link target succeeds
                        if(this.getClient().changeWorkingDirectory(child.getSymlinkTarget().getAbsolute())) {
                            // Workdir change succeeded
                            child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                        }
                        else {
                            child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
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
            log.warn(String.format("Directory listing failure for %s with failure %s", file, e.getMessage()));
            throw new FTPExceptionMappingService().map("Listing directory failed", e, file);
        }
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        try {
            if(!this.getClient().makeDirectory(file.getAbsolute())) {
                throw new FTPException(this.getClient().getReplyCode(),
                        this.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }

    @Override
    public void rename(final Path file, final Path renamed) throws BackgroundException {
        try {
            this.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    file.getName(), renamed));

            if(!this.getClient().rename(file.getAbsolute(), renamed.getAbsolute())) {
                throw new FTPException(this.getClient().getReplyCode(),
                        this.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public void delete(final Path file, final LoginController prompt) throws BackgroundException {
        try {
            this.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    file.getName()));

            if(file.attributes().isFile() || file.attributes().isSymbolicLink()) {
                if(!this.getClient().deleteFile(file.getAbsolute())) {
                    throw new FTPException(this.getClient().getReplyCode(),
                            this.getClient().getReplyString());
                }
            }
            else if(file.attributes().isDirectory()) {
                for(Path child : this.list(file)) {
                    if(!this.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    this.delete(child, prompt);
                }
                this.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        file.getName()));

                if(!this.getClient().removeDirectory(file.getAbsolute())) {
                    throw new FTPException(this.getClient().getReplyCode(),
                            this.getClient().getReplyString());
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot delete {0}", e, file);

        }
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(!this.getClient().setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new FTPException(this.getClient().getReplyCode(),
                        this.getClient().getReplyString());
            }
            if(status.isResume()) {
                // Where a server process supports RESTart in STREAM mode
                if(!this.getClient().isFeatureSupported("REST STREAM")) {
                    status.setResume(false);
                }
                else {
                    this.getClient().setRestartOffset(status.getCurrent());
                }
            }
            final InputStream in = this.data(file, new DataConnectionAction<InputStream>() {
                @Override
                public InputStream run() throws IOException {
                    return getClient().retrieveFileStream(file.getAbsolute());
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
                            if(!getClient().completePendingCommand()) {
                                throw new FTPException(getClient().getReplyCode(),
                                        getClient().getReplyString());
                            }
                        }
                        else {
                            // Interrupted transfer
                            if(!getClient().abort()) {
                                log.error("Error closing data socket:" + getClient().getReplyString());
                            }
                        }
                    }
                }
            };
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Download failed", e, file);
        }
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(!this.getClient().setFileType(FTPClient.BINARY_FILE_TYPE)) {
                throw new FTPException(this.getClient().getReplyCode(),
                        this.getClient().getReplyString());
            }
            final OutputStream out = this.data(file, new DataConnectionAction<OutputStream>() {
                @Override
                public OutputStream run() throws IOException {
                    if(status.isResume()) {
                        return getClient().appendFileStream(file.getAbsolute());
                    }
                    else {
                        return getClient().storeFileStream(file.getAbsolute());
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
                            if(!getClient().completePendingCommand()) {
                                throw new FTPException(getClient().getReplyCode(),
                                        getClient().getReplyString());
                            }
                        }
                        else {
                            // Interrupted transfer
                            if(!getClient().abort()) {
                                log.error("Error closing data socket:" + getClient().getReplyString());
                            }
                        }
                    }
                }
            };
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Upload failed", e, file);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == UnixPermission.class) {
            return (T) new FTPUnixPermissionFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new FTPTimestampFeature(this);
        }
        if(type == Command.class) {
            return (T) new FTPCommandFeature(this);
        }
        return super.getFeature(type, prompt);
    }
}