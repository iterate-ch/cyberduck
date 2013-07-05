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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.FTPExceptionMappingService;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.ftp.parser.LaxUnixFTPEntryParser;
import ch.cyberduck.core.ftp.parser.RumpusFTPEntryParser;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.parser.NetwareFTPEntryParser;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.apache.log4j.Logger;

import java.io.IOException;
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
     * @return Directory listing parser depending on response for SYST command
     * @throws IOException Failure initializing parser
     */
    public CompositeFileEntryParser getParser() throws IOException {
        return parser;
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
        return new FTPPath(this, directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
    }

    @Override
    public boolean isSendCommandSupported() {
        return true;
    }

    @Override
    public void sendCommand(final String command) throws BackgroundException {
        this.message(command);
        try {
            client.sendSiteCommand(command);
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
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

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == UnixPermission.class) {
            return (T) new FTPUnixPermissionFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new FTPTimestampFeature(this);
        }
        return null;
    }
}